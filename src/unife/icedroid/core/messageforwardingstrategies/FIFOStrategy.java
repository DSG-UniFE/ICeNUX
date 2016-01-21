package unife.icedroid.core.messageforwardingstrategies;

import unife.icedroid.core.HelloMessage;
import unife.icedroid.core.BaseMessage;
import java.util.ArrayList;

public class FIFOStrategy extends MessageForwardingStrategy {

    @Override
    public void add(ArrayList<BaseMessage> list, BaseMessage msg, int index) {
        if (msg.getTypeOfMessage().equals(HelloMessage.HELLO_MESSAGE)) {
            removeHelloMessage(list);
        }
        list.add(msg);
    }

    private void removeHelloMessage(ArrayList<BaseMessage> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTypeOfMessage().equals(HelloMessage.HELLO_MESSAGE)) {
                    list.remove(i);
                    break;
                }
            }
    }
}
