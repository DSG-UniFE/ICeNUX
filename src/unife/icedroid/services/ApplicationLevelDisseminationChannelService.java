package unife.icedroid.services;

import unife.icedroid.core.Intent;
import unife.icedroid.core.BaseMessage;
import unife.icedroid.core.HelloMessage;
import unife.icedroid.core.ICeDROIDMessage;
import unife.icedroid.core.NeighborInfo;
import unife.icedroid.core.managers.*;
import unife.icedroid.utils.Settings;
import java.util.ArrayList;
import java.util.Random;

public class ApplicationLevelDisseminationChannelService extends Thread {
    private static final String TAG = "AppDissChannelService";
    private static final boolean DEBUG = true;

    public static final String EXTRA_ADC_MESSAGE = "unife.icedroid.ADC_MESSAGE";

    private MessageQueueManager messageQueueManager;
    private ChannelListManager channelListManager;
    private NeighborhoodManager neighborhoodManager;
    private OnMessageReceiveListener onMessageReceiveListener;
    private final Settings settings;
    private ArrayList<Intent> intents;
    
    public final double cachingProbability;
    public final double forwardingProbability;


    public ApplicationLevelDisseminationChannelService(OnMessageReceiveListener listener) {
        messageQueueManager = MessageQueueManager.getMessageQueueManager();
        channelListManager = ChannelListManager.getChannelListManager();
        neighborhoodManager = NeighborhoodManager.getNeighborhoodManager();
        onMessageReceiveListener = listener;
        settings = Settings.getSettings();
        intents = new ArrayList<>(0);
        
        cachingProbability = settings.getADCCachingProbability();
        forwardingProbability = settings.getADCForwardingProbability();
    }

    @Override
    public void run() {
        Intent intent;
        while (!Thread.interrupted()) {
            synchronized (intents) {
                while (intents.size() == 0) {
                    try {
                        intents.wait();
                    } catch (Exception ex) {}
                }
                intent = intents.get(0);
                intents.remove(0);
            }

            ICeDROIDMessage iceMessage = (ICeDROIDMessage) intent.getExtra(EXTRA_ADC_MESSAGE);

            /* There's a new regular message; first it needs to be decided whether 
             * to cache it or not, and then whether to forward it or not */
            if (iceMessage != null) {
                //This host's messages
                if (iceMessage.getHostID().equals(Settings.getSettings().getHostID())) {
                    switch (settings.getRoutingAlgorithm()) {
                        case SPRAY_AND_WAIT:
                            messageQueueManager.removeMessageFromForwardingQueue(iceMessage);
                            messageQueueManager.removeMessageFromCachedMessages(iceMessage);
                            messageQueueManager.addToCache(iceMessage);
                            forwardMessage(iceMessage, true);
                            break;
                        default:
                            break;
                    }

                } else {
                    //Other hosts' messages
                    boolean toCache = true;

                    if (!messageQueueManager.isCached(iceMessage) &&
                        !messageQueueManager.isDiscarded(iceMessage) &&
                        !messageQueueManager.isExpired(iceMessage)) {

                        if (channelListManager.isSubscribedToChannel(iceMessage)) {
                        	// Within-channel transmission --> deliver to the application
                            onMessageReceiveListener.receive(iceMessage);
                        } else {
                            switch (settings.getRoutingAlgorithm()) {
                                case SPRAY_AND_WAIT:
                                    Integer L = iceMessage.getProperty("L");
                                    /* Message discarded if Spray and Wait is not in the spraying
                                     * phase and the membrane-passing procedure fails */
                                    if (L == null || L <= 0) {
                                        Random random = new Random(System.currentTimeMillis());
                                        if (random.nextDouble() > cachingProbability) {
                                            toCache = false;
                                            messageQueueManager.addToDiscarded(iceMessage);
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }

                        if (toCache) {
                            messageQueueManager.addToCache(iceMessage);
                            forwardMessage(iceMessage, false);
                        }
                    }
                }
            } else if (intent.hasExtra(ChannelListManager.EXTRA_NEW_CHANNEL)) {
            	/* If the host has become member of a new ADC, look among all cached
            	 * messages for those eligible for delivery to the application */
                for (ICeDROIDMessage msg : messageQueueManager.getCachedMessages()) {
                    if (channelListManager.isSubscribedToChannel(msg)) {
                        onMessageReceiveListener.receive(msg);
                    }
                }
            }
            // Received a HELLO message from a (new?) neighbor
            else {
                HelloMessage helloMessage = (HelloMessage)
                		intent.getExtra(HelloMessage.EXTRA_HELLO_MESSAGE);

                if (intent.hasExtra(NeighborInfo.EXTRA_NEW_NEIGHBOR)) {
                	// There is a new neighbor -> recompute list of messages to forward
                    messageQueueManager.removeICeDROIDMessagesFromForwardingQueue();

                    for (ICeDROIDMessage msg : messageQueueManager.getCachedMessages()) {
                        forwardMessage(msg, false);
                    }
                    
                } else {
                    //If everyone has a message then stop forwarding it
                    if (DEBUG) {
                        System.out.println(TAG + " - Handling the HELLO Message UPDATE " +
                        		helloMessage.getMsgID());
                    }
                    ArrayList<String> newChannels = (ArrayList<String>) intent.
                                            getExtra(NeighborInfo.EXTRA_NEW_CHANNELS);
                    NeighborInfo n = neighborhoodManager.getNeighborByID(helloMessage.getHostID());
                    for (ICeDROIDMessage msg : messageQueueManager.getCachedMessages()) {
                        if (newChannels.contains(msg.getADCID())) {
                            if (!n.hasInCache(msg)) {
                                messageQueueManager.addToForwardingQueue(msg);
                            }
                        }
                    }

                    ArrayList<BaseMessage> fm = messageQueueManager.getForwardingQueue();

                    for (BaseMessage m : fm) {
                        if (m.getTypeOfMessage().equals(ICeDROIDMessage.ICEDROID_MESSAGE)) {
                            if (neighborhoodManager.everyoneHasThisMessage(m)) {
                                if (DEBUG) System.out.println(TAG + " Handling an HelloMessage: removed " + m);
                                messageQueueManager.removeMessageFromForwardingQueue(m);
                            }
                        }
                    }
                }
            }
        }
    }

    public void add(Intent intent) {
        synchronized (intents) {
            intents.add(intent);
            intents.notifyAll();
        }
    }

    public void add(OnMessageReceiveListener listener) {
    	onMessageReceiveListener = listener;
    }

    private void forwardMessage(ICeDROIDMessage msg, boolean thisHostMessage) {
        boolean send = false;
        switch (Settings.getSettings().getRoutingAlgorithm()) {
            case SPRAY_AND_WAIT:
                //If the message is new from this host
                if (thisHostMessage) {
                    if (neighborhoodManager.isThereNeighborSubscribedToChannel(msg)) {
                        send = true;
                    } else if (neighborhoodManager.isThereNeighborWithoutThisMessage(msg)) {
                        Random random = new Random(System.currentTimeMillis());
                        if (random.nextDouble() <= forwardingProbability) {
                            send = true;
                        }
                    }
                } else {
                    //If we are not in the spraying phase or the message is not new,
                    //then according to the Spray and Wait Algorithm the message must be
                    //delivered only if there is a direct interested neighbor.
                    if (neighborhoodManager.isThereNeighborSubscribedToChannel(msg)) {
                        send = true;
                    }
                }
                break;
            default:
                break;
        }

        if (send) {
            messageQueueManager.addToForwardingQueue(msg);
        }

    }
    
    public interface OnMessageReceiveListener {
        public void receive(ICeDROIDMessage message);
    }
}