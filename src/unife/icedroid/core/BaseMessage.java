package unife.icedroid.core;

import unife.icedroid.utils.Settings;


import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public abstract class BaseMessage implements Serializable, Cloneable {
	private static final long serialVersionUID = 1;
    private static final String TAG = "BaseMessage";
    private static final boolean DEBUG = true;

    public static final int INFINITE_TTL = -1;
    public static final int NO_PRIORITY_LEVEL = 0;
    public static final int MAX_PRIORITY_LEVEL = Integer.MAX_VALUE;

    private static int counterOfmsgID;

    protected String typeOfMessage;
    protected String hostID;
    protected String hostUsername;
    protected int msgID;
    protected Date creationTime;
    protected Date receptionTime;
    protected long ttl;
    protected int priority;
    protected int size;

    public BaseMessage() {
        hostID = Settings.getSettings().getHostID();
        setMsgID();
        long time = System.currentTimeMillis();
        creationTime = new Date(time);
        receptionTime = new Date(time);
    }

    public BaseMessage(BaseMessage msg) {
        typeOfMessage = msg.typeOfMessage;
        hostID = msg.hostID;
        hostUsername = msg.hostUsername;
        msgID = msg.msgID;
        creationTime = msg.creationTime;
        receptionTime = msg.receptionTime;
        ttl = msg.ttl;
        priority = msg.priority;
        size = msg.size;

    }

    public String getTypeOfMessage() {
        return typeOfMessage;
    }

    public String getHostID() {
        return hostID;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public int getMsgID() {
        return msgID;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Date getReceptionTime() {
        return receptionTime;
    }

    public long getTtl() {
        return ttl;
    }

    public int getPriority() {
        return priority;
    }

    public int getSize() {
        return size;
    }

    public void setTypeOfMessage(String type) {
        typeOfMessage = type;
    }

    public void setHostID(String id) {
        hostID = id;
    }

    public void setHostUsername(String username) {
        hostUsername = username;
    }

    public void setMsgID() {
        msgID = counterOfmsgID;
        incrementCounterOfmsgID();
    }

    public void setCreationTime(Date time) {
        creationTime = time;
    }

    public void setReceptionTime(Date time) {
        receptionTime = time;
    }

    public void setTtl(long period) {
        ttl = period;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setSize() {
        this.size = calculateSize();
    }

    @Override
    public boolean equals(Object object) {
        BaseMessage msg = (BaseMessage) object;
        return (typeOfMessage.equals(msg.typeOfMessage) &&
                hostID.equals(msg.hostID) &&
                (msgID == msg.msgID));
    }

    public void resetCounterOfmsgID() {
        counterOfmsgID = 0;
    }

    private void incrementCounterOfmsgID() {
        if(counterOfmsgID == Integer.MAX_VALUE) {
            resetCounterOfmsgID();
        } else {
            counterOfmsgID++;
        }
    }

    @Override
    public String toString() {
        return typeOfMessage + ": " + hostID + ", MsgID: " + msgID +
                " ReceptionTime: " + receptionTime;
    }

    private int calculateSize() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + " - " + ((msg != null) ? msg : "Error getting the message size");
            	System.err.println(msg);
            }
        }
        return byteArrayOutputStream.toByteArray().length;
    }

    @Override
    public BaseMessage clone() {
        BaseMessage baseMessage = null;
        try {
            baseMessage = (BaseMessage) super.clone();
            baseMessage.setCreationTime(new Date(creationTime.getTime()));
            baseMessage.setReceptionTime(new Date(receptionTime.getTime()));
        } catch (Exception ex) {}
        return baseMessage;
    }
}
