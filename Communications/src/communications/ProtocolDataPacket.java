/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import java.io.Serializable;

/**
 * Class that stores each packet, made of the source identifier (MAC), target
 * identifier (MAC), id of the protocol, and object to be sent.
 * @author Jaume Fullana, Joan Gil
 */
public class ProtocolDataPacket implements Serializable{
    
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
