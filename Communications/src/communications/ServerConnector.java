/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;

/**
 *
 * @author masa
 */
public class ServerConnector implements Runnable{
    
    //This class waits for external connections, when one is received it checks
    //if there's an empty connection and fills it with the data
    private CommunicationController controller;
    
    public ServerConnector(CommunicationController control){
        controller = control;
    }
    
    private ServerSocket serverSocket;

    public void connect() {
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
                
                if(controller.availableConnections()){
                    //Send to communications the socket so it's saved
                    controller.getSocket(serverSocket.accept());
                }else{
                    //TODO: Change empty accept for an accept + redirection, giving
                    //the connecting client an ip to connect to
                    serverSocket.accept().close();
                }
            } catch (Exception e) {
                System.out.println("server error");
                e.printStackTrace();
            }
        }
    }
    
    public String getMac(){
        //This gets the first MAC it finds. We may need to check which MAC we get.
        try{
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            byte[] mac = ni.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            String address = sb.toString();
            return address;
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Couldnt get Mac address");
            return null;
        }
    }

    @Override
    public void run() {
        while(true){
            connect();
        }
    }
    
}
