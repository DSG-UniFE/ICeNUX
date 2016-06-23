package unife.icedroid;

import unife.icedroid.core.ICeDROID;
import unife.icedroid.resources.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
        subscriptionsList = new ArrayList<Subscription>(0);
        try {
        	File subscriptionsFile = new File (Constants.SUBSCRIPTIONS_PATH);
            if (!subscriptionsFile.exists()) {
            	File subscriptionParentDir = subscriptionsFile.getParentFile();
            	if (!subscriptionParentDir.exists() && !subscriptionParentDir.mkdirs()) {
            		throw new IOException("Failed to create the path: " + subscriptionsFile.getParent());
            	}
            	subscriptionsFile.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(Constants.SUBSCRIPTIONS_PATH));

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
            	msg = TAG + ": " + ((msg != null) ? msg : "Error loading subscriptions list");
            	System.err.println(msg);
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

    public synchronized Subscription subscribe(String channel, String application) {
        Subscription subscription = new Subscription(channel, application);
        if (!subscriptionsList.contains(subscription)) {
            subscriptionsList.add(subscription);

            ICeDROID.getInstance().subscribe(channel);
            try {
            	FileOutputStream fos = null;
	            try {
	            	fos = new FileOutputStream(Constants.SUBSCRIPTIONS_PATH, true);
	            } catch (FileNotFoundException fnfex) {
	            	File subscriptionsParentDir = new File (Constants.SUBSCRIPTIONS_PATH).getParentFile();
	            	if (!subscriptionsParentDir.exists()) {
		            	if (!subscriptionsParentDir.mkdirs()) {
		            		throw new IOException("Failed to create the path: " + subscriptionsParentDir);
		            	}
	            	}
	            	fos = new FileOutputStream(Constants.SUBSCRIPTIONS_PATH, true);
	            }
                
                fos.write((subscription.toString() + "\n").getBytes());
                fos.close();

                //Create conversation file                
                String conversationFilePath = Constants.CONVERSATIONS_PATH + subscription.getSubscriptionFileName();
                File conversationLogFile = new File (conversationFilePath);
                if (!conversationLogFile.exists()) {
                	File conversationLogParentDir = conversationLogFile.getParentFile();
                	if (!conversationLogParentDir.exists() && !conversationLogParentDir.mkdirs()) {
                		throw new IOException("Failed to create the path: " + conversationFilePath);
                	}
                	conversationLogFile.createNewFile();
                }
                if (!conversationLogFile.exists()) {
                	throw new IOException("Impossible to create the file: " + conversationFilePath);
                }

                if (DEBUG) {
                	System.out.println(TAG + " - Application " + subscription.getApplicationName() + 
                			" successfully subscribed to ADC " + subscription.getADCID());
                }
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (DEBUG) {
                	msg = TAG + ": " + ((msg != null) ? msg : "Error subscribing");
                	System.err.println(msg);
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

    public synchronized ArrayList<Subscription> getNewSubscriptions(ArrayList<Subscription> oldSubscriptions) {
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