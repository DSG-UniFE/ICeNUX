package unife.icedroid.core.managers;

import unife.icedroid.core.BaseMessage;
import unife.icedroid.core.HelloMessage;
import unife.icedroid.core.ICeDROIDMessage;
import unife.icedroid.utils.Settings;
import unife.icedroid.core.messagecachingstrategies.MessageCachingStrategy;
import unife.icedroid.core.messageforwardingstrategies.MessageForwardingStrategy;
import java.util.*;

public class MessageQueueManager {
    private static final String TAG = "MessageQueueManager";
    private static final boolean DEBUG = true;

    private volatile static MessageQueueManager instance;

    private ArrayList<ICeDROIDMessage> cachedMessages;
    private ArrayList<ICeDROIDMessage> discardedMessages;
    private ArrayList<BaseMessage> forwardingMessages;
    private Timer cachedMessagesTimer;
    private Timer discardedMessagesTimer;
    private MessageCachingStrategy cachingStrategy;
    private MessageForwardingStrategy forwardingStrategy;

    private static int indexForwardingMessages;


    private MessageQueueManager() {
        cachedMessages = new ArrayList<>(0);
        discardedMessages = new ArrayList<>(0);
        forwardingMessages = new ArrayList<>(0);
        indexForwardingMessages = 0;

        cachedMessagesTimer = new Timer();
        discardedMessagesTimer = new Timer();

        cachingStrategy = MessageCachingStrategy.newInstance(Settings.getSettings());
        forwardingStrategy = MessageForwardingStrategy.newInstance(Settings.getSettings());
    }

    public static MessageQueueManager getMessageQueueManager() {
        if (instance == null) {
            synchronized (MessageQueueManager.class) {
                if (instance == null) {
                    instance = new MessageQueueManager();
                }
            }
        }
        return instance;
    }

    public boolean isCached(ICeDROIDMessage msg) {
        synchronized (cachedMessages) {
            return cachedMessages.contains(msg);
        }
    }

    public
    boolean isDiscarded(ICeDROIDMessage msg) {
        synchronized (discardedMessages) {
            return discardedMessages.contains(msg);
        }
    }

    public void addToCache(final ICeDROIDMessage msg) {
        if (!isExpired(msg)) {
            synchronized (cachedMessages) {
                cachingStrategy.add(cachedMessages, msg);
            }

            //If the msg TTL isn't infinite then set a timer to delete it.
            if (msg.getTtl() != BaseMessage.INFINITE_TTL) {
                cachedMessagesTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        removeFromQueue(cachedMessages, msg);
                    }

                }, new Date(msg.getCreationTime().getTime() + msg.getTtl()));
            }
        }
    }

    public void addToDiscarded(ICeDROIDMessage iceMsg) {
        if (!isExpired(iceMsg)) {

            //Remove payload (data)
            final ICeDROIDMessage msg = new ICeDROIDMessage(iceMsg);

            synchronized (discardedMessages) {
                discardedMessages.add(msg);
            }

            //If the msg TTL isn't infinite then set a timer to delete it.
            if (msg.getTtl() != BaseMessage.INFINITE_TTL) {
                discardedMessagesTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        removeFromQueue(discardedMessages, msg);
                    }

                }, new Date(msg.getCreationTime().getTime() + msg.getTtl()));
            }
        }
    }

    public void addToForwardingMessages(BaseMessage msg) {
        synchronized (forwardingMessages) {
            forwardingStrategy.add(forwardingMessages, msg, indexForwardingMessages);
            forwardingMessages.notifyAll();
        }
    }

    public ArrayList<ICeDROIDMessage> getCachedMessages() {
        synchronized (cachedMessages) {
            return new ArrayList<>(cachedMessages);
        }
    }

    public ArrayList<ICeDROIDMessage> getDiscardedMessages() {
        synchronized (discardedMessages) {
            return new ArrayList<>(discardedMessages);
        }
    }

    public ArrayList<BaseMessage> getForwardingMessages() {
        synchronized (forwardingMessages) {
            return new ArrayList<>(forwardingMessages);
        }
    }

    public void removeFromQueue(ArrayList<?> queue, BaseMessage msg) {
        synchronized (queue) {
            queue.remove(msg);
        }
    }

    public void removeMessageFromCachedMessages(ICeDROIDMessage msg) {
        removeFromQueue(cachedMessages, msg);
    }

    public void removeMessageFromForwardingMessages(BaseMessage msg) {
       removeFromQueue(forwardingMessages, msg);
    }

    public void removeICeDROIDMessagesFromForwardingMessages() {
        synchronized (forwardingMessages) {
            ArrayList<BaseMessage> fm = getForwardingMessages();

            for (BaseMessage m : fm) {
                if (m.getTypeOfMessage().equals(ICeDROIDMessage.ICEDROID_MESSAGE)) {
                    forwardingMessages.remove(m);
                }
            }
        }
    }

    public BaseMessage getMessageToSend() throws InterruptedException {
        BaseMessage message = null;
        synchronized (forwardingMessages) {
            while (message == null) {
                while (forwardingMessages.size() == 0) {
                    try {
                        forwardingMessages.wait();
                    } catch (InterruptedException ex) {
                        throw ex;
                    }
                }

                if (indexForwardingMessages >= forwardingMessages.size()) {
                    indexForwardingMessages = 0;
                }

                message = forwardingMessages.get(indexForwardingMessages);

                //Hellomessages must be sent just once
                if (message.getTypeOfMessage().equals(HelloMessage.HELLO_MESSAGE)) {
                    forwardingMessages.remove(indexForwardingMessages);
                } else {
                    if (isExpired(message)) {
                        forwardingMessages.remove(indexForwardingMessages);
                        message = null;
                    } else {
                        indexForwardingMessages++;
                    }
                }
            }
            return message;
        }

    }

    public boolean isExpired(BaseMessage msg) {
        if (msg.getTtl() != BaseMessage.INFINITE_TTL) {
            if (System.currentTimeMillis() > msg.getCreationTime().getTime() + msg.getTtl()) {
                return true;
            }
        }
        return false;
    }
}