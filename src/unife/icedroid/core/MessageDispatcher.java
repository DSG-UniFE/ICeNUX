package unife.icedroid.core;

import unife.icedroid.services.ApplicationLevelDisseminationChannelService;
import unife.icedroid.utils.Settings;
import java.net.DatagramPacket;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.util.Date;

public class MessageDispatcher {
    private static final String TAG = "MessageDispatcher";
    
    private Settings s;
    
    public MessageDispatcher() {
    	s = Settings.getSettings();
    }

    // Received a new packet from the NIC
    public void dispatch(DatagramPacket packet) {
        ByteArrayInputStream byteArrayInputStream;
        ObjectInputStream rawMessage;
        BaseMessage baseMessage;
        Intent intent;

            try {
                byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                rawMessage = new ObjectInputStream(byteArrayInputStream);
                baseMessage = (BaseMessage) rawMessage.readObject();
                rawMessage.close();
                byteArrayInputStream.close();

                //Filter out messages generated from this host
                if (!baseMessage.getHostID().equals(s.getHostID())) {
                    //Set message reception time
                    baseMessage.setReceptionTime(new Date(System.currentTimeMillis()));

                    if (baseMessage.getTypeOfMessage().equals(ICeDROIDMessage.ICEDROID_MESSAGE)) {
                        intent = new Intent();
                        intent.putExtra(ApplicationLevelDisseminationChannelService.EXTRA_ADC_MESSAGE, baseMessage);
                        Settings.getSettings().getADCThread().add(intent);
                    } else {
                        intent = new Intent();
                        intent.putExtra(HelloMessage.EXTRA_HELLO_MESSAGE, baseMessage);
                        s.getHMThread().add(intent);
                    }
                }
            } catch (Exception ex) {
            	System.err.println(TAG + " - " + ex.getMessage());
            }
    }
}