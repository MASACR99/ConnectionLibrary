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
    private ArrayList<ConnectionInterface> closingListeners = new ArrayList<>();
    
    void addPacketListener(ConnectionInterface methodToAdd){
        packetListeners.add(methodToAdd);
    }
    
    void addConnectionListener(ConnectionInterface methodToAdd){
        connectionListeners.add(methodToAdd);
    }
    
    void addClosingListener(ConnectionInterface methodToAdd){
        closingListeners.add(methodToAdd);
    }
    
    /**
     * Called when a packet is received and the id isn't of the required protocol
     * @param packet Received packet
     */
    synchronized void packetEvent(ProtocolDataPacket packet){
        for(ConnectionInterface conn : packetListeners){
            conn.onMessageReceived(packet);
        }
    }
    
    /**
     * Called when a connection is made successfully
     * @param mac Mac address of the connected peer
     */
    synchronized void connectionEvent(String mac){
        for(ConnectionInterface conn : connectionListeners){
            conn.onConnectionAccept(mac);
        }
    }
    
    /**
     * Called when a connection is fully closed
     * @param mac Mac address of the peer to be disconnected
     */
    synchronized void closingEvent(String mac){
        for(ConnectionInterface conn : closingListeners){
            conn.onConnectionClosed(mac);
        }
    }
}
