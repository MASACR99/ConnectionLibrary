/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

/**
 * Stores the description of each protocol with it's id and the expected return type
 * @author Jaume Fullana, Joan Gil
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
