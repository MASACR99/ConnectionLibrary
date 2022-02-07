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
    
    static final int PC = 37;
    static final int MVL = 38;
    
    static final int SERVER = 24;
    static final int CLIENT = 25;
    
    private ArrayList<Connection> pcConnections = new ArrayList<>();
    private ArrayList<Connection> mobileConnections = new ArrayList<>();
    private final int maxPc;
    private ServerConnector serverConn;
    private ClientConnector clientConn;
    private ConnectionInterfaceInitiater initiater;
    
    /**
     * Empty constructor, starts port as 42069, healthwait 1500, ackwait = 2500
     * and maxPc = 2
     */
    public CommunicationController(){
        PORT = 42069;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
        maxPc = 2;
        libraryStarter();
    }
    
    /**
     * Constructor with port and maxPc as parameters, healthwait will start as 1500
     * and ackwait as 2500.
     * @param port Port that will be used by the sockets
     * @param maxPc Max amount of connected PCs at the same time, will be overriden if someone uses more and both connect
     */
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
    
    /**
     * Constructor asking for every possible parameter
     * @param port Port that will be used by the sockets
     * @param serverWait Amount of time to wait between messages to send the server health petition
     * @param ackWait Amount of time to wait for an acknowledge signal by server health
     * @param maxPc Max amount of connected PCs at the same time, will be overriden if someone uses more and both connect
     */
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
    
    /**
     * Method used to initialize all needed classes, methods and threads of the library
     */
    private void libraryStarter(){
        for(int i = 0; i < maxPc; i++){
            Connection conn = null;
            pcConnections.add(conn);
        }
        
        initiater = new ConnectionInterfaceInitiater();
        
        serverConn = new ServerConnector(this);
        clientConn = new ClientConnector(this);
        
        Thread serverThread = new Thread(serverConn);
        Thread clientThread = new Thread(clientConn);
        
        serverThread.start();
        clientThread.start();
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
    
    /**
     * Adds a ConnectionInterface to the initiater to be called
     * as an event when a packet is received.
     * @param conn A class implementing ConnectionInterface
     */
    public void addOnPacketListener(ConnectionInterface conn){
        initiater.addListener(conn);
    }
    
    /**
     * Attempts to connect to the given ip
     * @param ip String with the ip direction to connect to
     */
    public void connectToIp(String ip){
        this.getClientSocket(clientConn.connect(ip, PORT));
    }
    
    /**
     * Private getter of pcConnections
     * @return 
     */
    ArrayList<Connection> getPcConnections(){
        return this.pcConnections;
    }
    
    /**
     * Private getter of mobileConnections
     * @return 
     */
    ArrayList<Connection> getMobileConnections(){
        return this.mobileConnections;
    }
    
    /**
     * TO DO: In the future we will have to add to the packet where has it come through
     * so we will have to add our local mac to the packet, this can be done here
     * or inside connection. This will also have to check for the shortest path
     * to the target mac once the lookup tables are implemented.
     * We will also probably have to check if the 
     * @param conn
     * @param packet 
     */
    void resend(Connection conn, ProtocolDataPacket packet){
        boolean found = false;
        for(Connection e : this.getPcConnections()){
            if(e != conn){
                if(e.getConnectedMAC() != null && e.getConnectedMAC().equals(packet.getTargetID())){
                    e.send(packet);
                    found = true;
                    break;
                }
            }
        }
        if(!found){
            for(Connection e : this.getPcConnections()){
                if(e != conn){
                    e.send(packet);
                }
            }
        }
    }

    /**
     * Returns a boolean if there's enough space for a new connection
     * @return Boolean true if there's space, false if there isn't
     */
    boolean availableConnections(){
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
    
    /**
     * When a connection is accepted it gets sent to this socket
     * @param socket Accepted socket
     */
    void getServerSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket,initiater);
            conn.setConnectionType(SERVER);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    /**
     * When a connection is started this method gets called with the accepted socket
     * @param socket 
     */
    private void getClientSocket(Socket socket){
        try{
            Connection conn = new Connection(this, socket,initiater);
            Thread thread = new Thread(conn);
            thread.start();
        }catch(IOException ex){
            System.out.println("Problem creating a connection: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a controller connection to the mobileConnections ArrayList
     * @param conn Connection with a controller
     */
    void addMobileConnection(Connection conn){
        if(!sameMobileConnection(conn)){
            ServerHealth health = new ServerHealth(this,conn);
            conn.setServerHealth(health);
            mobileConnections.add(conn);
        }
    }
    
    /**
     * Adds a new Pc connection to the pcConnections arrayList
     * @param conn A handshaken pc connection
     */
    void addPcConnection(Connection conn){
        if(availableConnections()){
            if(!samePcConnection(conn)){
                ServerHealth health = new ServerHealth(this,conn);
                conn.setServerHealth(health);
                pcConnections.add(conn);
            }
        }
    }
    
    /**
     * Checks if the connection passed by parameter is already existing
     * Checked to avoid adding already existing connections usually due to a serverhealth reconnection
     * @param conn Handshaken connection
     * @return True if already existing, else false
     */
    private boolean samePcConnection(Connection conn){
        boolean same = false;
        for(Connection con : this.pcConnections){
            if(con != null && con.getConnectedMAC() != null && con.getConnectedMAC().equals(conn.getConnectedMAC())){
                same = true;
                break;
            }
        }
        return same;
    }
    
    /**
     * Checks if the controller connection passed by parameter is already existing
     * Checked to avoid adding already existing connections usually due to a serverhealth reconnection
     * @param conn
     * @return True if already existing, else false
     */
    private boolean sameMobileConnection(Connection conn){
        boolean same = false;
        for(Connection con : this.mobileConnections){
            if(con != null && con.getConnectedMAC() != null && con.getConnectedMAC().equals(conn.getConnectedMAC())){
                same = true;
                break;
            }
        }
        return same;
    }
    
    /**
     * Puts a closed connection to null to avoid ClientConnector reconnecting to it
     * @param conn Closed connection
     */
    void nullifyConnection(Connection conn){
        for(int i=0; i<this.pcConnections.size(); i++){
            if(this.pcConnections.get(i).equals(conn)){
                this.pcConnections.set(i, null);
            }
        }
    }
}
