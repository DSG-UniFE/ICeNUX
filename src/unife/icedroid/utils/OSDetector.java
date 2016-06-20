package unife.icedroid.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OSDetector {
	
    public enum OS {UNKNOWN, LINUX, MAC, WINDOWS}
    public enum LINUX_DISTRIBUTION {ERROR, UNKNOWN, UBUNTU, RED_HAT}
	
	private final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private final String OS_VERSION = System.getProperty("os.version").toLowerCase();
	private String LINUX_DIST = "";

	private OS os = OS.UNKNOWN;
	private LINUX_DISTRIBUTION linuxDistribution = LINUX_DISTRIBUTION.UNKNOWN;
	
	private static OSDetector singleOSDetecor = null;
	
	public static OSDetector getOSDetector() {
		if (singleOSDetecor == null) {
			singleOSDetecor = new OSDetector();
		}
		
		return singleOSDetecor;
	}
	
	public OS getOSName() {
		return os;
	}
	
	public String getOSVersion() {
		return OS_VERSION;
	}
	
	public LINUX_DISTRIBUTION getLinuxDistribution() {
		return linuxDistribution;
	}

	public boolean isWindows() {

		return os == OS.WINDOWS;

	}

	public boolean isMac() {

		return os == OS.MAC;

	}

	public boolean isUnix() {

		return os == OS.LINUX;
		
	}
	
	private OSDetector() {
		if (OS_NAME.indexOf("win") >= 0) {
			os = OS.WINDOWS;
		}
		else if (OS_NAME.indexOf("mac") >= 0) {
			os = OS.MAC;
		}
		else if (OS_NAME.indexOf("linux") >= 0) {
			os = OS.LINUX;
			try {
				detectOSDistribution();
			}
			catch (Exception ex) {
				LINUX_DIST = "ERROR";
				linuxDistribution = LINUX_DISTRIBUTION.ERROR;
			}
		}
		else {
			os = OS.UNKNOWN;
		}
	}
    
    private void detectOSDistribution() {
        String strLine = null;
    	try {
	    	BufferedReader myReader = new BufferedReader(new FileReader("/etc/os-release"));
        	while ((strLine = myReader.readLine()) != null) {
			strLine = strLine.toLowerCase();
		    if (strLine.startsWith("id=")) {
		    	LINUX_DIST = strLine.substring("id=".length());
	    		break;
			    }
			}
			if (LINUX_DIST.equals("")) {
				System.err.println("Failed to detect LINUX OS distribution");
				LINUX_DIST = "ERROR";
				linuxDistribution = LINUX_DISTRIBUTION.ERROR;
			}
			else if (LINUX_DIST.contains("ubuntu")) {
				linuxDistribution = LINUX_DISTRIBUTION.UBUNTU;
			}
			else if (LINUX_DIST.contains("fedora") || LINUX_DIST.contains("rhel")) {
				linuxDistribution = LINUX_DISTRIBUTION.RED_HAT;
			}
			else {
				linuxDistribution = LINUX_DISTRIBUTION.UNKNOWN;
			}
        	myReader.close();
        }
        catch (IOException ioex) {
        	ioex.printStackTrace();
        }	
	}
}
