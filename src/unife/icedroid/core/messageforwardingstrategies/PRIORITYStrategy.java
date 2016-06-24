package unife.icedroid.core.messageforwardingstrategies;

import unife.icedroid.core.HelloMessage;
import unife.icedroid.core.BaseMessage;
import unife.icedroid.core.ICeDROIDMessage;

import java.util.ArrayList;

public class PRIORITYStrategy extends MessageForwardingStrategy {

    @Override
    public void add(ArrayList<BaseMessage> list, BaseMessage msg, int indexForwardingMessages) {
        if (msg.getTypeOfMessage().equals(HelloMessage.HELLO_MESSAGE)) {
            int helloMessageIndex = getHelloMessageIndex(list);
            if (helloMessageIndex != -1) {
                list.add(helloMessageIndex, msg);
                list.remove(helloMessageIndex + 1);
                return;
            }
        } else {
            ICeDROIDMessage regularMessage = (ICeDROIDMessage) msg;
            if (regularMessage.getADCID().equals("Sport")) {
                if (indexForwardingMessages >= list.size()) {
                    indexForwardingMessages = 0;
                }
                int i;
                for (i = indexForwardingMessages; i < list.size(); i++) {
                    BaseMessage m = list.get(i);
                    if (m.getTypeOfMessage().equals(ICeDROIDMessage.ICEDROID_MESSAGE)) {
                        ICeDROIDMessage rm = (ICeDROIDMessage) m;
                        if (!rm.getADCID().equals("Sport")) {
                            break;
                        }
                    }
                }
                if (i < list.size()) {
                    list.add(i, regularMessage);
                    return;
                }
            }
        }
        list.add(msg);
    }

    private int getHelloMessageIndex(ArrayList<BaseMessage> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTypeOfMessage().equals(HelloMessage.HELLO_MESSAGE)) {
                return i;
            }
        }
        return -1;
    }
}
