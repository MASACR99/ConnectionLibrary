/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author PC
 */
public class ServerHealth implements Runnable{
    
    private final Connection connection;
    private final CommunicationController controller;
    private long timeSent;
    private boolean ACKwait;
    private int checkCode;

    public ServerHealth(CommunicationController controller, Connection connection) {
        this.connection = connection;
        this.controller = controller;
    }

    public int getLastTestCode() {
        return checkCode;
    }

    public void setTestRequestWaiting(boolean ACKwait) {
        this.ACKwait = ACKwait;
    }
    
    @Override
    public void run() {
        while (true){
            checkState();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    private void checkState(){
        if (this.connection.isStatusOk() && System.currentTimeMillis()-this.connection.getLastMessageReceived()>controller.SERVERHEALTHMAXWAIT){
            this.createTestMessage();
            ProtocolDataPacket packet=new ProtocolDataPacket(connection.getLocalMAC(),connection.getConnectedMAC(),1,Integer.toString(this.checkCode));
            this.connection.send(packet);
            this.waitTestAnswer();
            
            if (ACKwait){
                System.out.println("Socket is dead, restarting...");
                this.connection.setStatusOk(false);
                this.connection.cerrarSocket();
                this.ACKwait=false;
            }
        }
    }
    
    private void createTestMessage(){
        Random r=new Random();
        byte [] bytes=new byte[4];
        r.nextBytes(bytes);
        this.checkCode=ByteBuffer.wrap(bytes).getInt();
    }
    
    private void waitTestAnswer(){
        this.ACKwait=true;
        this.timeSent=System.currentTimeMillis();
        long timeRequestPassed=System.currentTimeMillis()-this.timeSent;
        while (timeRequestPassed<controller.ACKMAXWAIT && ACKwait){
            timeRequestPassed=System.currentTimeMillis()-this.timeSent;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public void checkTestAnswer(ProtocolDataPacket packet){
        if (((int)packet.getObject()) == checkCode){
            this.ACKwait=false;
        }
    }
}
