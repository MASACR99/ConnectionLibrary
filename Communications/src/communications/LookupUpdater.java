/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;


/**
 * A threaded class that will run anytime we get an update on the lookup table
 * @author Jaume Fullana, Joan Gil
 */
public class LookupUpdater implements Runnable {
    
    private CommunicationController controller;
    
    public LookupUpdater(CommunicationController controller){
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.sendToNeighbors(13, controller.joinMaps());
    }
    
}
