package unife.icedroid.utils;

import unife.icedroid.exceptions.WifiAdhocImpossibleToEnable;
import unife.icedroid.exceptions.WifiAdhocImpossibleToDisable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import sun.misc.PerformanceLogger;

public class NICManager {
    
    private static enum OSDistribution {UNKNOWN, UBUNTU, RED_HAT}
    
    private final static String TAG = "NICManager";
    private final static boolean DEBUG = true;
    private static OSDistribution osDistro = OSDistribution.UNKNOWN;

    public static void startWifiAdhoc(Settings s) throws WifiAdhocImpossibleToEnable {
        try {
        	if (osDistro == OSDistribution.UNKNOWN) {
        		detectOSDistribution();
            	if (osDistro == OSDistribution.UNKNOWN) {
            		throw new WifiAdhocImpossibleToEnable("Impossible to detect OS Distribution name");
            	}
        	}
        	else if (osDistro == OSDistribution.UBUNTU) {
	            //Check if the interface is already in adhoc state
	            if (!checkInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
	                
	            	String cmd;
	            	
	            	//Turn off network-manager
	            	cmd = "service network-manager stop";
	                Utils.rootExec(cmd);
	                
	                //Pull down Wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " down";
	                Utils.rootExec(cmd);
	
	                //Set wifi ad-hoc mode
	                cmd = "iwconfig " + s.getNetworkInterface() + " mode ad-hoc channel " +
	                        s.getNetworkChannel() + " essid " + s.getNetworkESSID();
	                Utils.rootExec(cmd);
	
	                //Pull up wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " up";
	                Utils.rootExec(cmd);
	
	                //Set IP address and network settings
	                s.getHostIP();
	            }
        	}
        	else if (osDistro == OSDistribution.RED_HAT) {
	            //Check if the interface is already in adhoc state
	            if (!checkInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
	                
	            	String cmd;
	            	
	            	//Turn off network-manager
	            	cmd = "systemctl stop network.service";
	                Utils.rootExec(cmd);
	                
	                //Pull down Wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " down";
	                Utils.rootExec(cmd);
	
	                //Set wifi ad-hoc mode
	                cmd = "iwconfig " + s.getNetworkInterface() + " mode ad-hoc channel " +
	                        s.getNetworkChannel() + " essid " + s.getNetworkESSID();
	                Utils.rootExec(cmd);
	
	                //Pull up wifi interface
	                cmd = "ip link set " + s.getNetworkInterface() + " up";
	                Utils.rootExec(cmd);
	
	                //Set IP address and network settings
	                s.getHostIP();
	            }
        	}
        	
            //Check that the interface is in ad-hoc mode and on the right ESSID
            if (!checkInterfaceStatus(s, "Mode:Ad-Hoc", "ESSID:\"" + s.getNetworkESSID() + "\"")) {
                throw new WifiAdhocImpossibleToEnable("Impossible to enable Wifi Ad-Hoc");
            }
        } catch(Exception ex){
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
            String cmd = "";
            //Turn on network-manager
            if (osDistro == OSDistribution.UBUNTU) {
            	cmd = "start network-manager";
            }
            else if (osDistro == OSDistribution.RED_HAT) {
            	cmd = "systemctl start network.service";
            }
            Utils.rootExec(cmd);
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

    private static boolean checkInterfaceStatus(Settings s, String... fields) {
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
    
    private static void detectOSDistribution() throws FileNotFoundException {
    	BufferedReader myReader = new BufferedReader(new FileReader("/etc/os-release"));
        String strLine = null;
        try {
			while ((strLine = myReader.readLine()) != null) {
				strLine = strLine.toLowerCase();
			    if (strLine.startsWith("id=")) {
			    	if (strLine.contains("ubuntu")) {
			    		osDistro = OSDistribution.UBUNTU;
			    	}
			    	else if (strLine.contains("fedora") || strLine.contains("rhel")) {
			    		osDistro = OSDistribution.RED_HAT;
			    	}
			    	else {
			    		System.err.println("Failed to detect OS distribution");
			    		System.exit(-1);
			    	}
			    	break;
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
