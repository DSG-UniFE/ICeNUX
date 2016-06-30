package unife.icedroid.core;

import unife.icedroid.core.OnMessageReceiveListener;
import unife.icedroid.core.managers.ChannelListManager;
import unife.icedroid.core.routingalgorithms.RoutingAlgorithm;
import unife.icedroid.core.routingalgorithms.RoutingAlgorithmFactory;
import unife.icedroid.utils.Settings;

public class ICeDROID {
    private static ICeDROID instance = null;

    private ChannelListManager channelListManager;
    private RoutingAlgorithm routingAlgorithm;
    private Settings icedroidSettings;

    private ICeDROID(OnMessageReceiveListener listener) throws Exception {
        if ((icedroidSettings = Settings.getSettings(listener)) != null) {
            channelListManager = ChannelListManager.getChannelListManager();
        } else {
            throw new Exception("Impossible to retrieve instance of the Settings class");
        }
    }

    public static ICeDROID getInstance(OnMessageReceiveListener listener) {
        if (instance == null) {
            synchronized (ICeDROID.class) {
                if (instance == null) {
                    try {
                        instance = new ICeDROID(listener);
                    } catch (Exception ex) {
                        instance = null;
                    }
                }
            }
        }
        return instance;
    }
    
    public static synchronized ICeDROID getInstance() {
    	return instance;
    }

    public void subscribe(String adcID) {
        channelListManager.subscribe(adcID);
    }

    public void send(ICeDROIDMessage message) {
    	if (routingAlgorithm == null) {
    		routingAlgorithm = RoutingAlgorithmFactory.makeRoutingAlgorithm(
    				icedroidSettings.getRoutingAlgorithm());
        	((Thread) routingAlgorithm).start();
    	}
    	boolean added = false;
        while (!added) {
            added = routingAlgorithm.addMessageForTransmission(message);
            if (!added) {
            	routingAlgorithm = RoutingAlgorithmFactory.makeRoutingAlgorithm(
            			icedroidSettings.getRoutingAlgorithm());
            	((Thread) routingAlgorithm).start();
            }
        }
    }

    public void close() {
    	if (routingAlgorithm != null) ((Thread) routingAlgorithm).interrupt();
    	Settings.getSettings().close();
    }
}
