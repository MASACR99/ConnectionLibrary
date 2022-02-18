/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import static communications.CommunicationController.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that controls a Socket and everything related to it. It also has a thread
 * that will be listening and processing all packets received.
 * @author Jaume Fullana, Joan Gil
 */
class Connection implements Runnable{
    
    private CommunicationController controller;
    private Protocol protocol;
    private Socket socket;
    private ServerHealth serverHealth;
    private boolean statusOk;
    private long lastMessageReceived;
    private InetAddress ip;
    private int connectionType;
    private boolean running;
    private String connectedMAC;
    private ConnectionInterfaceInitiater initiater;
    private HashMap<String, Integer> lookup= new HashMap<>();
    
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    Connection(CommunicationController controller, Socket socket, ConnectionInterfaceInitiater initiater, Protocol protocol) throws IOException {
        this.controller = controller;
        this.socket = socket;
        this.ip=this.socket.getInetAddress();
        this.protocol=protocol;
        this.connectedMAC = null;
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.statusOk=true;
        this.lastMessageReceived = System.currentTimeMillis();
        this.serverHealth = new ServerHealth(controller, this);
        this.initiater = initiater;
        this.connectionType=CLIENT;
        
        if (controller.getLocalIP()==null){
            controller.setLocalIP(this.socket.getLocalAddress());
        }
    }
    
    /**
     * Merges the already existing lookup with the neighbor one to get more
     * precise results
     * @param neighborMap Received hashmap of the neighbor
     */
    void addToLookup(HashMap<String,Integer> neighborMap){
        boolean saved = false;
        for(String str : neighborMap.keySet()){
            if(!lookup.containsKey(str)){
                //Notify and start a thread that will send a message to all
                //neighbors with the updated lookup
                saved = true;
                lookup.put(str, neighborMap.get(str)+1);
            }else{
                if(lookup.get(str) > (neighborMap.get(str)+1)){
                    //Notify and start a thread that will send a message to all
                    //neighbors with the updated lookup
                    saved = true;
                    lookup.replace(str, neighborMap.get(str)+1);
                }
            }
        }
        //To avoid spending processing time instanciating stuff we put it last inside
        //an if to just do it if necessary
        if(saved){
            LookupUpdater lookUpd = new LookupUpdater(this.controller);
            Thread lookupUpdaterThread = new Thread(lookUpd);
            lookupUpdaterThread.start();
        }
    }
    
    /**
     * Merges already existing lookup with data from the packet trace packet.
     * @param macPath Macs of all the received macs from the packet trace packet.
     */
    void addToLookup(ArrayList <String> macPath){
        int counter=1;
        boolean saved = false;
        for(String str : macPath){
            if(!lookup.containsKey(str)){
                //Notify and start a thread that will send a message to all
                //neighbors with the updated lookup
                saved = true;
                lookup.put(str, counter);
            }else{
                if(lookup.get(str) > counter){
                    //Notify and start a thread that will send a message to all
                    //neighbors with the updated lookup
                    saved = true;
                    lookup.replace(str, counter);
                }
            }
            counter++;
        }
        //To avoid spending processing time instanciating stuff we put it last inside
        //an if to just do it if necessary
        if(saved){
            LookupUpdater lookUpd = new LookupUpdater(this.controller);
            Thread lookupUpdaterThread = new Thread(lookUpd);
            lookupUpdaterThread.start();
        }
    }
    
    /**
     * Adds the mac as the connected mac to the lookup.
     * @param mac Neighbor mac.
     */
    void addToLookup(String mac){
        boolean saved = false;
        if(!lookup.containsKey(mac)){
            //Notify and start a thread that will send a message to all
            //neighbors with the updated lookup
            saved = true;
            lookup.put(mac, 1);
        }else{
            if(lookup.get(mac) > 1){
                //Notify and start a thread that will send a message to all
                //neighbors with the updated lookup
                saved = true;
                lookup.replace(mac, 1);
            }
        }
        //To avoid spending processing time instanciating stuff we put it last inside
        //an if to just do it if necessary
        if(saved){
            LookupUpdater lookUpd = new LookupUpdater(this.controller);
            Thread lookupUpdaterThread = new Thread(lookUpd);
            lookupUpdaterThread.start();
        }
    }
    
    /**
     * Returns the localMAC of the machine
     * @return Local mac of the machine
     */
    String getLocalMac(){
        return this.controller.getLocalMAC();
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
    
    HashMap<String,Integer> getLookup(){
        return lookup;
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

    void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
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
    
    /**
     * Set the socket and opens the two streams of the class.
     * @param socket to be setted
     * @throws IOException
     */
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
                    //If the packet hasn't exceeded the max number of allowed jumps
                    if(received.getHops() < 2 || received.getHops() <= (this.lookup.size()*2)){
                        //If the received packet id isn't one of the protocol
                        //and the target MAC is equals to ours
                        //we activate the connectionEvent
                        if(received.getTargetID() == null || received.getTargetID().equals(this.controller.getLocalMAC())){
                            if(!this.protocol.processMessage(this, received)){
                                initiater.packetEvent(received);
                            }
                        }else{
                            if (received.getId()==7){
                                this.protocol.processMessage(this, received);
                            }
                            else {
                                received.addHop();
                                controller.sendPacket(this,received);
                            }
                        }
                        lastMessageReceived=System.currentTimeMillis();
                    }
                }
                Thread.sleep(50);
            } catch (Exception ex) {
                System.out.println("run connection: "+ex.getMessage());
            }
        }
    }
    
    /**
     * Send the ProtocolDataPacked recvied in the paramether throught the 
     * ObjectOutputStream.
     * @param packet ProtocolDataPacked to be send.
     */
    synchronized void send(ProtocolDataPacket packet){
        try {
            this.output.writeObject(packet);
        } catch (IOException ex) {
            System.out.println("Couldn't send message: "+ex.getMessage());
        }
    }
    
    /**
     * Waits for a object to be recived throught the ObjectInputStream. If there 
     * is an error reciving the packet the statusOk is setted to false and the 
     * testRequestWaiting
     * @return object that is the packet recived
     */
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
    
    /**
     * Answer the request to know if this connection is still aveilable.
     * It has to resend the recived code in the object.
     * @param packetReceived packet where there is the request and the code that 
     * has to be resend.
     */
    void answerTestRequest(ProtocolDataPacket packetReceived){
        ProtocolDataPacket packet = new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,2,packetReceived.getObject());
        send(packet);
    }
    
    /**
     * Send a protocolDataPacket to ask the device type of the connected 
     * device.
     */
    void askDeviceType(){
        ProtocolDataPacket packet=new ProtocolDataPacket(this.controller.getLocalMAC(),null,3,null);
        send(packet);
    }
    
    /**
     * Send the device type of this device to the other side of the socket. 
     * 
     * @param packetReceived packet that ask the device type
     */
    void sendDeviceType(ProtocolDataPacket packetReceived){
        this.addToLookup((String) packetReceived.getSourceID());
        this.connectedMAC = (String) packetReceived.getSourceID();
        ProtocolDataPacket packet = new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,4,PC);
        send(packet);
    }
    
    /**
     * Process if the connected device is a mobile phone or a pc. If it's a pc  
     * the method checks if the controller can have another connection. If there 
     * isn't space the connection is rejected, otherwise is accepted. Then the 
     * method send its lookup table if the connection is accepted or a false 
     * boolean if the connection is rejected.
     * 
     * @param packetReceived the last packet received with the device type
     */
    void processDeviceType(ProtocolDataPacket packetReceived){
        ProtocolDataPacket packet;
        this.addToLookup((String) packetReceived.getSourceID());
        this.connectedMAC = (String) packetReceived.getSourceID();
        boolean validated=false;
        int deviceType=(int)packetReceived.getObject(); 
        if (deviceType == MVL){
            validated = true;
            packet = new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,5,this.controller.joinMaps());
            send(packet);
            this.controller.addMobileConnection(this);
            this.initiater.connectionEvent(this.connectedMAC);
        } 
        else if (deviceType == PC){
            validated=this.controller.availableConnections();
            if (validated){
                packet = new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,5,this.controller.joinMaps());
                send(packet);
                this.controller.addPcConnection(this);
                this.initiater.connectionEvent(this.connectedMAC);
            }
        }
        
        if (!validated){
            try {
                //First we get the hashmap with the macs and the connections to get to those
                //Then we start asking our first neighbour
                this.startAskingMacs();
                packet = new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,7,false);
                send(packet);
                Thread.sleep(1000);
                this.closeSocket();
                this.running=false;
            } catch(Exception ex){
                System.out.println("sleep processDeviceType: "+ex.getMessage());
                this.closeSocket();
                this.running=false;
            }
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
            this.initiater.connectionEvent(this.connectedMAC);
        } else {
            this.closeSocket();
            this.running=false;
        }
    }
    
    /**
     * Notify to the other side of the socket that this connection is going to be
     * closed and this socket don't want to reconect. After that and after a wait
     * of 1 second this connections closes.
     */
    void notifyClousure(){
        ProtocolDataPacket packet=new ProtocolDataPacket(this.controller.getLocalMAC(),this.getConnectedMAC(),8,null);
        this.send(packet);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            System.out.println("sleep processDeviceType: "+ex.getMessage());
        }
        this.running=false;
        this.controller.nullifyConnection(this);
        this.statusOk = false; //put to false just to stop serverhealth from reconnecting
        this.closeSocket();
        this.initiater.closingEvent(this.connectedMAC);
    }
    
    /**
     * Only used by Protocol to close a connection when notified
     */
    void processClousure(boolean running){
        this.running=running;
        this.controller.nullifyConnection(this);
        this.closeSocket();
        this.initiater.closingEvent(this.connectedMAC);
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
    
    /**
     * Send a package that traces it's route between us and it's target
     * @param targetId Target MAC address
     */
    void sendTraceroute(String targetId){
        ArrayList macPath=new ArrayList<>();
        macPath.add(this.controller.getLocalMAC());
        this.send(new ProtocolDataPacket(this.controller.getLocalMAC(),targetId,9,macPath));
    }
    
    /**
     * When a traceroute packet is received we check if it's for us, if so we add
     * the data to the lookup table, if it's not for us we add our mac to the packet
     * and send it via the shortest known path to the target
     * @param packetReceived Packet received via connection
     */
    void addMacTraceroute(ProtocolDataPacket packetReceived){
        if (packetReceived.getSourceID().equals(this.controller.getLocalMAC())){
            this.addToLookup((ArrayList)packetReceived.getObject());
        }
        else {
            ArrayList <String> macPath=(ArrayList)packetReceived.getObject();
            macPath.add(0,this.controller.getLocalMAC());
            this.send(new ProtocolDataPacket(packetReceived.getSourceID(),packetReceived.getTargetID(),9,macPath));
        }
    }
    
    /**
     * Updates the lookup table based on the neighbor peer. And sends back it's own
     * HashMap of the lookup table.
     * @param packetReceived Packet received via connection
     */
    void receiveLookupTable(ProtocolDataPacket packetReceived){
        this.addToLookup((HashMap<String,Integer>)packetReceived.getObject());
        this.send(new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,6,this.controller.joinMaps()));
    }
    
    /**
     * Updates the lookup table based on the neighbor peer. And sends back a connection
     * approved packet
     * @param packetReceived Packet received via connection
     */
    void receiveLookupTable2(ProtocolDataPacket packetReceived){
        this.addToLookup((HashMap<String,Integer>)packetReceived.getObject());
        send(new ProtocolDataPacket(this.controller.getLocalMAC(),this.connectedMAC,7,true));
    }
    
    /**
     * Updated the lookup table based on the neighbor peer. Doesn't do anything else
     * @param packetReceived  Packet received via connection
     */
    void updateLookup(ProtocolDataPacket packetReceived){
        this.addToLookup((HashMap<String,Integer>)packetReceived.getObject());
    }
    
    /**
     * Method to begin a package that will be propagated through all the peers
     * unless it finds a peer that has space for a connection.
     */
    private void startAskingMacs(){
        for(String mac : this.controller.getConnectedMacs()){
            ArrayList<String> macList = new ArrayList<String>();
            macList.add(this.socket.getInetAddress().toString());
            macList.add(this.controller.getLocalMAC());
            controller.sendPacket(null,new ProtocolDataPacket(this.controller.getLocalMAC(),mac,10,macList));
        }
    }
    
    /**
     * Method to receive a packet that checks if the controller has space for another
     * pc connection. If it does it starts a new connection to the ip in the packet
     * if not it resends the packet adding it's mac address to the packet object.
     * @param packet Received packet via connection.
     */
    void checkAvailability(ProtocolDataPacket packet){
        if(!this.controller.availableConnections()){
            ArrayList<String> macList = (ArrayList)packet.getObject();
            macList.add(this.controller.getLocalMAC());
            for(String mac : this.controller.getConnectedMacs()){
                if(!macList.contains(mac)){
                    controller.sendPacket(null,new ProtocolDataPacket(this.controller.getLocalMAC(),mac,10,macList));
                }
            }
        }else{
            String ip = ((ArrayList<String>)packet.getObject()).get(0);
            this.controller.connectToIp(ip);
        }
    }
    
    void requestChangePosition(String targetID){
        send(new ProtocolDataPacket(controller.getLocalMAC(),targetID,11,this.controller.getConnectedPcsIps()));
    }
    
    //TO DO: provar metodes i borrar comentaris. Lo comentat esta per probar en cas 
    // de que no vagi be si tancam es thread massa prest. En cas de que aixo simplement vagi be.
    //Lleva parametre a closeAllConnections i a processClousure
    void answerChangePosition(ProtocolDataPacket packet){
        ArrayList <InetAddress> ips=(ArrayList <InetAddress>)packet.getObject();
        send(new ProtocolDataPacket(controller.getLocalMAC(),packet.getSourceID(),12,this.controller.getConnectedPcsIps()));
        //this.controller.closeAllConnections(this);
        //ALOMILLO POSA UN SLEEP
        //this.processClousure(true);
        this.controller.closeAllConnections(null);
        this.controller.createNewConnections(ips, packet.getSourceID());
        //this.running=false;
    }
    
    void changingPositions(ProtocolDataPacket packet){
        ArrayList <InetAddress> ips=(ArrayList <InetAddress>)packet.getObject();
        //this.controller.closeAllConnections(this);
        //this.processClousure(true);
        this.controller.closeAllConnections(null);
        this.controller.createNewConnections(ips, packet.getSourceID());
        //this.running=false;
    }
}
