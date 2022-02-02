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
public class ProtocolDescription {
    private final int id;
    private final String description;
    private final String expectedReturn;
    
    public ProtocolDescription(int id, String description, String expectedReturn){
        this.id = id;
        this.description = description;
        this.expectedReturn = expectedReturn;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    
    public String getExpectedReturn() {
        return expectedReturn;
    }    
}
