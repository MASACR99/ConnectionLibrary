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
    private ArrayList<ConnectionInterface> packetListeners = new ArrayList<>();
    private ArrayList<ConnectionInterface> connectionListeners = new ArrayList<>();
    
    void addPacketListener(ConnectionInterface methodToAdd){
        packetListeners.add(methodToAdd);
    }
    
    void addConnectionListener(ConnectionInterface methodToAdd){
        connectionListeners.add(methodToAdd);
    }
    
    synchronized void packetEvent(ProtocolDataPacket packet){
        for(ConnectionInterface conn : packetListeners){
            conn.onMessageReceived(packet);
        }
    }
    
    synchronized void connectionEvent(String mac){
        for(ConnectionInterface conn : connectionListeners){
            conn.onConnectionAccept(mac);
        }
    }
}
