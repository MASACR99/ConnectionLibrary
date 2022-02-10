/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String localMAC;
    Protocol protocol;
    
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
        this.localMAC=this.getMac();
        
        protocol = new Protocol();
        
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
    
    public String getLocalMAC() {
        return localMAC;
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
     * Sends this message via the fastest available path. Also checks if the packet
     * is valid.
     * @param packet Packet to be sent
     * @return True if packet is valid and sent, else false
     */
    public boolean sendMessage(ProtocolDataPacket packet){
        return send(packet);
    }
    
    /**
     * Checks if the packet is valid and also sends it to resend to send it via the fastest path available.
     * Also overrides the packet source MAC to use this one
     * @param packet A packet to be checked and sent
     * @return True if packet is valid, else false
     */
    private boolean send(ProtocolDataPacket packet){
        boolean isValid = true;
        if(packet.getId() > protocol.getMinId() && packet.getTargetID() != null && !packet.getTargetID().equals(localMAC)){
            //send based on target id
            resend(null, new ProtocolDataPacket(localMAC,packet.getTargetID(),packet.getId(),packet.getObject()));
        }else{
            isValid = false;
        }
        return isValid;
    }
    
    /**
     * Serach for all the MACs in all the lookup tables of every connection. 
     * @return ArrayList where are all the MACs
     */
    public ArrayList<String> getConnectedMacs(){
        ArrayList <String> connectedMacs=new ArrayList<>();
        for(Connection conn:this.pcConnections){
           if (conn!=null){
                for (String mac:conn.getLookup().keySet()){
                    if (!connectedMacs.contains(mac) && !mac.equals(this.localMAC)){
                        connectedMacs.add(mac);
                    }
                }
           }
        }
        return connectedMacs;
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
        Connection leastJumps = null;
        int minJumps = -1;
        int aux;
        for(Connection e : this.getPcConnections()){
            if(e != null && e != conn){
                if(e.getConnectedMAC() != null){
                    aux = e.getJumpsTo(packet.getTargetID());
                    if(aux != -1){
                        if(aux == 1){
                            e.send(packet);
                            found = true;
                            break;
                        }else{
                            if(minJumps == -1 || aux < minJumps){
                                leastJumps = e;
                                minJumps = aux;
                            }
                        }
                    }
                }
            }
        }
        if(!found){
            leastJumps.send(packet);
        }//TO DO: Decide what to do if there's no path
    }
    
    /**
     * Joins the lookup tables of the pc connections into 1
     * @return Big lookup table of all the connections
     */
    HashMap<String, Integer> joinMaps(){
        HashMap<String,Integer> map = new HashMap<>();
        HashMap<String,Integer> pointerMap = new HashMap<>();
        for(Connection e : this.getPcConnections()){
            if(e != null){
                pointerMap = e.getLookup();
                for(String macs : pointerMap.keySet()){
                    if(!map.containsKey(macs)){
                        map.put(macs, e.getJumpsTo(macs));
                    }else{
                        if(map.get(macs) > e.getJumpsTo(macs)){
                            map.replace(macs, e.getJumpsTo(macs));
                        }
                    }
                }
            }
        }
        map.put(localMAC, 0);
        return map;
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
            Connection conn = new Connection(this, socket,initiater, protocol);
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
            Connection conn = new Connection(this, socket,initiater, protocol);
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
            if(this.pcConnections!=null && this.pcConnections.get(i).equals(conn)){
                this.pcConnections.set(i, null);
            }
        }
    }
    
    /**
     * Creates a new HashMap based on the joined map where every mac has the connection
     * with the fastest path to a mac address.
     * @param joinedMap HashMap of all lookup tables joined with the fastest paths
     * @return A HashMap with the fastest connection for a given mac address
     */
    HashMap connectMaps(HashMap<String, Integer> joinedMap){
        HashMap<String, Connection> returnMap = new HashMap();
        for(String str : joinedMap.keySet()){
            for(Connection e : this.pcConnections){
                if(e.getLookup().containsKey(str) && e.getLookup().get(str) == joinedMap.get(str)){
                    returnMap.put(str, e);
                    break;
                }
            }
        }
        return returnMap;
    }
    
        
    private String getMac(){
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
}
