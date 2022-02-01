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
public class Protocol {
    
    ArrayList<Integer> nonUsableIDs = new ArrayList<>();
    private ArrayList<ProtocolDescription> desc = new ArrayList<>();
    
    public Protocol(){
        
    }
    
    public boolean addCmd(int id, ProtocolDescription desc){
        if(nonUsableIDs.add(id)){
            this.desc.add(desc);
            return true;
        }else{
            return false;
        }
    }
    
    public ProtocolDescription getProtocol(int id){
        for(ProtocolDescription protocol : desc){
            if(protocol.getId() == id){
                return protocol;
            }
        }
        return null;
    }
    
    public ProtocolDataPacket constructPacket(int id, int source, int target, Object object){
        return new ProtocolDataPacket(source, target, id, object);
    }
}
