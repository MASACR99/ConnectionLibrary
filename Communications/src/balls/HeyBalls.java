/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package balls;

import communications.CommunicationController;
import communications.ConnectionInterface;
import communications.ProtocolDataPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Main class that controls the execution of the program
 * @author Joan Gil
 */
public class HeyBalls implements ConnectionInterface{

    //Define all static variables
    public final static int MAXSPEED = 2;
    public final static int LOSS = 1;
    public final static int GRAVITY = 1;
    public final static int MAXBALLS = 1000;
    public final static int MAXRADIUS = 50;
    public final static int MAXWEIGHT = 10;
    public final static int MINRADIUS = 10;
    public final static int MAXFPS = 60;
    public final static double MINWEIGHT = 0.1;
    public final static int MAX_PLAYER_ACC = 2;
    public static int frames = 0;
    
    public CommunicationController controller;
    public HashMap<String,Integer> connections = new HashMap();
    
    private ScreenManager managment;
    public static Ball playerBall = null;
    private int nextDirection = 0;
    
    public HeyBalls(){
        // Call screen manager to begin program
        long time1 = 0;
        long time2 = 0;
        float mspf = 1000/MAXFPS;
        controller = new CommunicationController(42069,2);
        controller.addAllListeners(this);
        Graphics g;
        ImageIcon pink = new ImageIcon("/home/masa/Downloads/pink.jpg");
        JFrame frame = new JFrame("Hey balls");
        frame.setSize(500,500);
        frame.setLayout(new BorderLayout());
        managment = new ScreenManager(this);
        frame.setIconImage(pink.getImage());
        frame.add(managment);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while(true){
            time1 = System.currentTimeMillis();
            while(time2-time1 < mspf){
                time2 = System.currentTimeMillis();
            }
            mspf = 1000/MAXFPS;
            frames++;
            frame.repaint();
            time2 = System.currentTimeMillis();
        }
    }
    
    /**
     * Returns the mac address of the specified direction
     * @param direction Integer between 1 and 4, 1 being north,2 east...
     * @return Mac of the connection
     */
    public String getInfo(int direction){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                return e;
            }
        }
        return null;
    }
    
    /**
     * Returns true if there's a connection on that direction
     * @param direction Integer between 1 and 4, 1 being north,2 east...
     * @return True if connection aiming in the direction exists, false otherwise
     */
    public boolean haveDirection(int direction){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Connect to the specified ip with the direction also specified
     * @param ip Ipv4 addres in string format (XXX.XXX.XXX.XXX)
     * @param direction Integer between 1 and 4, 1 being north,2 east...
     */
    public void connectTo(String ip, int direction){
        if(!haveDirection(direction)){
            System.out.println("Connecting to: " + ip);
            nextDirection = direction;
            controller.connectToIp(ip);
        }else{
            System.out.println("Not connecting");
        }
    }
    
    /**
     * Disconnects from the specified mac address
     * @param mac Mac address to disconnect from
     */
    public void disconnect(String mac){
        controller.disconnect(mac);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HeyBalls main = new HeyBalls();
    }
    
    /**
     * Send a ball object from this simulation to the simulation of the neighbor
     * @param direction Integer between 1 and 4, 1 being north,2 east...
     * @param ball Object Ball already defined
     */
    public void sendBall(int direction, Ball ball){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                controller.sendMessage(controller.createPacket(e,69,ball));
            }
        }
    }
    
    /**
     * Send a neighbor the position where you are.
     * @param mac Mac address of the neighbor
     * @param direction The direction of the neighbor from you
     */
    public void sendPosition(String mac, int direction){
        controller.sendMessage(controller.createPacket(mac, 420, direction));
    }

    /**
     * Event called by the connection library when a packet is received
     * @param packet Packet received
     */
    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        System.out.println("Received packet");
        switch(packet.getId()){
            case 69:
                System.out.println("Packet 69");
                managment.addBall((Ball)packet.getObject());
                break;
            case 420:
                System.out.println("Packet 420");
                int retVal = (int)packet.getObject();
                if(retVal < 3){
                    retVal += 2;
                }else{
                    retVal -= 2;
                }
                connections.replace(packet.getSourceID(), retVal);
                break;
            //I'm guessing packet 50 is for movement commands
            case 50:
                System.out.println("Packet 50, moving ball");
                int move[] = (int[])packet.getObject();
                //Guessing move[0] is power and move[1] is angle
                int power = move[0];
                int angle = move[1];
                power = (power * MAX_PLAYER_ACC) / 100; //Get the actual acceleration limited by a constant
                double powerX = (int) (power * Math.cos(angle)); //Get the X and Y axis acceleration
                double powerY = (int) (power * Math.sin(angle));
                playerBall.accelerate(powerX,powerY); //Send to the ball
                break;
            case 51:
                System.out.println("Phone connected creating ball");
                playerBall = new Ball(Color.MAGENTA,MAXRADIUS); //Create the player ball which will be Magenta and the biggest possible radius
                break;
            default:
                break;
        }
    }

    /**
     * Event called when a connection is fully accepted and the handshake
     * has been done
     * @param mac Mac address accepted
     */
    @Override
    public void onConnectionAccept(String mac) {
        System.out.println("Connection accepted");
        connections.put(mac, nextDirection);
        if(nextDirection != 0){
            this.sendPosition(mac,nextDirection);
        }
        nextDirection = 0;
    }

    /**
     * Event called when a connection is fully closed
     * @param mac Mac address of the disconnecting peer
     */
    @Override
    public void onConnectionClosed(String mac) {
        System.out.println("Connection closed");
        connections.remove(mac);
    }

    @Override
    public void onLookupUpdate(ArrayList<String> macs) {
        //Do nothing
    }
}
