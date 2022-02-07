/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

/**
 *
 * @author masa
 */
public interface ConnectionInterface {
    void onMessageReceived(ProtocolDataPacket packet);
}
