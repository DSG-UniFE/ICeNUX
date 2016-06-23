package unife.icedroid.services;

import unife.icedroid.core.MessageDispatcher;
import unife.icedroid.utils.Settings;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class BroadcastReceiveThread extends Thread {
    private static final String TAG = "BroadcastReceiveThread";
    private static final boolean DEBUG = true;

    private Settings s;
    private DatagramSocket socket;
    private MessageDispatcher messageDispatcher;

    public BroadcastReceiveThread() throws Exception{
    	try {
	        s = Settings.getSettings();
	        InetAddress broadcastWildcard = InetAddress.getByName("0.0.0.0");
	        socket = new DatagramSocket(s.getReceivePort(), broadcastWildcard);
	        messageDispatcher = new MessageDispatcher();
    	} catch (Exception ex) {
    		throw ex;
    	}

    }

    @Override
    public void run() {
        if (!Thread.interrupted()) {
            try {
                byte[] data = new byte[s.getMessageSize()];
                DatagramPacket packet;
                while (true) {
                    packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    //Filter out packets sent from this host
                    if (!packet.getAddress().getHostAddress().equals(s.getHostIP())) {
                        messageDispatcher.dispatch(packet);
                    }
                }
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (DEBUG) {
                	msg = TAG + ": " + ((msg != null) ? msg : "Error in BroadcastReceiveThread");
                	System.err.println(msg);
                }
            }
        }
        socket.close();
    }
}
