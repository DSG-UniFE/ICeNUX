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
    private ArrayList<String> hostChannels;
    private ArrayList<ICeDROIDMessage> cachedMessages;

    public NeighborInfo(String id,
                        String username,
                        Date time,
                        ArrayList<String> channels,
                        ArrayList<ICeDROIDMessage> messages) {
        hostID = id;
        hostUsername = username;
        lastTimeSeen = time;
        hostChannels = new ArrayList<>(channels);
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

    public synchronized ArrayList<String> getHostChannels() {
        return new ArrayList<>(hostChannels);
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

    public synchronized void setHostChannels(ArrayList<String> channels) {
        hostChannels = new ArrayList<>(channels);
    }

    public synchronized void setCachedMessages(ArrayList<ICeDROIDMessage> messages) {
        cachedMessages = new ArrayList<>(messages);
    }

    public boolean hasInCache(ICeDROIDMessage msg) {
        synchronized (cachedMessages) {
            return cachedMessages.contains(msg);
        }
    }

    public synchronized void update(NeighborInfo neighbor) {
        hostUsername = neighbor.hostUsername;
        lastTimeSeen = neighbor.lastTimeSeen;
        hostChannels = neighbor.hostChannels;
        cachedMessages = neighbor.cachedMessages;
    }

    @Override
    public synchronized boolean equals(Object object) {
        NeighborInfo nb = (NeighborInfo) object;
        return hostID.equals(nb.hostID);
    }

}
