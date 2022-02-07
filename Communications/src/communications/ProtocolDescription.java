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
class ProtocolDescription {
    private final int id;
    private final String description;
    private final String expectedReturn;
    
    ProtocolDescription(int id, String description, String expectedReturn){
        this.id = id;
        this.description = description;
        this.expectedReturn = expectedReturn;
    }

    int getId() {
        return id;
    }

    String getDescription() {
        return description;
    }
    
    String getExpectedReturn() {
        return expectedReturn;
    }    
}
