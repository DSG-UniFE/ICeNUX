package unife.icedroid.core;

import java.util.HashMap;


public class ICeDROIDMessage extends BaseMessage {
	private static final long serialVersionUID = 1;
    private static final String TAG = "ICeDROIDMessage";
    public static final String ICEDROID_MESSAGE = "ICeDROIDMessage";
    public static final String EXTRA_ICEDROID_MESSAGE = "unife.icedroid.ICEDROID_MESSAGE";

    private String adcID;
    private HashMap<String, Integer> properties;

    public ICeDROIDMessage(String channel) {
        super();
        
        typeOfMessage = ICEDROID_MESSAGE;
        ttl = INFINITE_TTL;
        priority = NO_PRIORITY_LEVEL;
        
        this.adcID = channel;
        properties = new HashMap<>(0);
        setSize();
    }

    public ICeDROIDMessage(ICeDROIDMessage msg) {
        super(msg);
        
        adcID = msg.adcID;
        properties = msg.properties;
    }

    public String getADCID() {
        return adcID;
    }

    public Integer getProperty(String key) {
        return properties.get(key);
    }

    public void setADCID(String channel) {
        this.adcID = channel;
    }

    public void setProperty(String key, Integer value) {
        properties.put(key, value);
    }

    @Override
    public ICeDROIDMessage clone() {
        ICeDROIDMessage iCeDROIDMessage = null;
        try {
            iCeDROIDMessage = (ICeDROIDMessage) super.clone();
            iCeDROIDMessage.properties = (HashMap<String, Integer>) properties.clone();
        } catch (Exception ex) {
        	System.err.println(TAG + " - " + ex + ": " + ex.getMessage());
        }
        return iCeDROIDMessage;
    }
}
