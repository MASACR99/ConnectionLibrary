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
    
    //TO DO: Cambiar valor dinamicas de temps de espera,... per CONSTANTS.
    
    private Connection connection;
    private long timeTestRequest;
    private boolean testRequestWaiting;
    private int lastTestCode;

    public ServerHealth(Connection connection) {
        this.connection = connection;
    }

    public int getLastTestCode() {
        return lastTestCode;
    }

    public void setTestRequestWaiting(boolean testRequestWaiting) {
        this.testRequestWaiting = testRequestWaiting;
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
        if (this.connection.isStatusOk() && System.currentTimeMillis()-this.connection.getLastTimeSendingOk()>SERVERHEALTHMAXWAIT){
            this.createTestMessage();
            //TO DO: This ids are fixed, change
            ProtocolDataPacket po=new ProtocolDataPacket(1,2,1,Integer.toString(this.lastTestCode));
            this.connection.send(po);
            this.waitTestAnswer();
            
            if (testRequestWaiting){
                System.out.println("Socket is dead, restarting...");
                this.connection.setStatusOk(false);
                this.connection.cerrarSocket();
                this.testRequestWaiting=false;
            }
        }
    }
    
    private void createTestMessage(){
        Random r=new Random();
        byte [] bytes=new byte[4];
        r.nextBytes(bytes);
        this.lastTestCode=ByteBuffer.wrap(bytes).getInt();
    }
    
    private void waitTestAnswer(){
        this.testRequestWaiting=true;
        this.timeTestRequest=System.currentTimeMillis();
        long timeRequestPassed=System.currentTimeMillis()-this.timeTestRequest;
        while (timeRequestPassed<ACKMAXWAIT && testRequestWaiting){
            timeRequestPassed=System.currentTimeMillis()-this.timeTestRequest;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public void checkTestAnswer(ProtocolDataPacket po){
        if (((int)po.getObject()) == lastTestCode){
            this.testRequestWaiting=false;
        }
    }
}
