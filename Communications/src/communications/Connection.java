/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 *
 * @author PC
 */
public class Connection implements Runnable{
    
    //TO DO: Check if this implementation of statics is correct for our app
    //no es correcte, aniran a nes controlador.
    public static final int PORT = 42069;
    public static final int SERVERHEALTHMAXWAIT = 1500;
    public static final int ACKMAXWAIT = 2500;
    private Protocol protocol;
    private Socket socket;
    private ServerHealth serverHealth;
    private boolean statusOk;
    private long lastMessageReceived;
    private InetAddress ip;
    private String connectedMAC;
    private String localMAC;
    
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.ip=this.socket.getInetAddress();
        this.protocol=new Protocol();
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.statusOk=true;
        this.lastMessageReceived = System.currentTimeMillis();
        this.serverHealth = new ServerHealth(this);
    }
    
    //TO DO: Do we really need all of this getters and setters?
    public void setServerHealth(ServerHealth serverHealth) {
        this.serverHealth = serverHealth;
    }

    public ServerHealth getServerHealth() {
        return serverHealth;
    }

    public String getConnectedMAC() {
        return connectedMAC;
    }

    public void setConnectedMAC(String connectedMAC) {
        this.connectedMAC = connectedMAC;
    }

    public String getLocalMAC() {
        return localMAC;
    }

    public void setLocalMAC(String localMAC) {
        this.localMAC = localMAC;
    }

    public long getLastMessageReceived() {
        return lastMessageReceived;
    }

    public InetAddress getIp() {
        return ip;
    }

    public boolean isStatusOk() {
        return statusOk;
    }
    
    public void setStatusOk(boolean statusOk) {
        this.statusOk = statusOk;
    }

    public void setSocket(Socket socket) throws IOException {
        System.out.println("Openning socket...");
        this.socket = socket;
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }
    
    @Override
    public void run() {
        System.out.println("Connection succesfull");
        lastMessageReceived=System.currentTimeMillis();
        while (true){
            if (this.statusOk){
                ProtocolDataPacket recibido=recive();
                this.protocol.processMessage(this, recibido);
                lastMessageReceived=System.currentTimeMillis();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());

            }
        }
    }
    
    public synchronized void send(ProtocolDataPacket packet){
        try {
            this.output.writeObject(packet);
        } catch (IOException ex) {
            System.out.println("Couldn't send message: "+ex.getMessage());
        }
    }
    
    public ProtocolDataPacket recive(){
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
    
    public void answerTestRequest(ProtocolDataPacket packetReceived){
        ProtocolDataPacket po = new ProtocolDataPacket(this.localMAC,this.connectedMAC,2,packetReceived.getObject());
        send(po);
    }
    
    public void cerrarSocket(){
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
