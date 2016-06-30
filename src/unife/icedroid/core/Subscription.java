package unife.icedroid.core;

import java.io.Serializable;

public class Subscription implements Serializable {
    public final static String EXTRA_SUBSCRIPTION = "unife.icedroid.SUBSCRIPTION";

    private String adcID;
    private String appID;

    public Subscription(String adcIdentifier, String applicationID) {
        adcID = adcIdentifier;
        appID = applicationID;
    }

    public String getADCID() {
        return adcID;
    }

    public String getApplicationName() {
        return appID;
    }

    @Override
    public boolean equals(Object object) {
    	if (!(object instanceof Subscription)) {
    		return false;
    	}
    	
        Subscription subscription = (Subscription) object;
        return (adcID.equals(subscription.adcID) &&
                appID.equals(subscription.appID));
    }

    @Override
    public String toString() {
        return adcID + ":" + appID;
    }
    
    public String getSubscriptionFileName() {
        return adcID + "." + appID;
    }

}
