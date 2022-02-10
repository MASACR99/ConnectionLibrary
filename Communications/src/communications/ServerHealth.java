/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package communications;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Sends messages based on the defined variables and waits for an ok signal
 * with an integer value, it checks if the value is correct, if it isn't it forces,
 * by using the client connector logic, the socket to be rebuilt to attempt to
 * solve any possible problem
 * @author Jaume Fullana, Joan Gil
 */
class ServerHealth implements Runnable{
    
    private final Connection connection;
    private final CommunicationController controller;
    private long timeSent;
    private boolean ACKwait;
    private int checkCode;

    ServerHealth(CommunicationController controller, Connection connection) {
        this.connection = connection;
        this.controller = controller;
    }

    int getLastTestCode() {
        return checkCode;
    }

    void setTestRequestWaiting(boolean ACKwait) {
        this.ACKwait = ACKwait;
    }
    
    @Override
    public void run() {
        while (connection.isStatusOk()){
            checkState();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /**
     * Waits for a defined time and sends a test package. Then waits for a return
     * and if it doesn't arrive or is incorrect it restarts the socket.
     */
    private void checkState(){
        if (this.connection.isStatusOk() && System.currentTimeMillis()-this.connection.getLastMessageReceived()>controller.SERVERHEALTHMAXWAIT){
            this.createTestMessage();
            ProtocolDataPacket packet=new ProtocolDataPacket(connection.getLocalMac(),connection.getConnectedMAC(),1,this.checkCode);
            this.connection.send(packet);
            this.waitTestAnswer();
            
            if (ACKwait){
                System.out.println("Socket is dead, restarting...");
                this.connection.setStatusOk(false);
                this.connection.closeSocket();
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
    
    /**
     * Waits for the ACK message
     */
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
    
    /**
     * Checks if the integer in the ACK packet is the same that was sent
     * @param packet Received ACK packet
     */
    void checkTestAnswer(ProtocolDataPacket packet){
        if (((int)packet.getObject()) == checkCode){
            this.ACKwait=false;
        }
    }
}
