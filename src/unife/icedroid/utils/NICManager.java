package unife.icedroid.utils;

import unife.icedroid.utils.OSDetector.*;
import unife.icedroid.exceptions.*;

import java.util.ArrayList;
/*
import javax.script.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
*/

public class NICManager {
    private final static String TAG = "NICManager";
    private final static boolean DEBUG = true;
    private final static int ARP_PROBES_NUM = 3;
    private static OSDetector osDetector = OSDetector.getOSDetector();

    public static void startWifiAdhoc(Settings s) throws WifiAdhocImpossibleToEnable {
    	String cmd;
    	OS osHost = osDetector.getOSName();
        try {
        	switch (osHost) {
			case UNKNOWN:
        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
        				"mode: unknown OS");
        	case WINDOWS:
        		throw new WifiAdhocImpossibleToEnable("Ad-hoc networking mode " +
        				"not yet supported for Windows OS");
			case MAC:
				// Call Applescript interpreter
	            if (checkWirelessInterfaceStatus(s, "op mode: IBSS", "SSID: " + s.getNetworkESSID())) {
	            	System.out.println("Network interface " + s.getNetworkInterface() + 
	            			" already in ad-hoc mode and on SSID " + s.getNetworkESSID());
	            	break;
	            }
				cmd = "/usr/bin/osascript resources/enableWiFiAdHoc " + s.getNetworkESSID() + " " + s.getNetworkChannel(); 
				Utils.exec(cmd);
				
                configureNICIPAddress (s.getNetworkInterface(), s.getHostIP(),
                		s.getNetworkMask(), s.getNetworkBroadcastAddress());
                
	            //Check if the NIC is now in ad-hoc mode and on the right ESSID
                /*
                 * The following check always returns an empty string on MAC OS X 10.11.5
                 * Assuming things went well!
	            if (!checkWirelessInterfaceStatus(s, "op mode: IBSS", "SSID: "+ s.getNetworkESSID())) {
	                throw new WifiAdhocImpossibleToEnable("Impossible to enable WiFi Ad-Hoc mode");
	            }
	            */
	            
				break;
				/*
				InputStream is = NICManager.class.getClassLoader().getResourceAsStream("enableWiFiAdHoc");
		        BufferedReader br = new BufferedReader(new InputStreamReader(is));
				ScriptEngineManager mgr = new ScriptEngineManager();
		        ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
		        ScriptContext context = engine.getContext();

				Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
				bindings.put("javax_script_function", "run");
				bindings.put(ScriptEngine.ARGV, s.getNetworkESSID());

		        engine.eval(br, context);
        		break;
        		*/
        	case LINUX:
    			//Check if the NIC is already in ad-hoc mode
	            if (checkWirelessInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
	            	System.out.println("Network interface " + s.getNetworkInterface() + 
	            			" already in ad-hoc mode");
	            	break;
	            }
        		switch (osDetector.getLinuxDistribution()) {
				case ERROR:
	        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
	        				"mode: an error occurred retrieving the Linux distribution");
        		case UNKNOWN:
        			System.err.println("Unknown Linux distribution; assuming a systemd-compliant system");
        		case RED_HAT:
	            	//Turn off network-manager
	            	cmd = "systemctl disable NetworkManager.service";
	            	try {
	            		Utils.rootExec(cmd);
	            	} catch (CommandImpossibleToRun citr) {}
	                
	            	cmd = "systemctl stop NetworkManager.service";
	                Utils.rootExec(cmd);
	                
	                //Pull down Wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " down";
	                Utils.rootExec(cmd);
	
	                //Set wifi ad-hoc mode
	                cmd = "iwconfig " + s.getNetworkInterface() + " mode ad-hoc channel " +
	                        s.getNetworkChannel() + " essid " + s.getNetworkESSID();
	                Utils.rootExec(cmd);
	                
	                //Set IP address and network settings
	                configureNICIPAddress (s.getNetworkInterface(), s.getHostIP(),
	                		s.getNetworkMask(), s.getNetworkBroadcastAddress());
	
	                //Pull up wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " up";
	                Utils.rootExec(cmd);
	                
	                break;
        		case UBUNTU:
	            	//Turn off network-manager
	            	cmd = "stop network-manager";
	                Utils.rootExec(cmd);
	                
	                //Pull down Wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " down";
	                Utils.rootExec(cmd);
	
	                //Set wifi ad-hoc mode
	                cmd = "iwconfig " + s.getNetworkInterface() + " mode ad-hoc channel " +
	                        s.getNetworkChannel() + " essid " + s.getNetworkESSID();
	                Utils.rootExec(cmd);
	                
	                //Set IP address and network settings
	                configureNICIPAddress (s.getNetworkInterface(), s.getHostIP(),
	                		s.getNetworkMask(), s.getNetworkBroadcastAddress());
	
	                //Pull up wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " up";
	                Utils.rootExec(cmd);
	                
	                break;
        		}
        	
	            //Check if the NIC is now in ad-hoc mode and on the right ESSID
	            if (!checkWirelessInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
	                throw new WifiAdhocImpossibleToEnable("Impossible to enable WiFi Ad-Hoc mode");
	            }
        	}
        } catch(Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ?  msg :
            		"startWifiAdhoc(): Impossible to enable Wifi Ad-Hoc");
            	System.err.println(msg);
            }
                                        
            throw new WifiAdhocImpossibleToEnable("Impossible to enable Wifi Ad-Hoc");
        }
    }

	private static void configureNICIPAddress(String networkInterface, String hostIP,
			String networkMask, String networkBroadcastAddress)
					throws UnsupportedOSException, CommandImpossibleToRun {
		String cmd = null;
		OS osHost = osDetector.getOSName();
    	switch (osHost) {
		case LINUX:
            cmd = "ip addr add " + hostIP + networkMask + " broadcast " +
            		networkBroadcastAddress + " dev " + networkInterface;
			break;
		case MAC:
            cmd = "ifconfig " + networkInterface + " inet " + hostIP +
            		networkMask + " broadcast " + networkBroadcastAddress;
			break;
		case UNKNOWN:
		case WINDOWS:
			throw new UnsupportedOSException("Impossible to configure a static IP " +
					"address under " + osHost + " OS");        	
    	}
		try {
            //Set IP address and network settings
            Utils.rootExec(cmd);
            } catch (Exception ex) {
                String msg = ex.getMessage();
            	msg = TAG + ": " + ((msg != null) ?  msg :
            		"Impossible to set an address for the NIC " + networkInterface);
            	System.err.println(msg);
                
                throw new CommandImpossibleToRun("Error running the following command: " + cmd);
        }
		
	}

	public static void stopWifiAdhoc(Settings s) throws WifiAdhocImpossibleToDisable {
        try {
            String cmd;
        	OS osHost = osDetector.getOSName();
        	
            switch (osHost) {
			case MAC:
				// Bring down and up the NIC, to restore the system-default network settings
            	cmd = "ifconfig " + s.getNetworkInterface() + " down";
                Utils.rootExec(cmd);

            	cmd = "ifconfig " + s.getNetworkInterface() + " up";
                Utils.rootExec(cmd);
                
        		break;
        	case LINUX:
        		switch (osDetector.getLinuxDistribution()) {
				case ERROR:
	        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
	        				"mode: an error occurred retrieving the Linux distribution");
        		case UNKNOWN:
        			System.err.println("Unknown Linux distribution; assuming a systemd-compliant system");
        		case RED_HAT:
	            	// Re-enable network-manager
                	cmd = "systemctl enable NetworkManager.service";
                	try {
                		Utils.rootExec(cmd);
                	} catch (CommandImpossibleToRun citr) {}
                	
	            	// Turn on again the NetworkManager
                	cmd = "systemctl start NetworkManager.service";
                    Utils.rootExec(cmd);
                    
	                break;
        		case UBUNTU:
                    //Turn on again the network-manager
                	cmd = "start network-manager";
                    Utils.rootExec(cmd);
	                
	                break;
        		}
        		break;
        	case WINDOWS:
        		throw new WifiAdhocImpossibleToEnable("Ad-hoc networking mode " +
        				"not yet supported for Windows OS");
			case UNKNOWN:
        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
        				"mode: unknown OS");
        	}
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ?  msg :
            		"stopWifiAdhoc(): Impossible to disable Wifi Ad-Hoc");
            	System.out.println(msg);
            }
            throw new WifiAdhocImpossibleToDisable("Impossible to disable Wifi Ad-Hoc");
        }
    }

    private static boolean checkWirelessInterfaceStatus(Settings s, String... fields)
    		throws UnsupportedOSException {
        String cmd;
    	OS osHost = osDetector.getOSName();
    	
        switch (osHost) {
		case MAC:
	        cmd = "airport -I " + s.getNetworkInterface();
	        try {
	            ArrayList<String> results = Utils.exec(cmd);
	            for (String f : fields) {
	                if (!Utils.containsSubstring(results, f)) {
	                    return false;
	                }
	            }
	        } catch (Exception ex) {
	        	String msg = ex.getMessage();
	            if (DEBUG) {
	            	msg = TAG + ": " + ((msg != null) ?  msg :
	            		"stopWifiAdhoc(): Impossible to disable Wifi Ad-Hoc");
	            	System.out.println(msg);
	            }
	        }
	        return true;
		case LINUX:
	        cmd = "iwconfig " + s.getNetworkInterface();
	        try {
	            ArrayList<String> results = Utils.exec(cmd);
	            for (String f : fields) {
	                if (!Utils.containsSubstring(results, f)) {
	                    return false;
	                }
	            }
	        } catch (Exception ex) {
	        	String msg = ex.getMessage();
	            if (DEBUG) {
	            	msg = TAG + ": " + ((msg != null) ?  msg :
	            		"stopWifiAdhoc(): Impossible to disable Wifi Ad-Hoc");
	            	System.out.println(msg);
	            }
	        }
	        return true;
    	case WINDOWS:
    		throw new UnsupportedOSException("Impossible to check wireless status: " +
    				"Windows OS not yet supported");
		case UNKNOWN:
    		throw new UnsupportedOSException("Impossible to check wireless status for unknown OS");
        }
        
        return false;
    }

	public static boolean isIPAvailable(String networkInterface, String address)
			throws UnsupportedOSException, CommandImpossibleToRun {
		ArrayList<String> commandResults = null;
        String cmd = "arping -I " + networkInterface + " -D -c " + ARP_PROBES_NUM + " " + address;
        try {
        	commandResults = Utils.rootExec(cmd);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	msg = TAG + ": " + ((msg != null) ?  msg :
            		"isIPAvailable(): Impossible to get results from the arping command");
            	System.out.println(msg);
            }
            throw new CommandImpossibleToRun("Failed execution of the arping command");
	    }
        
    	String arpingRes = commandResults.remove(commandResults.size() - 1).toLowerCase();
    	OS osHost = osDetector.getOSName();
    	switch (osHost) {
		case LINUX:
			if (arpingRes.contains("received 0")) {
				return true;
			}
			break;
		case MAC:
			if (arpingRes.contains("100% packet loss")) {
				return true;
			}
			break;
		case UNKNOWN:
		case WINDOWS:
			throw new UnsupportedOSException("arping not supported for " + osHost + " OS");
    	}

		return false;
	}
}
