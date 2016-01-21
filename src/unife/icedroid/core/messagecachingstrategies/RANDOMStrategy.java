package unife.icedroid.core.messagecachingstrategies;

import unife.icedroid.core.ICeDROIDMessage;
import java.util.ArrayList;
import java.util.Random;

public class RANDOMStrategy extends MessageCachingStrategy {

    //In bytes
    private int cacheSize;

    RANDOMStrategy(int size) {
        cacheSize = size;
    }

    @Override
    public void add(ArrayList<ICeDROIDMessage> list, ICeDROIDMessage msg) {
        int msgSize = msg.getSize();
        Random random = new Random(System.currentTimeMillis());
        int index;
        while (getListSize(list) + msgSize <= cacheSize) {
            index = random.nextInt(list.size());
            list.remove(index);
        }
        list.add(msg);
    }
}
