package unife.icedroid;

import unife.icedroid.core.ICeDROID;

public class Settings {
    private static final String TAG = "Application Settings";
    private static final boolean DEBUG = true;

    private volatile static Settings instance;

    private Settings() throws Exception {
    	ChatsManager chatsManager = ChatsManager.getInstance();
        if (ICeDROID.getInstance(chatsManager) != null) {
            SubscriptionListManager.getSubscriptionListManager();
        } else {
            throw new Exception();
        }
    }

    public static Settings getSettings() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    try {
                        instance = new Settings();
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        if (DEBUG) {
                        	if (msg != null) {
                        		msg = TAG + " - " + msg;
                        	} else {
                        		msg = TAG + " - " + "Error loading settings!";
                        	}
                        	System.out.println(msg);
                        }
                        instance = null;
                    }
                }
            }
        }
        return instance;
    }
    
    public void close() {
    	ICeDROID.getInstance().close();
    }
}
