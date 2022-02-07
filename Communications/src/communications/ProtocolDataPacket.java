/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.io.Serializable;

/**
 *
 * @author masa
 */
public class ProtocolDataPacket implements Serializable{    
    //This will contain a full packet, this has the ability to be sent via
    //socket or be received and read.
    
    private final String sourceID;
    private final String targetID;
    private final int id;
    private final Object object;
    
    public ProtocolDataPacket(String source, String target, int id, Object obj){
        this.sourceID = source;
        this.targetID = target;
        this.id = id;
        this.object = obj;
    }

    public String getSourceID() {
        return sourceID;
    }

    public String getTargetID() {
        return targetID;
    }

    public int getId() {
        return id;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "ProtocolDataPacket{" + "sourceID=" + sourceID + ", targetID=" + targetID + ", id=" + id + ", object=" + object + '}';
    }
}
