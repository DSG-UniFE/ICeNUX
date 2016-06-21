package unife.icedroid.core;

import java.util.HashMap;


public class ICeDROIDMessage extends BaseMessage {
	private static final long serialVersionUID = 1;
    public static final String ICEDROID_MESSAGE = "ICeDROIDMessage";
    public final static String EXTRA_ICEDROID_MESSAGE = "unife.icedroid.ICEDROID_MESSAGE";

    private String channel;
    private HashMap<String, Integer> properties;

    public ICeDROIDMessage(String channel) {
        super();
        typeOfMessage = ICEDROID_MESSAGE;
        ttl = INFINITE_TTL;
        priority = NO_PRIORITY_LEVEL;
        this.channel = channel;
        properties = new HashMap<>(0);
        setSize();
    }

    public ICeDROIDMessage(ICeDROIDMessage msg) {
        super(msg);
        channel = msg.channel;
        properties = msg.properties;
    }

    public String getChannel() {
        return channel;
    }

    public Integer getProperty(String key) {
        return properties.get(key);
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setProperty(String key, Integer value) {
        properties.put(key, value);
    }

    @Override
    public ICeDROIDMessage clone() {
        ICeDROIDMessage iCeDROIDMessage = null;
        try {
            iCeDROIDMessage = (ICeDROIDMessage) super.clone();
            iCeDROIDMessage.properties = (HashMap) properties.clone();
        } catch (Exception ex) {}
        return iCeDROIDMessage;
    }
}
