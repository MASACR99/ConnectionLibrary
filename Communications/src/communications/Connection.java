/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import static communications.CommunicationController.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author PC
 */
class Connection implements Runnable{
    
    private CommunicationController controller;
    private Protocol protocol;
    private Socket socket;
    private ServerHealth serverHealth;
    private boolean statusOk;
    private long lastMessageReceived;
    private InetAddress ip;
    private String connectedMAC;
    private String localMAC;
    private int connectionType;
    private boolean running;
    private ConnectionInterfaceInitiater initiater;
    private HashMap<String, Integer> lookup= new HashMap<>();
    
    private ObjectInputStream input;
    private ObjectOutputStream output;

    Connection(CommunicationController controller, Socket socket, ConnectionInterfaceInitiater initiater) throws IOException {
        this.controller = controller;
        this.socket = socket;
        this.ip=this.socket.getInetAddress();
        this.protocol=new Protocol();
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.statusOk=true;
        this.lastMessageReceived = System.currentTimeMillis();
        this.serverHealth = new ServerHealth(controller, this);
        this.initiater = initiater;
        //comença amb 0 ja que sino li feim es setter per cambiar valor a tipus
        //de conexio, instanciat pes seervidor o pes client, farem que simplement
        //sigui tractat com una conexio de ses "antigues"
        this.connectionType=CLIENT;
    }
    
    void addToLookup(HashMap<String,Integer> neighbourMap){
        for(String str : neighbourMap.keySet()){
            if(!lookup.containsKey(str)){
                lookup.put(str, neighbourMap.get(str)+1);
            }else{
                if(lookup.get(str) > (neighbourMap.get(str)+1)){
                    lookup.replace(str, neighbourMap.get(str)+1);
                }
            }
        }
    }
    
    /**
     * Returns the number of jumps to a mac address based on the lookup table
     * Returns -1 if that MAC doesn't exist in the table
     * @param mac String with the MAC address to get the jumps to
     * @return Number of jumps if any, else -1
     */
    int getJumpsTo(String mac){
        if(lookup.containsKey(mac)){
            return lookup.get(mac);
        }else{
            return -1;
        }
    }
    
    void setServerHealth(ServerHealth serverHealth) {
        this.serverHealth = serverHealth;
        Thread thread = new Thread(this.serverHealth);
        thread.start();
    }

    ServerHealth getServerHealth() {
        return serverHealth;
    }

    String getConnectedMAC() {
        return connectedMAC;
    }

    void setConnectedMAC(String connectedMAC) {
        this.connectedMAC = connectedMAC;
    }

    void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    String getLocalMAC() {
        return localMAC;
    }

    void setLocalMAC(String localMAC) {
        this.localMAC = localMAC;
    }

    long getLastMessageReceived() {
        return lastMessageReceived;
    }

    InetAddress getIp() {
        return ip;
    }

    boolean isStatusOk() {
        return statusOk;
    }
    
    void setStatusOk(boolean statusOk) {
        this.statusOk = statusOk;
    }

    void setSocket(Socket socket) throws IOException {
        System.out.println("Openning socket...");
        this.socket = socket;
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
    }

    Socket getSocket() {
        return socket;
    }
    
    @Override
    public void run() {
        running=true;
        System.out.println("Connection succesfull");
        lastMessageReceived=System.currentTimeMillis();
        if(connectionType==SERVER){
            this.askDeviceType();
        }
        while (running){
            try{
                if (this.statusOk){
                    ProtocolDataPacket received=receive();
                    //If the received packet id isn't one of the protocol
                    //and the target MAC is equals to ours
                    //we activate the connectionEvent
                    if(received.getTargetID() == null || received.getTargetID().equals(localMAC)){
                        if(!this.protocol.processMessage(this, received)){
                            initiater.connectionEvent(received);
                        }
                    }else{
                        controller.resend(this,received);
                    }
                    lastMessageReceived=System.currentTimeMillis();
                }
                Thread.sleep(50);
            } catch (Exception ex) {
                System.out.println("run connection: "+ex.getMessage());
            }
        }
    }
    
    synchronized void send(ProtocolDataPacket packet){
        try {
            this.output.writeObject(packet);
        } catch (IOException ex) {
            System.out.println("Couldn't send message: "+ex.getMessage());
        }
    }
    
    private ProtocolDataPacket receive(){
        ProtocolDataPacket object=null;
        try {
            object = (ProtocolDataPacket)this.input.readObject();
        } catch (Exception ex) {
            System.out.println("Error receiving message: "+ex.getMessage());
            this.statusOk=false;
            this.serverHealth.setTestRequestWaiting(false);
        }
        return object;
    }
    
    void answerTestRequest(ProtocolDataPacket packetReceived){
        ProtocolDataPacket packet = new ProtocolDataPacket(this.localMAC,this.connectedMAC,2,packetReceived.getObject());
        send(packet);
    }
    
    void askDeviceType(){
        ProtocolDataPacket packet=new ProtocolDataPacket(this.localMAC,null,3,null);
        send(packet);
    }
    
    void sendDeviceType(ProtocolDataPacket packetReceived){
        this.connectedMAC = (String) packetReceived.getSourceID();
        ProtocolDataPacket packet = new ProtocolDataPacket(this.localMAC,this.connectedMAC,4,PC);
        send(packet);
    }
    
    void processDeviceType(ProtocolDataPacket packetReceived){
        ProtocolDataPacket packet;
        this.connectedMAC = (String) packetReceived.getSourceID();
        boolean validated=false;
        int deviceType=(int)packetReceived.getObject(); 
        if (deviceType == MVL){
            validated = true;
            packet = new ProtocolDataPacket(this.localMAC,this.connectedMAC,5,validated);
            send(packet);
            this.controller.addMobileConnection(this);
        } 
        else if (deviceType == PC){
            validated=this.controller.availableConnections();
            packet = new ProtocolDataPacket(this.localMAC,this.connectedMAC,5,validated);
            send(packet);
            if (validated){
                this.controller.addPcConnection(this);
            }
        }
        
        if (!validated){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("sleep processDeviceType: "+ex.getMessage());
            }
            this.closeSocket();
            this.running=false;
        }
    }
    
    /**
     * Last handshake message. If the returned packet is true we save the pc connection
     * else we close the socket and stop the thread from running anymore.
     * Always receives a pc connections, since mobile ones don't need to
     * be validated.
     * @param packetReceived This is the last packet received from the starting handshake. 
     */
    void processValidation(ProtocolDataPacket packetReceived){
        if ((boolean)packetReceived.getObject()){
            this.controller.addPcConnection(this);
        } else {
            this.closeSocket();
            this.running=false;
        }
    }
    
    void notifyClousure(){
        ProtocolDataPacket packet=new ProtocolDataPacket(this.localMAC,this.connectedMAC,6,null);
        this.send(packet);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            System.out.println("sleep processDeviceType: "+ex.getMessage());
        }
        this.closeSocket();
        this.running=false;
    }
    
    /**
     * Only used by Protocol to close a connection when notified
     */
    void processClousure(){
        this.controller.nullifyConnection(this);
        this.closeSocket();
        this.running=false;
    }
    
    /**
     * Called to fully close a socket
     */
    void closeSocket(){
        try {
            System.out.println("Closing sockets");
            this.socket.close();
            this.input.close();
            this.output.close();
            this.socket = null;
        } catch (IOException ex) {
            System.out.println("Error closing sockets: "+ex.getMessage());
        }
    }
}
