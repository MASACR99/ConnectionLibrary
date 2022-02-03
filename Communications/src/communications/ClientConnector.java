/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author PC
 */
public class ClientConnector implements Runnable{
    
    //TO DO: Check if actual implementation of PORT is valid for our application
    
    private CommunicationController controller; 
    private ArrayList <Connection> connectionsList;
    
    public ClientConnector(CommunicationController controller) {
        this.controller = controller;
        this.connectionsList = new ArrayList <>();
    }
    
    public Socket connect(String ip, int port){
        try {
            Socket socket=new Socket(ip,port);
            System.out.println("Connected");
            return socket;
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            for (int i=0;i<this.connectionsList.size();i++){
                if (!this.connectionsList.get(i).isStatusOk()){

                    this.tryToReconnect(this.connectionsList.get(i));
                }
            }
        }
    }
    
    private void tryToReconnect(Connection conn){
        try {
            Socket socket=new Socket(conn.getIp(),controller.PORT);
            conn.setSocket(socket);
            conn.setStatusOk(true);
            System.out.println("Reconnected");
        } catch (IOException ex) {
            System.out.println("Error reconnecting: " + ex.getMessage());
        }
    }
    
}
