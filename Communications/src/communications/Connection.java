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
    
    private MyTask myTask;
    private Protocol protocol;
    private Socket socket;
    private HealthSurveivor healthSurveivor;
    private boolean statusOk;
    private long lastTimeSendingOk;
    private InetAddress ip;
    
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public Connection(MyTask myTask, Socket socket) throws IOException {
        this.myTask = myTask;
        this.socket = socket;
        this.ip=this.socket.getInetAddress();
        this.protocol=new Protocol();
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.statusOk=true;
        this.lastTimeSendingOk = System.currentTimeMillis();
    }

    public void setHealthSurveivor(HealthSurveivor healthSurveivor) {
        this.healthSurveivor = healthSurveivor;
    }

    public HealthSurveivor getHealthSurveivor() {
        return healthSurveivor;
    }

    public long getLastTimeSendingOk() {
        return lastTimeSendingOk;
    }

    public String getIp() {
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
                ProtocolObject recibido=recive();
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
    
    public synchronized void send(ProtocolObject po){
        try {
            System.out.println("Enviando: "+po.getDescription());
            this.output.writeObject(po);
        } catch (IOException ex) {
            System.out.println("No enviado: "+ex.getMessage());
        }
    }
    
    public ProtocolObject recive(){
        ProtocolObject object=null;
        try {
            object = (ProtocolObject)this.input.readObject();
        } catch (Exception ex) {
            System.out.println("Excepcion recibiendo mensaje: "+ex.getMessage());
            this.statusOk=false;
            this.healthSurveivor.setTestRequestWaiting(false);
        }
        return object;
    }
    
    public void answerTestRequest(ProtocolObject poRecived){
        ProtocolObject po = new ProtocolObject(1,poRecived.getSourceId(),2,"TestRequestACK",poRecived.getData());
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
