package unife.icedroid.core;

/**
 * Interface that defines a listener that will
 * be invoked by ICeNUX upon message reception
 */
public interface OnMessageReceiveListener {

    public void receive(ICeDROIDMessage message);
    
}
