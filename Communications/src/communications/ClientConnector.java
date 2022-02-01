/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author PC
 */
public class ClientConnector implements Runnable{
    
    private MyTask myTask; 
        private ArrayList <Connection> connectionsList;
    
    public ClientConnector(MyTask myTask) {
        this.myTask = myTask;
        this.connectionsList = new ArrayList <>();
    }
    
    public void connect(String ip, int port){
        try {
            Socket socket=new Socket(ip,port);
            System.out.println("Connectado");
            myTask.createConnection(socket);
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void realizarAccion(){
        Scanner sc=new Scanner(System.in);
        System.out.println("A que direccion nos conectamos?");
        String ip=sc.nextLine();
        this.connect(ip, MyTask.PORT);
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
            Socket socket=new Socket(conn.getIp(),MyTask.PORT);
            conn.setSocket(socket);
            conn.setStatusOk(true);
            System.out.println("Reconnectado");
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage()+": No es posible reconectarse");
        }
    }
    
}
