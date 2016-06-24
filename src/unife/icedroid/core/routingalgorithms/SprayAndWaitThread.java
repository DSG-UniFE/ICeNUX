package unife.icedroid.core.routingalgorithms;

import unife.icedroid.core.Intent;
import unife.icedroid.core.NeighborInfo;
import unife.icedroid.core.ICeDROIDMessage;
import unife.icedroid.core.managers.MessageQueueManager;
import unife.icedroid.core.managers.NeighborhoodManager;
import unife.icedroid.services.ApplevDisseminationChannelService;
import unife.icedroid.utils.Settings;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class SprayAndWaitThread extends Thread implements RoutingAlgorithm {
    private static final String TAG = "SprayAndWaitThread";
    private static final boolean DEBUG = true;

    private ArrayList<ICeDROIDMessage> messages;
    private ArrayList<ArrayList<String>> ackLists;
    private Lock lock;
    private Condition condition;
    private boolean stopped;

    public SprayAndWaitThread() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        messages = new ArrayList<>(0);
        ackLists = new ArrayList<>(0);
        stopped = false;
    }

    @Override
    public void run() {
        lock.lock();
        //Wait for the first message
        while (messages.size() == 0) {
            try {
                condition.await();
            } catch (Exception ex) {
            }
        }
        lock.unlock();

        ICeDROIDMessage msg;
        NeighborhoodManager neighborhoodManager = NeighborhoodManager.getNeighborhoodManager();
        MessageQueueManager messageQueueManager = MessageQueueManager.getMessageQueueManager();
        int L = 1;
        int msgL;
        Intent intent = new Intent();
        ArrayList<String> ackL;
        int index = 0;

        while (!isInterrupted()) {
            lock.lock();
            if (messages.size() == 0) {
                interrupt();
                stopped = true;
                lock.unlock();
            } else {
                lock.unlock();
                msg = messages.get(index);

                if (!messageQueueManager.isExpired(msg)) {

                    if (msg.getProperty("L") == null) {
                        msg.setProperty("L", L);
                        ICeDROIDMessage cMsg = msg.clone();
                        if (neighborhoodManager.getNumberOfNeighbors() == 0) {
                            intent.putExtra(ApplevDisseminationChannelService.EXTRA_ADC_MESSAGE,
                                                                                            cMsg);
                            Settings.getSettings().getADCThread().add(intent);
                        }
                    } else {
                        msgL = msg.getProperty("L");

                        ackL = ackLists.get(index);
                        for (NeighborInfo neighbor : neighborhoodManager.
                                whoHasThisMessageButNotInterested(msg)) {
                            if (!ackL.contains(neighbor.getHostID())) {
                                msgL = (int) Math.ceil(L / 2);
                                ackL.add(neighbor.getHostID());
                                if (msgL <= 0) {
                                    break;
                                }
                            }
                        }
                        msg.setProperty("L", msgL);

                        if (msgL > 0) {
                            if (neighborhoodManager.isThereNeighborSubscribedToChannel(msg)) {
                                ICeDROIDMessage cMsg = msg.clone();
                                cMsg.setProperty("L", 0);
                                intent.putExtra(ApplevDisseminationChannelService.EXTRA_ADC_MESSAGE,
                                        cMsg);
                                Settings.getSettings().getADCThread().add(intent);
                            } else {
                                if (neighborhoodManager.
                                        isThereNeighborNotInterestedToMessageAndNotCached(msg)) {
                                    ICeDROIDMessage cMsg = msg.clone();
                                    cMsg.setProperty("L", msgL);
                                    intent.putExtra(
                                         ApplevDisseminationChannelService.EXTRA_ADC_MESSAGE, cMsg);
                                    Settings.getSettings().getADCThread().add(intent);
                                }
                            }

                        } else {
                            removeMessage(msg, index);
                            messageQueueManager.removeMessageFromForwardingMessages(msg);
                            messageQueueManager.removeMessageFromCachedMessages(msg);
                            messageQueueManager.addToCache(msg);
                        }
                    }

                } else {
                    removeMessage(msg, index);
                }

                boolean waitForUpdate = false;
                lock.lock();
                if (index + 1 >= messages.size()) {
                    index = 0;
                    waitForUpdate = true;
                } else {
                    index++;
                }
                lock.unlock();

                if (waitForUpdate) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {}
                }
            }
        }
    }

    public boolean addMessageForTransmission(ICeDROIDMessage msg) {
        boolean result = false;
        
        lock.lock();
        if (!stopped) {
            messages.add(msg);
            ackLists.add(new ArrayList<String>());
            condition.signalAll();
            result = true;
        }
        lock.unlock();

        if (DEBUG && result) {
        	System.out.println(TAG + " - Added message with ID " + msg.getMsgID() + " and size " +
        			msg.getSize() + " to the queue for transmission");
        }
        
        return result;
    }

    public ICeDROIDMessage removeMessage(ICeDROIDMessage msg, int index) {
    	ICeDROIDMessage message = null;
    	
        lock.lock();
        message = messages.remove(index);
        ackLists.remove(index);
        lock.unlock();

        if (DEBUG) {
        	String info = TAG + " - " + ((message != null) ? "Removed message with ID " +
        			message.getMsgID() + " and size " + message.getSize() + " from the cache" :
        				"No message found at index " + index);
        	System.out.println(info);
        }
        
        return message;
    }
}