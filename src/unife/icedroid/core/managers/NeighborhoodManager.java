package unife.icedroid.core.managers;

import unife.icedroid.core.NeighborInfo;
import unife.icedroid.core.BaseMessage;
import unife.icedroid.core.ICeDROIDMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NeighborhoodManager {
    private static final String TAG = "NeighborhoodManager";
    private static final boolean DEBUG = true;

    private volatile static NeighborhoodManager instance;
    private static final long ttlOfNeighbor = 60*1000;

    private ArrayList<NeighborInfo> neighborsList;
    private Timer neighborhoodManagerTimer;
    private long lastUpdate;


    private NeighborhoodManager() {
        neighborsList = new ArrayList<>(0);
        neighborhoodManagerTimer = new Timer();
    }

    public static NeighborhoodManager getNeighborhoodManager() {
        if (instance == null) {
            synchronized (NeighborhoodManager.class) {
                if (instance == null) {
                    instance = new NeighborhoodManager();
                }
            }
        }
        return instance;
    }

    public ArrayList<String> add(NeighborInfo neighbor) {
        synchronized (neighborsList) {
            Date lastTimeSeen = new Date(System.currentTimeMillis());
            neighbor.setLastTimeSeen(lastTimeSeen);

            NeighborInfo ni = isNeighborPresent(neighbor);

            NeighborRemoveTask task;
            ArrayList<String> newChannels = null;
            if (ni != null) {
                newChannels = new ArrayList<>(0);
                for (String c : neighbor.getHostChannels()) {
                    if (!ni.getHostChannels().contains(c)) {
                        newChannels.add(c);
                    }
                }
                ni.update(neighbor);
                task = new NeighborRemoveTask(this, ni);
            } else {
                neighborsList.add(neighbor);
                task = new NeighborRemoveTask(this, neighbor);
            }

            Date expirationTime = new Date(lastTimeSeen.getTime() + ttlOfNeighbor);
            neighborhoodManagerTimer.schedule(task, expirationTime);

            lastUpdate = lastTimeSeen.getTime();
            //Notify that an update was done
            neighborsList.notifyAll();

            return newChannels;
        }
    }

    public synchronized void remove(NeighborInfo neighbor) {
        if (System.currentTimeMillis() > neighbor.getLastTimeSeen().getTime() + ttlOfNeighbor) {
            if (DEBUG) System.out.println(TAG + " Deleting neighbor...");
            neighborsList.remove(neighbor);
        }
    }

    public NeighborInfo getNeighborByID(String ID) {
        synchronized (neighborsList) {
            for (NeighborInfo n : neighborsList) {
                if (n.getHostID().equals(ID)) {
                    return n;
                }
            }
            return null;
        }
    }

    public synchronized int getNumberOfNeighbors() {
        return neighborsList.size();
    }

    public synchronized boolean isThereNeighborNotInterestedToMessageAndNotCached(
                                                                            ICeDROIDMessage msg) {
        String channel = msg.getChannel();
        for (NeighborInfo neighbor : neighborsList) {
            if (!neighbor.getHostChannels().contains(channel) &&
                !neighbor.getCachedMessages().contains(msg)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isThereNeighborSubscribedToChannel(ICeDROIDMessage msg) {
        String channel = msg.getChannel();
        for (NeighborInfo neighbor : neighborsList) {
            if (neighbor.getHostChannels().contains(channel) &&
                    !neighbor.getCachedMessages().contains(msg)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isThereNeighborWithoutThisMessage(ICeDROIDMessage msg) {
        String channel = msg.getChannel();
        //Is there a neighbor that isn't interested to this message (doesn't belong
        //to the same message channel) and hasn't the message in its own cache?
        for (NeighborInfo neighbor : neighborsList) {
            if (!neighbor.getHostChannels().contains(channel) &&
                    !neighbor.getCachedMessages().contains(msg)) {
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<NeighborInfo> whoHasThisMessageButNotInterested(
                                                                            ICeDROIDMessage msg) {
        ArrayList<NeighborInfo> neighbors = new ArrayList<>(0);
        String channel = msg.getChannel();
        for (NeighborInfo neighbor : neighborsList) {
            if (!neighbor.getHostChannels().contains(channel)) {
                if (neighbor.getCachedMessages().contains(msg)) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    public boolean everyoneHasThisMessage(BaseMessage msg) {
        synchronized (neighborsList) {
            for (NeighborInfo n : neighborsList) {
                ArrayList<ICeDROIDMessage> cms = n.getCachedMessages();
                if (!cms.contains(msg)) {
                    return false;
                }
            }
            return true;
        }
    }

    public long isThereAnUpdate(long time) {
        synchronized (neighborsList) {
            try {
                while (time == lastUpdate) {
                    neighborsList.wait();
                }
            } catch (Exception ex) {}
            return lastUpdate;
        }
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    private NeighborInfo isNeighborPresent(NeighborInfo neighbor) {
        for (NeighborInfo n : neighborsList) {
            if (neighbor.equals(n)) {
                return n;
            }
        }
        return null;
    }

    private class NeighborRemoveTask extends TimerTask {

        private NeighborhoodManager neighborhoodManager;
        private NeighborInfo neighbor;

        public NeighborRemoveTask(NeighborhoodManager neighborhoodManager,
                                  NeighborInfo neighbor) {
            this.neighborhoodManager = neighborhoodManager;
            this.neighbor = neighbor;
        }

        @Override
        public void run() {
            neighborhoodManager.remove(neighbor);
        }

    }

}