package unife.icedroid;

import java.io.Serializable;

public class Subscription implements Serializable {
    public final static String EXTRA_SUBSCRIPTION = "unife.icedroid.SUBSCRIPTION";

    private String channelID;
    private String groupName;

    public Subscription(String channel, String group) {
        channelID = channel;
        groupName = group;
    }

    public String getADCID() {
        return channelID;
    }

    public String getApplicationName() {
        return groupName;
    }

    @Override
    public boolean equals(Object object) {
    	if (!(object instanceof Subscription)) {
    		return false;
    	}
        Subscription subscription = (Subscription) object;
        return (channelID.equals(subscription.channelID) &&
                groupName.equals(subscription.groupName));
    }

    @Override
    public String toString() {
        return channelID + ":" + groupName;
    }
    
    public String getSubscriptionFileName() {
        return channelID + "." + groupName;
    }

}
