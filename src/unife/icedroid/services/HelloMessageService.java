package unife.icedroid.services;

import unife.icedroid.core.*;
import unife.icedroid.core.managers.MessageQueueManager;
import unife.icedroid.core.managers.NeighborhoodManager;
import unife.icedroid.utils.Settings;
import java.util.*;

public class HelloMessageService extends Thread {
    private static final String TAG = "HelloMessageService";
    private static final boolean DEBUG = true;

    private MessageQueueManager messageQueueManager;
    private Timer helloMessageTimer;
    private ArrayList<Intent> intents;
    private ApplicationLevelDisseminationChannelService ADCThread;
    private final int helloMessagePeriod;

    public HelloMessageService() {
    	intents = new ArrayList<>();
        messageQueueManager = MessageQueueManager.getMessageQueueManager();
        ADCThread = Settings.getSettings().getADCThread();
        helloMessagePeriod = Settings.getSettings().getHelloMessagePeriod();

        helloMessageTimer = new Timer(TAG);
        helloMessageTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                HelloMessage helloMessage = new HelloMessage();
                messageQueueManager.addToForwardingQueue(helloMessage);
            }

        }, new Date(System.currentTimeMillis()), helloMessagePeriod);

        ADCThread.registerHelloMessageService(this);
    }

    
/*
 * The run method of the HelloMessageService processes intents from the
 * MessageDispatcher, associated to newly received HELLO messages.
 */
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
    		
    		HelloMessage helloMessage = (HelloMessage) intent.getExtra(HelloMessage.EXTRA_HELLO_MESSAGE);
            NeighborInfo neighbor = createNeighborInfo(helloMessage);
            
            // If add() returns null, the neighbor is new
            ArrayList<String> newChannels = NeighborhoodManager.getNeighborhoodManager().add(neighbor);

            intent = new Intent();
            intent.putExtra(HelloMessage.EXTRA_HELLO_MESSAGE, helloMessage);
            if (newChannels == null) {
                // There is a new neighbor --> there's the need to recalculate messages to forward
                intent.putExtra(NeighborInfo.EXTRA_NEW_NEIGHBOR, true);
            }
            else {
                // All neighbors have a message --> no need to forward it anymore
                intent.putExtra(NeighborInfo.EXTRA_NEIGHBOR_UPDATE, true);
                intent.putExtra(NeighborInfo.EXTRA_NEW_CHANNELS, newChannels);
            }
            ADCThread.add(intent);
    	}
    	
    	helloMessageTimer.cancel();
    }
    
    public void add(Intent intent) {
        synchronized (intents) {
            intents.add(intent);
            intents.notifyAll();
        }
    }
    
    public void addAckingHelloMessageToQueue() {
    	helloMessageTimer.cancel();
    	helloMessageTimer = new Timer(TAG);
        helloMessageTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                HelloMessage helloMessage = new HelloMessage();
                messageQueueManager.addToForwardingQueue(helloMessage);
            }

        }, new Date(System.currentTimeMillis()), helloMessagePeriod);
        if (DEBUG) {
        	System.out.println(TAG + " - Scheduled a new HELLO message for ACKing");
        }
    }
    
    private NeighborInfo createNeighborInfo(HelloMessage helloMessage) {
        return new NeighborInfo(helloMessage.getHostID(), helloMessage.getHostUsername(), null,
        		helloMessage.getHostSubscribedADCs(), helloMessage.getCachedMessages());
    }

}