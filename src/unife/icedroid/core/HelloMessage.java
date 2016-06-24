package unife.icedroid.core;

import unife.icedroid.core.managers.MessageQueueManager;
import unife.icedroid.core.managers.ChannelListManager;
import java.util.ArrayList;

public class HelloMessage extends BaseMessage {
	private static final long serialVersionUID = 1;
    public static final String HELLO_MESSAGE = "helloMessage";
    public static final String EXTRA_HELLO_MESSAGE = "unife.icedroid.HELLO_MESSAGE";

    private ArrayList<String> hostSubscribedADCs;
    private ArrayList<ICeDROIDMessage> cachedMessages;

    public HelloMessage() {
        super();
        typeOfMessage = HELLO_MESSAGE;
        ttl = INFINITE_TTL;
        priority = MAX_PRIORITY_LEVEL;
        hostSubscribedADCs = ChannelListManager.getChannelListManager().getChannelList();

        ArrayList<ICeDROIDMessage> cm = MessageQueueManager.getMessageQueueManager().getCachedMessages();
        ArrayList<ICeDROIDMessage> dm = MessageQueueManager.getMessageQueueManager().getDiscardedMessages();

        //Remove payload, save only the header field of cached messages
        ArrayList<ICeDROIDMessage> cmHeaders = new ArrayList<>(0);
        for(ICeDROIDMessage m : cm) {
            cmHeaders.add(new ICeDROIDMessage(m));
        }

        cachedMessages = joinArrayLists(cmHeaders, dm);

        setSize();
    }


    public ArrayList<String> getHostSubscribedADCs() {
        return hostSubscribedADCs;
    }

    public ArrayList<ICeDROIDMessage> getCachedMessages() {
        return cachedMessages;
    }

    public void setHostChannels(ArrayList<String> channels) {
        hostSubscribedADCs = new ArrayList<>(channels);
    }

    public void setCachedMessages(ArrayList<ICeDROIDMessage> messages) {
        cachedMessages = new ArrayList<>(messages);
    }

    private <T> ArrayList<T> joinArrayLists(ArrayList<T> listOne, ArrayList<T> listTwo) {
        ArrayList<T> jointList = new ArrayList<>(listOne);
        for (T item : listTwo) {
            jointList.add(item);
        }
        return jointList;
    }

}