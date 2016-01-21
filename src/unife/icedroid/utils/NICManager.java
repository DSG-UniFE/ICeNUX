package unife.icedroid.utils;

import unife.icedroid.exceptions.WifiAdhocImpossibleToEnable;
import unife.icedroid.exceptions.WifiAdhocImpossibleToDisable;
import java.util.ArrayList;

public class NICManager {
    private final static String TAG = "NICManager";
    private final static boolean DEBUG = true;

    public static void startWifiAdhoc(Settings s) throws WifiAdhocImpossibleToEnable {
        try {
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

                //Controls to check that the interface is on ad-hoc mode and on the right essid
                if (!checkInterfaceStatus(s, "Mode:Ad-Hoc",
                        "ESSID:\"" + s.getNetworkESSID() + "\"")) {
                    throw new WifiAdhocImpossibleToEnable("Impossible to enable Wifi Ad-Hoc");
                }
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
            String cmd;
            //Turn on network-manager
        	cmd = "start network-manager";
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
}
