package unife.icedroid.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class NeighborInfo implements Serializable {
    public static final String EXTRA_NEIGHBOR = "unife.icedroid.NEIGHBOR";
    public static final String EXTRA_NEW_NEIGHBOR = "unife.icedroid.NEW_NEIGHBOR";
    public static final String EXTRA_NEIGHBOR_UPDATE = "unife.icedroid.NEIGHBOR_UPDATE";
    public static final String EXTRA_NEW_CHANNELS = "unife.icedroid.NEW_CHANNELS";

    private String hostID;
    private String hostUsername;
    private Date lastTimeSeen;
    private ArrayList<String> hostSubscribedADCs;
    private ArrayList<ICeDROIDMessage> cachedMessages;

    public NeighborInfo(String id, String username, Date time, ArrayList<String> subscribedADCs,
                        ArrayList<ICeDROIDMessage> messages) {
        hostID = id;
        hostUsername = username;
        lastTimeSeen = time;
        hostSubscribedADCs = new ArrayList<>(subscribedADCs);
        cachedMessages = new ArrayList<>(messages);
    }

    public synchronized String getHostID() {
        return hostID;
    }

    public synchronized String getHostUsername() {
        return hostUsername;
    }

    public synchronized Date getLastTimeSeen() {
        return lastTimeSeen;
    }

    public synchronized ArrayList<String> getNeighborSubscribedADCs() {
        return new ArrayList<>(hostSubscribedADCs);
    }

    public synchronized ArrayList<ICeDROIDMessage> getCachedMessages() {
        return new ArrayList<>(cachedMessages);
    }

    public synchronized void setHostID(String id) {
        hostID = id;
    }

    public synchronized void setHostUsername(String username) {
        hostUsername = username;
    }

    public synchronized void setLastTimeSeen(Date time) {
        lastTimeSeen = time;
    }

    public boolean hasInCache(ICeDROIDMessage msg) {
        synchronized (cachedMessages) {
            return cachedMessages.contains(msg);
        }
    }

    public boolean addToCache(ICeDROIDMessage msg) {
        synchronized (cachedMessages) {
            return cachedMessages.add(msg);
        }
    }

    public synchronized void update(NeighborInfo neighbor) {
        hostUsername = neighbor.hostUsername;
        lastTimeSeen = neighbor.lastTimeSeen;
        hostSubscribedADCs = neighbor.hostSubscribedADCs;
        cachedMessages = neighbor.cachedMessages;
    }

    @Override
    public synchronized boolean equals(Object object) {
    	if (!(object instanceof NeighborInfo)) {
    		return false;
    	}
    	
        return hostID.equals(((NeighborInfo) object).hostID);
    }

}
