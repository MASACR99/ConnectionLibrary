
/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import java.util.ArrayList;

/**
 * It stores the "protocols" to be used, where any protocol has an integer id,
 * a name and the return type. It has the ability to be expanded with client
 * "protocols" and returns the received packets to the client by using the
 * connection interface initiater.
 * @author Jaumer Fullana, Joan Gil
 */
class Protocol {
    
    private ArrayList<Integer> nonUsableIDs;
    private ArrayList<ProtocolDescription> protocolList;
    private final int lengthRequiredProtocol;
    
    Protocol(){
        this.nonUsableIDs = new ArrayList<>();
        this.protocolList = new ArrayList <>();
        this.protocolList.add(new ProtocolDescription(1, "Socket test", "Integer"));
        this.protocolList.add(new ProtocolDescription(2, "Socket test ACK", "Integer"));
        this.protocolList.add(new ProtocolDescription(3, "Acknowledge device type", "Null"));
        this.protocolList.add(new ProtocolDescription(4, "Return device type", "Integer"));
        this.protocolList.add(new ProtocolDescription(5, "Receive Lookup Table", "Hashmap<String,Integer>"));
        this.protocolList.add(new ProtocolDescription(6, "Receive Lookup Table2", "Hashmap<String,Integer>"));
        //antes de validar pasa lookup tables
        //conectedMac pasarles a sa lookup quan s'obri sa conexio
        this.protocolList.add(new ProtocolDescription(7, "Validate connection", "Boolean"));
        this.protocolList.add(new ProtocolDescription(8, "Close connection", "Null"));
        this.protocolList.add(new ProtocolDescription(9, "Traceroute", "ArrayList <String>"));
        this.protocolList.add(new ProtocolDescription(10, "Available Connections", "ArrayList <String>"));
        this.protocolList.add(new ProtocolDescription(13, "Update Lookups", "Hashmap<String,Integer>"));
        this.lengthRequiredProtocol = this.protocolList.size();
    }
    
    boolean addCmd(int id, ProtocolDescription desc){
        if(this.nonUsableIDs.add(id) && id > this.lengthRequiredProtocol){
            this.protocolList.add(desc);
            return true;
        }else{
            return false;
        }
    }
    
    ProtocolDescription getProtocol(int id){
        for(ProtocolDescription protocol : protocolList){
            if(protocol.getId() == id){
                return protocol;
            }
        }
        return null;
    }
    
    /**
     * Returns the minimal id that can be used from the beginning.
     * @return Returns the minimal integer id that can be used from the beginning.
     */
    int getMinId(){
        return this.lengthRequiredProtocol;
    }
    
    ProtocolDataPacket constructPacket(int id, String source, String target, Object object){
        return new ProtocolDataPacket(source, target, id, object);
    }
    
    ProtocolDataPacket constructPacket(int id, int hops, String source, String target, Object object){
        return new ProtocolDataPacket(source, target, hops, id, object);
    }
    
    boolean processMessage(Connection conn,ProtocolDataPacket packet){
        //TO DO: Talk. We need to check the target mac of the packet
        //though sometimes the packet will have a special protocol that will not have
        //a target MAC, so we must check this special cases.
        if (packet!=null){
            if(packet.getId() <= this.lengthRequiredProtocol){
                switch (packet.getId()){
                    case 1:
                        conn.answerTestRequest(packet);
                        break;

                    case 2: 
                        conn.getServerHealth().checkTestAnswer(packet);
                        break;

                    case 3:
                        conn.sendDeviceType(packet);
                        break;

                    case 4:
                        conn.processDeviceType(packet);
                        break;
                        
                    case 5:
                        conn.receiveLookupTable(packet);
                        break;

                    case 6:
                        conn.receiveLookupTable2(packet);
                        break;
                        
                    case 7:
                        conn.processValidation(packet);
                        break;
                        
                    case 8:
                        conn.processClousure();
                        break;
                        
                    case 9:
                        conn.addMacTraceroute(packet);
                        break;
                        
                    case 10:
                        //connection asks to controller if it has available connections
                        //returns an 11 with a boolean
                        conn.checkAvailability(packet);
                        break;
                        
                    case 13:
                        conn.updateLookup(packet);
                        break;
                        
                    default:
                        //Default isn't really needed, it's just used
                        //to make really sure we don't check "rogue" packets
                        return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }
}
