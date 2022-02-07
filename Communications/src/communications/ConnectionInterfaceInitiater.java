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
public class ConnectionInterfaceInitiater {
    private ArrayList<ConnectionInterface> listeners = new ArrayList<>();
    
    public void addListener(ConnectionInterface methodToAdd){
        listeners.add(methodToAdd);
    }
    
    public void connectionEvent(ProtocolDataPacket packet){
        for(ConnectionInterface conn : listeners){
            conn.onMessageReceived(packet);
        }
    }
}
