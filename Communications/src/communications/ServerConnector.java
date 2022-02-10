/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.net.ServerSocket;

/**
 *
 * @author masa
 */
class ServerConnector implements Runnable{
    
    //This class waits for external connections, when one is received it checks
    //if there's an empty connection and fills it with the data
    private CommunicationController controller;
    
    ServerConnector(CommunicationController control){
        controller = control;
    }
    
    private ServerSocket serverSocket;

    void connect() {
        try{
            serverSocket = new ServerSocket(controller.PORT);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        while(true){
            try {
                Thread.sleep(50);
                //System.out.println("Server online and waiting for connections...");
                //If we have available connections (at least one of the connections is socket null and
                //status false
                
                //TO DO: have to open a new connection for each one and have a 
                //handshake before fully accepting the connection and saving it
                controller.getServerSocket(serverSocket.accept());
            } catch (Exception e) {
                System.out.println("server error");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while(true){
            connect();
        }
    }
    
}
