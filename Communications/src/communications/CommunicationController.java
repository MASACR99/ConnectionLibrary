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
 * TO DO: Check if we need to use public, private, default or protected for our methods
 * @author masa
 */
public class CommunicationController {
    
    public final int PORT;
    public final int SERVERHEALTHMAXWAIT;
    public final int ACKMAXWAIT;
    
    public static final int PC = 37;
    public static final int MVL = 38;
    
    public static final int SERVER = 24;
    public static final int CLIENT = 25;
    
    private ArrayList<Connection> pcConnections = new ArrayList<>();
    private ArrayList<Connection> mobileConnections = new ArrayList<>();
    private final int maxPc;
    private ServerConnector serverConn;
    private ClientConnector clientConn;
    
    public CommunicationController(){
        PORT = 42069;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
        maxPc = 2;
        libraryStarter();
    }
    
    public CommunicationController(int port, int maxPc){
        PORT = port;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
        if(maxPc <= 1){
            this.maxPc = 2;
        }else{
            this.maxPc = maxPc;
        }
        libraryStarter();
    }
    
    public CommunicationController(int port, int serverWait, int ackWait, int maxPc){
        PORT = port;
        SERVERHEALTHMAXWAIT = serverWait;
        ACKMAXWAIT = ackWait;
        if(maxPc <= 1){
            this.maxPc = 2;
        }else{
            this.maxPc = maxPc;
        }
        libraryStarter();
    }
    
    private void libraryStarter(){
        for(int i = 0; i < maxPc; i++){
            Connection conn = null;
            pcConnections.add(conn);
        }
        
        serverConn = new ServerConnector(this);
        clientConn = new ClientConnector(this);
        
        Thread serverThread = new Thread(serverConn);
        Thread clientThread = new Thread(clientConn);
        
        serverThread.start();
        clientThread.start();
    }
    
    public boolean availableConnections(){
        boolean available = false;
        for(int i = 0; i < maxPc && available == false; i++){
            if(pcConnections.get(i) == null){
                available = true;
            }else{
                if(pcConnections.get(i).getIp() == null && pcConnections.get(i).getSocket() == null){
                    available = true;
                }
            }
        }
        return available;
    }
    
    public void getServerSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket);
            conn.setConnectionType(SERVER);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    public void getClientSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    public void connectToIp(String ip){
        this.getClientSocket(clientConn.connect(ip, PORT));
    }
    
    public ArrayList<Connection> getPcConnections(){
        return this.pcConnections;
    }
    
    public ArrayList<Connection> getMobileConnections(){
        return this.mobileConnections;
    }
    
    public void addPcConnection(Connection conn){
        if(availableConnections()){
            if(!sameConnection(conn)){
                ServerHealth health = new ServerHealth(this,conn);
                conn.setServerHealth(health);
                pcConnections.add(conn);
            }
        }
    }
    
    private boolean sameConnection(Connection conn){
        boolean same = false;
        for(Connection con : this.pcConnections){
            if(con != null && con.getConnectedMAC() != null && con.getConnectedMAC().equals(conn.getConnectedMAC())){
                same = true;
                break;
            }
        }
        return same;
    }
    
    public void addMobileConnection(Connection conn){
        ServerHealth health = new ServerHealth(this,conn);
        conn.setServerHealth(health);
        mobileConnections.add(conn);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        // TODO crear classe controladora per tot es programa, e interficie perque
        // sigui aplicada a nes programa que empleara aquesta biblioteca per tal de sobrescrigui el metode, el cual
        //sera l'encarregat de rebre coses del comands del protocol creats per l'usuari
        CommunicationController con = new CommunicationController();
        Scanner input = new Scanner(System.in);
        while(true){
            System.out.println("Enter command: ");
            System.out.println("1- Connect");
            System.out.println("2- Chat");
            switch(input.nextInt()){
                case 1:
                    System.out.println("Gib ip");
                    input.nextLine();
                    con.connectToIp(input.nextLine());
                    break;
                case 2:
                    System.out.println("Not yet bookaroo");
                    break;
                default:
                    System.out.println("You did an oopsie");
                    break;
            }
        }
    }
    
}
