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
/*

1- server health check (envia missatge per saber si tot obe V
2- server health ack (respon que to be) V
3- server ask Type (diferencia entre mvl i pc, seran constants, client retorna el tipo de modul  
qu es, no tendra target ja que sera es primer missatge enviat per server a nes client recient conectar) V
4- validateConnection (retorna true si ok, el servidor no te totes les conexions ocupades, false si les te ocupades) V
5- ProcessValidation V
6- CloseConnection (una conexio que es tanca envia aixo perque l'altre part sapiga que es va a tancar i per tant tambe la tanqui)

mes endevant:
7- demana a veinat directe mac ??? alomillo no necesari XX
8- demena mac qualsevol veinat
9- ask aveilable, s'intenten conectar, si no tens puesto demenaras a tots es veinats amem si colcu en te (retorna null o innetAddress)
10- ChangePositions
11- askLookup, a ne veinat

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
        this.protocolList.add(new ProtocolDescription(5, "Validate connection", "Boolean"));
        this.protocolList.add(new ProtocolDescription(6, "Close connection", "Null"));
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
    
    boolean processMessage(Connection conn,ProtocolDataPacket packet){
        //TO DO: Talk. We need to check the target mac of the packet
        //though sometimes the packet will have a special protocol that will not have
        //a target MAC, so we must check this special cases.
        if (packet!=null){
            if(packet.getId() <= 6){
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
                        conn.processValidation(packet);
                        break;
                        
                    case 6:
                        conn.processClousure();

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
