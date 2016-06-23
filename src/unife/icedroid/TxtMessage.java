package unife.icedroid;

import unife.icedroid.core.ICeDROIDMessage;

public class TxtMessage extends ICeDROIDMessage {
	private static final long serialVersionUID = 1;
    private String group;
    private String contentData;

    public TxtMessage(String channel, String group, String txt) {
        super(channel);
        this.group = group;
        contentData = txt;
    }

    public TxtMessage(Subscription subscription, String txt) {
        super(subscription.getADCID());
        group = subscription.getApplicationName();
        contentData = txt;
    }

    public String getGroup() {
        return group;
    }

    public String getContentData() {
        return contentData;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setContentData(String txt) {
        contentData = txt;
    }

    @Override
    public TxtMessage clone() {
        return (TxtMessage) super.clone();
    }
}
