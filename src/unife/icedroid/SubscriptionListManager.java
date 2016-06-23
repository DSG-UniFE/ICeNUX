package unife.icedroid;

import unife.icedroid.core.ICeDROID;
import unife.icedroid.resources.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SubscriptionListManager {
    private static final String TAG = "SubscriptionListManager";
    private static final boolean DEBUG = true;

    private volatile static SubscriptionListManager instance = null;

    private ArrayList<Subscription> subscriptionsList;


    private SubscriptionListManager() {
        subscriptionsList = new ArrayList<>(0);
        try {
            BufferedReader br = new BufferedReader(new FileReader(Constants.SUBSCRIPTIONS_FILE_NAME));

            Subscription subscription;
            String subscriptionLine;
            while ((subscriptionLine = br.readLine()) != null) {
                String[] channelAndGroup = subscriptionLine.split(":");
                subscription = new Subscription(channelAndGroup[0], channelAndGroup[1]);
                subscriptionsList.add(subscription);
            }
            br.close();
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	if (msg != null) {
            		msg = TAG + ": " + msg;
            	} else {
            		msg = TAG + ": " + "Error loading subscriptions list";
            	}
            	System.out.println(msg);
            }
        }
    }

    public static SubscriptionListManager getSubscriptionListManager() {
        if (instance == null) {
            synchronized (SubscriptionListManager.class) {
                if (instance == null) {
                    instance = new SubscriptionListManager();
                }
            }
        }
        return instance;
    }

    public synchronized Subscription subscribe(String channel, String group) {
        Subscription subscription = new Subscription(channel, group);
        if (!subscriptionsList.contains(subscription)) {
            subscriptionsList.add(subscription);

            ICeDROID.getInstance().subscribe(channel);

            try {
                FileOutputStream fos = new FileOutputStream(Constants.SUBSCRIPTIONS_FILE_NAME, true);
                fos.write((subscription.getSubscriptionFileName() + "\n").getBytes());
                fos.close();

                //Create conversation file                
                String conversationFilePath = Constants.CONVERSATIONS_PATH + subscription.getSubscriptionFileName();
                File conversationLogFile = new File (conversationFilePath);
                if (!conversationLogFile.exists()) {
                	if (!conversationLogFile.mkdirs()) {
                		throw new IOException("failed to create the path " + conversationFilePath);
                	}
                	conversationLogFile.createNewFile();
                }
                fos = new FileOutputStream(conversationLogFile);
                fos.close();

                if (DEBUG) {
                	System.out.println(TAG + " Subscribing to: " + subscription.toString());
                }
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (DEBUG) {
                	if (msg != null) {
                		msg = TAG + ": " + msg;
                	} else {
                		msg = TAG + ": " + "Error subscribing";
                	}
                	System.out.println(msg);
                }
            }
        }
        return subscription;
    }

    public synchronized ArrayList<Subscription> getSubscriptionsList() {
        return new ArrayList<>(subscriptionsList);
    }

    public synchronized boolean isSubscribedToMessage(TxtMessage msg) {
        Subscription subscription = new Subscription(msg.getChannel(), msg.getGroup());
        return subscriptionsList.contains(subscription);
    }

    public synchronized ArrayList<Subscription> getNewSubscriptions(
                                                        ArrayList<Subscription> oldSubscriptions) {
        ArrayList<Subscription> newSubscriptions = new ArrayList<>(0);
        for (Subscription s : getSubscriptionsList()) {
            if (!oldSubscriptions.contains(s)) {
                newSubscriptions.add(s);
            }
        }
        return newSubscriptions;
    }
    
    public synchronized Subscription getLastSubscription() {
    	return subscriptionsList.get(subscriptionsList.size() - 1);
    }

}