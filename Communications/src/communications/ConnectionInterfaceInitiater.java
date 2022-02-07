/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.util.ArrayList;

/**
 *
 * @author masa
 */
class ConnectionInterfaceInitiater {
    private ArrayList<ConnectionInterface> listeners = new ArrayList<>();
    
    void addListener(ConnectionInterface methodToAdd){
        listeners.add(methodToAdd);
    }
    
    void connectionEvent(ProtocolDataPacket packet){
        for(ConnectionInterface conn : listeners){
            conn.onMessageReceived(packet);
        }
    }
}
