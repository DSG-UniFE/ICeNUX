package unife.icedroid.core.routingalgorithms;

import unife.icedroid.core.ICeDROIDMessage;

public interface RoutingAlgorithm {

	public boolean addMessageForTransmission(ICeDROIDMessage msg);
    public ICeDROIDMessage removeMessage(ICeDROIDMessage msg, int index);
}
