/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

/**
 * Interface to override a method that is called as an event when a packet is received
 * outside the protocol required to run the backend.
 * @author Jaume Fullana, Joan Gil
 */
public interface ConnectionInterface {
    void onMessageReceived(ProtocolDataPacket packet);
    void onConnectionAccept(String mac);
    void onConnectionClosed(String mac);
}
