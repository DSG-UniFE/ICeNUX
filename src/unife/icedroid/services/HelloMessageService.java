package unife.icedroid.services;

import unife.icedroid.core.*;
import unife.icedroid.core.managers.MessageQueueManager;
import unife.icedroid.core.managers.NeighborhoodManager;
import unife.icedroid.utils.Settings;
import java.util.*;

public class HelloMessageService extends Thread {
    private static final String TAG = "HelloMessageService";
    private static final boolean DEBUG = true;
    private static final int DURATION = 15*1000;

    private MessageQueueManager messageQueueManager;
    private Timer helloMessageTimer;
    private ArrayList<Intent> intents;
    private ApplevDisseminationChannelService ADCThread;

    public HelloMessageService() {
    	intents = new ArrayList<>();
        messageQueueManager = MessageQueueManager.getMessageQueueManager();
        ADCThread = Settings.getSettings().getADCThread();

        helloMessageTimer = new Timer(TAG);
        helloMessageTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                HelloMessage helloMessage = new HelloMessage();
                messageQueueManager.addToForwardingMessages(helloMessage);
            }

        }, new Date(System.currentTimeMillis()), DURATION);

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
    		
    		HelloMessage helloMessage = (HelloMessage) intent.getExtra(HelloMessage.EXTRA_HELLO_MESSAGE);

            NeighborInfo neighbor = createNeighborInfo(helloMessage);
            ArrayList<String> newChannels = NeighborhoodManager.getNeighborhoodManager().add(neighbor);

            intent = new Intent();
            intent.putExtra(HelloMessage.EXTRA_HELLO_MESSAGE, helloMessage);

            if (newChannels == null) {
                // If there is a new neighbor then there's the need to recalculate messages to forward
                intent.putExtra(NeighborInfo.EXTRA_NEW_NEIGHBOR, true);
            } else {
                // If all neighbors have a message, then is no need to forward it anymore
                intent.putExtra(NeighborInfo.EXTRA_NEIGHBOR_UPDATE, true);
                intent.putExtra(NeighborInfo.EXTRA_NEW_CHANNELS, newChannels);
            }
            ADCThread.add(intent);
    	}
    	helloMessageTimer.cancel();
    }
    
    
    private NeighborInfo createNeighborInfo(HelloMessage helloMessage) {
        return new NeighborInfo(helloMessage.getHostID(), helloMessage.getHostUsername(), null,
        		helloMessage.getHostSubscribedADCs(), helloMessage.getCachedMessages());
    }
    
    public void add(Intent intent) {
        synchronized (intents) {
            intents.add(intent);
            intents.notifyAll();
        }
    }

}