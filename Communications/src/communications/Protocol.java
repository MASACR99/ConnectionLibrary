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
    
    private ArrayList<Integer> nonUsableIDs;
    private ArrayList<ProtocolDescription> protocolList;
    
    public Protocol(){
        this.nonUsableIDs = new ArrayList<>();
        this.protocolList = new ArrayList <>();
        this.protocolList.add(new ProtocolDescription(1, "Socket test", "Integer"));
        this.protocolList.add(new ProtocolDescription(2, "Socket test ACK", "Integer"));
    }
    
    public boolean addCmd(int id, ProtocolDescription desc){
        if(nonUsableIDs.add(id)){
            this.protocolList.add(desc);
            return true;
        }else{
            return false;
        }
    }
    
    public ProtocolDescription getProtocol(int id){
        for(ProtocolDescription protocol : protocolList){
            if(protocol.getId() == id){
                return protocol;
            }
        }
        return null;
    }
    
    public ProtocolDataPacket constructPacket(int id, String source, String target, Object object){
        return new ProtocolDataPacket(source, target, id, object);
    }
    
    public boolean processMessage(Connection conn,ProtocolDataPacket packet){
        if (packet!=null){
            switch (packet.getId()){
                case 1:
                    conn.answerTestRequest(packet);
                    break;
                    
                case 2: 
                    conn.getServerHealth().checkTestAnswer(packet);
                    break;
                
                default:
                    return false;
            }
        }
        return true;
    }
}
