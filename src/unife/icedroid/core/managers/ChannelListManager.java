package unife.icedroid.core.managers;

import unife.icedroid.core.ICeDROIDMessage;
import unife.icedroid.utils.Settings;
import unife.icedroid.core.Intent;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ChannelListManager {
    private static final String TAG = "ChannelListManager";
    private static final boolean DEBUG = true;
    public static final String EXTRA_NEW_CHANNEL = "unife.icedroid.NEW_CHANNEL";

    private static final String channelsFileName = "resources/channels";
    private volatile static ChannelListManager instance = null;

    private ArrayList<String> channelList;


    private ChannelListManager() {
        channelList = new ArrayList<>(0);
        try {
            BufferedReader br = new BufferedReader(new FileReader(channelsFileName));

            String channel;
            while ((channel = br.readLine()) != null) {
                channelList.add(channel);
            }
            br.close();
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	if (msg != null) {
            		msg = TAG + ": " + msg;
            	} else {
            		msg = TAG + ": " + "Error loading channel list";
            	}
            	System.out.println(msg);
            }
        }
    }

    public static ChannelListManager getChannelListManager() {
        if (instance == null) {
            synchronized (ChannelListManager.class) {
                if (instance == null) {
                    instance = new ChannelListManager();
                }
            }
        }
        return instance;
    }

    public synchronized void subscribe(String channel) {
        if (!channelList.contains(channel)) {
            channelList.add(channel);
            try {
                FileOutputStream fos = new FileOutputStream(channelsFileName, true);
                fos.write((channel + "\n").getBytes());
                fos.close();
                if (DEBUG) System.out.println(TAG + " Attachment to channel: " + channel);

                Intent intent = new Intent();
                intent.putExtra(EXTRA_NEW_CHANNEL, true);
                Settings.getSettings().getADCThread().add(intent);

            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (DEBUG) {
                	if (msg != null) {
                		msg = TAG + ": " + msg;
                	} else {
                		msg = TAG + ": " + "Error sticking to channel";
                	}
                	System.out.println(msg);
                }
            }
        }
    }

    public synchronized ArrayList<String> getChannelList() {
        return new ArrayList<>(channelList);
    }

    public synchronized boolean isSubscribedToChannel(ICeDROIDMessage msg) {
        for (String c : channelList) {
            if (c.equals(msg.getChannel())) {
                return true;
            }
        }
        return false;
    }

}
