package unife.icedroid.services;

import unife.icedroid.core.BaseMessage;
import unife.icedroid.core.managers.MessageQueueManager;
import unife.icedroid.utils.Settings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

/**
 * BroadcastSendThread performs broadcasts of raw packets 
 * on the network.
 */
public class BroadcastSendThread extends Thread {
    private static final String TAG = "BroadcastSendThread";
    private static final boolean DEBUG = true;

    private MessageQueueManager messageQueueManager;
    private DatagramSocket socket;
    private String networkBroadcastAddress;
    private int recvPort;

    
    public BroadcastSendThread() throws Exception {
        Settings s = Settings.getSettings();
        messageQueueManager = MessageQueueManager.getMessageQueueManager();
        try {
            InetAddress localHost = InetAddress.getByName(s.getHostIP());
            socket = new DatagramSocket(0, localHost);
            socket.setBroadcast(true);
            networkBroadcastAddress = s.getNetworkBroadcastAddress();
            recvPort = s.getReceivePort();
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void run() {
        if (!Thread.interrupted()) {
            try {
                InetAddress broadcastAddress = InetAddress.getByName(networkBroadcastAddress);
                BaseMessage baseMessage;
                byte[] data;
                ByteArrayOutputStream byteArrayOutputStream;
                ObjectOutputStream objectOutputStream;
                DatagramPacket packet;

                while (true) {
                    baseMessage = messageQueueManager.getMessageToSend();

                    //Need to get a byte representation of the message
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                        objectOutputStream.writeObject(baseMessage);
                        data = byteArrayOutputStream.toByteArray();
                    } catch (IOException ex) {
                        data = null;
                        String msg = ex.getMessage();
                        if (DEBUG) {
                        	msg = TAG + " - " + ((msg != null) ? msg :
                        		"Impossible to convert the message with ID " + baseMessage.getMsgID() +
                        		" to a sequence of bytes");
                        	System.err.println(msg);
                        }
                    }

                    if (data != null) {
                        packet = new DatagramPacket(data, data.length, broadcastAddress, recvPort);
                        socket.send(packet);
                        if (DEBUG) {
                        	System.out.println(TAG + " Message sent: " + baseMessage);
                        }
                    }
                }
            } catch (InterruptedException iex) {
            } catch (Exception ex) {
                String msg = ex.getMessage();
            	msg = TAG + " - " + ((msg != null) ? msg : 
            		"Unhandled exception: " + ex);
            	System.err.println(msg);
            }
            finally {
            	socket.close();
            }
        }
    }
}
