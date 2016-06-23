package unife.icedroid.utils;

import unife.icedroid.exceptions.RoutingAlgorithmNotFound;
import unife.icedroid.services.ApplevDisseminationChannelService;
import unife.icedroid.services.HelloMessageService;
import unife.icedroid.services.BroadcastReceiveThread;
import unife.icedroid.services.BroadcastSendThread;
import unife.icedroid.services.ApplevDisseminationChannelService.OnMessageReceiveListener;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.Inet4Address;
import java.util.Enumeration;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;

public class Settings {
    private static final String TAG = "ICeDROID Settings";
    private static final boolean DEBUG = true;

    public static enum RoutingAlgorithm {SPRAY_AND_WAIT}
    public static enum CachingStrategy {FIFO, RANDOM}
    public static enum ForwardingStrategy {FIFO, PRIORITY}

    private volatile static Settings instance;

    private String networkInterface;
    private String networkESSID;
    private String networkChannel;
    private String hostID;
    private String hostIP;
    private String networkMask;
    private String networkBroadcastAddress;
    private int receivePort;
    private int messageSize;
    private RoutingAlgorithm routingAlgorithm;
    private int cacheSize;
    private CachingStrategy cachingStrategy;
    private ForwardingStrategy forwardingStrategy;
    private ApplevDisseminationChannelService ADCThread;
    private HelloMessageService HMThread;
    private BroadcastReceiveThread rcvThread;
    private BroadcastSendThread sendThread;

    private Settings() throws Exception {
    	File file = new File("resources/settings.cfg");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String[] setting;
        String line;
        String settingName;
        while ((line = br.readLine()) != null) {
            setting = line.split(" ");
            if (setting.length > 0) {
                settingName = setting[0];

                switch (settingName) {
                    case "NetworkInterface":
                        networkInterface = setting[2];
                        break;
                    case "ESSID":
                        networkESSID = setting[2];
                        break;
                    case "NetworkChannel":
                        networkChannel = setting[2];
                        break;
                    case "HostID":
                        if (!setting[2].equals("null")) {
                            hostID = setting[2];
                        } else {
                            hostID = null;
                        }
                        break;
                    case "HostIP":
                        if (!setting[2].equals("null")) {
                            hostIP = setting[2];
                        } else {
                            hostIP = null;
                        }
                        break;
                    case "NetworkMask":
						Integer netMask;
						try {
							netMask = Integer.parseInt(setting[2]);
						} catch (Exception e) {
							System.out.println("Network Mask setting invalid. Using default mask: 16");
							netMask = 16;
						}
                        networkMask = "/" + netMask;
                        break;
                    case "BroadcastAddress":
                        networkBroadcastAddress = setting[2];
                        break;
                    case "ReceivePort":
                        receivePort = Integer.parseInt(setting[2]);
                        break;
                    case "MsgSize":
                        messageSize = Integer.parseInt(setting[2]);
                        break;
                    case "RoutingAlgorithm":
                        if (setting[2].equals("SprayAndWait")) {
                            routingAlgorithm = RoutingAlgorithm.SPRAY_AND_WAIT;
                        }
                        else {
                        	throw new RoutingAlgorithmNotFound("Specified routing algorithm " +
                        			setting[2] + " not supported");
                        }
                        break;
                    case "CacheSize":
                        cacheSize = Integer.parseInt(setting[2]);
                        break;
                    case "CachingStrategy":
                        switch (setting[2]) {
                            case "FIFO":
                                cachingStrategy = CachingStrategy.FIFO;
                                break;
                            case "RANDOM":
                                cachingStrategy = CachingStrategy.RANDOM;
                                break;
                            default:
                                cachingStrategy = CachingStrategy.FIFO;
                                break;
                        }
                        break;
                    case "ForwardingStrategy":
                        switch (setting[2]) {
                            case "FIFO":
                                forwardingStrategy = ForwardingStrategy.FIFO;
                                break;
                            case "PRIORITY":
                                forwardingStrategy = ForwardingStrategy.PRIORITY;
                                break;
                            default:
                                forwardingStrategy = ForwardingStrategy.FIFO;
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        br.close();
    }

	public void buildHostID() throws SocketException, Exception {
		//Assign HostID
        if (hostID == null) {
        	NetworkInterface ni = NetworkInterface.getByName(networkInterface);
        	if (ni == null) {
    			throw new Exception("Interface " + networkInterface + " could not be retrieved");
    		}
        	byte[] mac = ni.getHardwareAddress();
        	StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));        
            }
            
            hostID = sb.toString();
        }
	}

    private void startServices(OnMessageReceiveListener listener) throws Exception {
        /**************************************/
        /** Setting up subscriptions Manager **/
        /**************************************/

        /*******************************************/
        /** Starting various services for the app **/
        /*******************************************/
        //Application-level Dissemination Channel Service
        ADCThread = new ApplevDisseminationChannelService(listener);
        ADCThread.start();
        //BroadcastSendService
        sendThread = new BroadcastSendThread();
        sendThread.start();
        if (!isServiceRunning(sendThread)) {
            throw new Exception("BroadcastSendServiceNotRunning");
        }
        //BroadcastReceiveService
        rcvThread = new BroadcastReceiveThread();
        rcvThread.start();
        if (!isServiceRunning(rcvThread)) {
            throw new Exception("BroadcastReceiveServiceNotRunning");
        }
        //HelloMessageService
        HMThread = new HelloMessageService();
        HMThread.start();
        if (!isServiceRunning(HMThread)) {
            throw new Exception("HelloMessageServiceNotRunning");
        }
    }

    private boolean isServiceRunning(Thread thread) {
        return thread.isAlive();
    }

    public static Settings getSettings(OnMessageReceiveListener listener) {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    try {
                        instance = new Settings();
                        NICManager.startWifiAdhoc(instance);
                        instance.buildHostID();
                        instance.startServices(listener);
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        if (DEBUG) {
                        	if (msg != null) {
                        		msg = TAG + ": " + msg;
                        	} else {
                        		msg = TAG + ": " + "Error loading settings!";
                        	}
                        	System.out.println(msg);
                        }
                        if (instance != null) instance.close();
                    }
                }
            }
        }
        return instance;
    }
    
    public static synchronized Settings getSettings() {
        return instance;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getNetworkESSID() {
        return networkESSID;
    }

    public String getNetworkChannel() {
        return networkChannel;
    }

    public String getHostID() {
        return hostID;
    }

    public String getHostIP() {
    	if ((hostIP == null) || hostIP.equals("")) {
    		hostIP = configureHostIP();
    		if ((hostIP == null) || hostIP.equals("")) {
    			System.err.println ("Impossible to set any IP address for the NIC. " + 
    					"Please make sure that a static IP is specified in the settings.cfg file"); 
    			System.exit(-1);
    		}
            if (DEBUG) {
            	System.out.println (TAG + ": Ip address set to " + hostIP);
            }
    	}
        return hostIP;
    }

    private String configureHostIP() {
    	String hostIP = null;
        try {
            Enumeration<InetAddress> ias = NetworkInterface.getByName(networkInterface).getInetAddresses();
            InetAddress address;
            while (ias.hasMoreElements()) {
                address = ias.nextElement();
                if (address instanceof Inet4Address) {
                    hostIP = address.getHostAddress();
                    break;
                }
            }
        } catch (SocketException sex) {
            hostIP = null;
            if (DEBUG) {
                String msg = sex.getMessage();
            	msg = TAG + ": " + ((msg != null) ? msg : 
            		"Error detected while retrieving the NIC " + networkInterface);
            	System.err.println(msg);
        	}
        }
        
        if (hostIP == null) {
            Random randGen = new Random(System.currentTimeMillis());
            int addrC, addrD;
            String address = null;

            try {
                while (hostIP == null) {
                    addrC = randGen.nextInt(254) + 1;
                    addrD = randGen.nextInt(254) + 1;
                    address = "192.168." + addrC + "." + addrD;
                    
                    if (NICManager.isIPAvailable(networkInterface, address)) {
                        hostIP = address;
                    }
                }
            } catch (Exception ex) {
            	if (DEBUG) {
	                String msg = ex.getMessage();
	            	msg = TAG + ": " + ((msg != null) ? msg :
	            		"Impossible to check the unicity of the IP address");
	            	System.err.println(msg);
            	}
            	return null;
            }
        }
        
        return hostIP;
    }

    public String getNetworkMask() {
        return networkMask;
    }

    public String getNetworkBroadcastAddress() {
        return networkBroadcastAddress;
    }

    public int getReceivePort() {
        return receivePort;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public RoutingAlgorithm getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public CachingStrategy getCachingStrategy() {
        return cachingStrategy;
    }

    public ForwardingStrategy getForwardingStrategy() {
        return forwardingStrategy;
    }

    public ApplevDisseminationChannelService getADCThread() {
        return ADCThread;
    }
    
    public HelloMessageService getHMThread() {
    	return HMThread;
    }

    public void close() {
        try {
            instance = null;

            NICManager.stopWifiAdhoc(this);

            ADCThread.interrupt();
            HMThread.interrupt();
            rcvThread.interrupt();
            sendThread.interrupt();

        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	if (msg != null) {
            		msg = TAG + ": " + msg;
            	} else {
            		msg = TAG + ": " + "close(): Closing error!";
            	}
            	System.out.println(msg);
            }
        }
    }
}