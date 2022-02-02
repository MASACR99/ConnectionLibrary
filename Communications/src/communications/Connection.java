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
    public static int PORT = 42069;
    public static int SERVERHEALTHMAXWAIT = 1500;
    public static int ACKMAXWAIT = 2500;
    //TO DO: Change code to not use the main
    private Communications comms;
    private Protocol protocol;
    private Socket socket;
    private ServerHealth serverHealth;
    private boolean statusOk;
    private long lastTimeSendingOk;
    private InetAddress ip;
    
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public Connection(Communications comms, Socket socket) throws IOException {
        this.comms = comms;
        this.socket = socket;
        this.ip=this.socket.getInetAddress();
        this.protocol=new Protocol();
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.statusOk=true;
        this.lastTimeSendingOk = System.currentTimeMillis();
        this.serverHealth = new ServerHealth(this);
    }
    
    //TO DO: Do we really need a getter and setter?
    public void setServerHealth(ServerHealth serverHealth) {
        this.serverHealth = serverHealth;
    }

    public ServerHealth getServerHealth() {
        return serverHealth;
    }

    public long getLastTimeSendingOk() {
        return lastTimeSendingOk;
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
        System.out.println("abriendo sockets");
        this.socket = socket;
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }
    
    @Override
    public void run() {
        System.out.println("Conexion hecha");
        lastTimeSendingOk=System.currentTimeMillis();
        while (true){
            if (this.statusOk){
                ProtocolDataPacket recibido=recive();
                this.protocol.processMessage(this, recibido);
                lastTimeSendingOk=System.currentTimeMillis();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());

            }
        }
    }
    
    public synchronized void send(ProtocolDataPacket po){
        try {
            this.output.writeObject(po);
        } catch (IOException ex) {
            System.out.println("No enviado: "+ex.getMessage());
        }
    }
    
    public ProtocolDataPacket recive(){
        ProtocolDataPacket object=null;
        try {
            object = (ProtocolDataPacket)this.input.readObject();
        } catch (Exception ex) {
            System.out.println("Excepcion recibiendo mensaje: "+ex.getMessage());
            this.statusOk=false;
            this.serverHealth.setTestRequestWaiting(false);
        }
        return object;
    }
    
    public void answerTestRequest(ProtocolDataPacket poRecived){
        //TO DO: Change static id for dynamic ones
        ProtocolDataPacket po = new ProtocolDataPacket(1,poRecived.getSourceID(),2,poRecived.getObject());
        send(po);
    }
    
    public void cerrarSocket(){
        try {
            System.out.println("cerrando sockets");
            this.socket.close();
            this.input.close();
            this.output.close();
            this.socket = null;
        } catch (IOException ex) {
            System.out.println("Socket ya cerrado "+ex.getMessage());
        }
    }
    
}
