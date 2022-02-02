/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import static communications.Connection.PORT;
import java.net.NetworkInterface;
import java.net.ServerSocket;

/**
 *
 * @author masa
 */
public class ServerConnector implements Runnable{
    
    //This class waits for external connections, when one is received it checks
    //if there's an empty connection and fills it with the data
    
    private ServerSocket serverSocket;
    private Communications comms;
    
    public ServerConnector(Communications comms){
        this.comms = comms;
    }
    
    public void connectar() {
        try{
            serverSocket = new ServerSocket(PORT);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        while(true){
            try {
                System.out.println("Server online and waiting for connections...");
                //If we have available connections (at least one of the connections is socket null and
                //status false
                
                //TO DO: have to open a new connection for each one and have a 
                //handshake before fully accepting the connection and saving it
                
                //TO DO: Change this part below to not use the main (the commented part)
                /*if(comms.availableConnections()){
                    //Send to communications the socket so it's saved
                    comms.sendSocket(serverSocket.accept());
                    //comms.sendId();
                }else{
                    //TODO: Change empty accept for an accept + redirection, giving
                    //the connecting client an ip to connect to
                    serverSocket.accept().close();
                }*/
            } catch (Exception e) {
                System.out.println("server error");
                e.printStackTrace();
            }
        }
    }
    
    //TO DO: Test this, computers may have many macs and ips at the same time, we just need
    //one mac to identify ourselves, if this returns one correctly, this should be good enough
    public String getMac(){
        try{
            //TO DO: Change byte[] to String
            byte[] mac = NetworkInterface.getByInetAddress(serverSocket.getInetAddress()).getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            String address = sb.toString();
            return address;
        }catch(Exception ex){
            System.out.println("Couldnt get Mac address");
            return null;
        }
    }

    @Override
    public void run() {
        while(true){
            connectar();
        }
    }
    
}