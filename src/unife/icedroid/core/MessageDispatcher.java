package unife.icedroid.core;

import unife.icedroid.services.ApplevDisseminationChannelService;
import unife.icedroid.utils.Settings;
import java.net.DatagramPacket;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.util.Date;

public class MessageDispatcher {
    private static final String TAG = "MessageDispatcher";
    private static final boolean DEBUG = true;
    
    private Settings s;
    
    public MessageDispatcher() {
    	s = Settings.getSettings();
    }

    
    public void dispatch(DatagramPacket packet) {
        //DatagramPacket packet;
        ByteArrayInputStream byteArrayInputStream;
        ObjectInputStream rawMessage;
        BaseMessage baseMessage;
        Intent intent;

            try {
                byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                rawMessage = new ObjectInputStream(byteArrayInputStream);
                baseMessage = (BaseMessage) rawMessage.readObject();
                byteArrayInputStream.close();
                rawMessage.close();

                //Filter out messages generated from this host
                if (!baseMessage.getHostID().equals(s.getHostID())) {
                    //Set message reception time
                    baseMessage.setReceptionTime(new Date(System.currentTimeMillis()));

                    if (baseMessage.getTypeOfMessage().equals(ICeDROIDMessage.ICEDROID_MESSAGE)) {
                        intent = new Intent();
                        intent.putExtra(ApplevDisseminationChannelService.EXTRA_ADC_MESSAGE,
                                                                                    baseMessage);
                        Settings.getSettings().getADCThread().add(intent);
                    } else {
                        intent = new Intent();
                        intent.putExtra(HelloMessage.EXTRA_HELLO_MESSAGE, baseMessage);
                        Settings.getSettings().getHMThread().add(intent);
                    }
                }
            } catch (Exception ex) {
                /*String msg = ex.getMessage();
                if (DEBUG) {
                	if (msg != null) {
                		msg = TAG + " - " + msg;
                	} else {
                		msg = TAG + " - " + "deliver(): An error occurred";
                	}
                	System.out.println(msg);
                }*/
            	System.out.println(ex);
            }
    }
}