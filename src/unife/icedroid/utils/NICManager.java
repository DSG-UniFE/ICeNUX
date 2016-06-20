package unife.icedroid.utils;

import unife.icedroid.utils.OSDetector.*;
import unife.icedroid.exceptions.*;

import java.util.ArrayList;

public class NICManager {
    
    
    private final static String TAG = "NICManager";
    private final static boolean DEBUG = true;
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
        		break;
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
	                s.configureHostIP();
	
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
	                s.configureHostIP();
	
	                //Pull up wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " up";
	                Utils.rootExec(cmd);
	                
	                break;
        		}
        	
	            //Check if the NIC is now in ad-hoc mode and on the right ESSID
	            if (!checkWirelessInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
	                throw new WifiAdhocImpossibleToEnable("Impossible to enable Wifi Ad-Hoc");
	            }
        	}
        } catch(Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	if (msg != null) {
            		msg = TAG + ": " + msg;
            	} else {
            		msg = TAG + ": " + "startWifiAdhoc(): Impossible to enable Wifi Ad-Hoc";
            	}
            	System.out.println(msg);
            }
                                        
            throw new WifiAdhocImpossibleToEnable("Impossible to enable Wifi Ad-Hoc");
        }
    }

	public static void stopWifiAdhoc(Settings s) throws WifiAdhocImpossibleToDisable {
        try {
            String cmd;
        	OS osHost = osDetector.getOSName();
        	
            switch (osHost) {
			case UNKNOWN:
        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
        				"mode: unknown OS");
        	case WINDOWS:
        		throw new WifiAdhocImpossibleToEnable("Ad-hoc networking mode " +
        				"not yet supported for Windows OS");
			case MAC:
        		break;
        	case LINUX:
        		switch (osDetector.getLinuxDistribution()) {
				case ERROR:
	        		throw new WifiAdhocImpossibleToEnable("Impossible to set up ad-hoc networking " +
	        				"mode: an error occurred retrieving the Linux distribution");
        		case UNKNOWN:
        			System.err.println("Unknown Linux distribution; assuming a systemd-compliant system");
        		case RED_HAT:
	            	// Enable network-manager
                	cmd = "systemctl enable NetworkManager.service";
                	try {
                		Utils.rootExec(cmd);
                	} catch (CommandImpossibleToRun citr) {}
                	
	            	// Turn on network-manager
                	cmd = "systemctl start NetworkManager.service";
                    Utils.rootExec(cmd);
                    
	                break;
        		case UBUNTU:
                    //Turn on network-manager
                	cmd = "start network-manager";
                    Utils.rootExec(cmd);
	                
	                break;
        		}
        	}
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (DEBUG) {
            	if (msg != null) {
            		msg = TAG + ": " + msg;
            	} else {
            		msg = TAG + ": " + "stopWifiAdhoc(): Impossible to disable Wifi Ad-Hoc";
            	}
            	System.out.println(msg);
            }
            throw new WifiAdhocImpossibleToDisable("Impossible to disable Wifi Ad-Hoc");
        }
    }

    private static boolean checkWirelessInterfaceStatus(Settings s, String... fields) {
        String cmd;
        cmd = "iwconfig " + s.getNetworkInterface();
        try {
            ArrayList<String> results = Utils.exec(cmd);
            for (String f : fields) {
                if (!containsSubstring(results, f)) {
                    return false;
                }
            }
        } catch (Exception ex) {}
        return true;
    }

    private static boolean containsSubstring (ArrayList<String> results, String substring) {
        for (String line : results) {
            if (line.contains(substring)) {
                return true;
            }
        }
        return false;
    }
}
