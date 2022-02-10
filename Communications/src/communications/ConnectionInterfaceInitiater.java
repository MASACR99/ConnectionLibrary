/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import java.util.ArrayList;

/**
 * Stores methods defined in connection interface and calls them in the event
 * that a packet with id not in protocol required is received.
 * @author Jaume Fullana, Joan Gil
 */
class ConnectionInterfaceInitiater {
    private ArrayList<ConnectionInterface> listeners = new ArrayList<>();
    
    void addListener(ConnectionInterface methodToAdd){
        listeners.add(methodToAdd);
    }
    
    synchronized void connectionEvent(ProtocolDataPacket packet){
        for(ConnectionInterface conn : listeners){
            conn.onMessageReceived(packet);
        }
    }
}
