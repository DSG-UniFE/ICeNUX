package unife.icedroid;

import unife.icedroid.core.ICeDROIDMessage;
import unife.icedroid.resources.Constants;
import unife.icedroid.services.ApplevDisseminationChannelService.OnMessageReceiveListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChatsManager implements OnMessageReceiveListener {
    private static final String TAG = "ChatsManager";
    private static final boolean DEBUG = true;

    private static volatile ChatsManager instance;

    private ChatsManager() {}

    public static synchronized ChatsManager getInstance() {
        if (instance == null) {
            synchronized (ChatsManager.class) {
                if (instance == null) {
                    instance = new ChatsManager();
                }
            }
        }
        return instance;
    }

    public synchronized void saveMessageInConversation(TxtMessage message) throws IOException {
        String data = "[" + message.getReceptionTime().toString() + "] " +
                        message.getHostID() + ": " +
                        message.getContentData() + "\n";

        Subscription subscription = new Subscription(message.getChannel(), message.getGroup());
        String path = Constants.CONVERSATIONS_PATH + subscription.getSubscriptionFileName();
        File conversationLogFile = new File (path);
        if (!conversationLogFile.exists()) {
        	File conversationLogParentDir = conversationLogFile.getParentFile();
        	if (!conversationLogParentDir.exists() && !conversationLogParentDir.mkdirs()) {
        		throw new IOException("Failed to create the path: " + conversationLogParentDir);
        	}
        	conversationLogFile.createNewFile();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path, true);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + " - " + ((msg != null) ? msg : "Error writing to file");
            	System.err.println(msg);
            }
        }
    }

    public void receive (ICeDROIDMessage message) {
        try {
            TxtMessage txt = (TxtMessage) message;
            if (SubscriptionListManager.getSubscriptionListManager().isSubscribedToMessage(txt)) {
                saveMessageInConversation(txt);
            }
        } catch (Exception ex) {
        	System.err.println(TAG + " - error receiving the message with ID " +
        			message.getMsgID() + " of size " + message.getSize());
        }
    }
}
