package unife.icedroid.core.messagecachingstrategies;

import unife.icedroid.core.ICeDROIDMessage;
import java.util.ArrayList;

public class FIFOStrategy extends MessageCachingStrategy {

    //In bytes
    private int cacheSize;

    FIFOStrategy(int size) {
        cacheSize = size;
    }

    @Override
    public void add(ArrayList<ICeDROIDMessage> list, ICeDROIDMessage msg) {
        int msgSize = msg.getSize();
        while (getListSize(list) + msgSize > cacheSize) {
            list.remove(0);
        }
        list.add(msg);
    }
}
