/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balls;

import communications.CommunicationController;
import communications.ConnectionInterface;
import communications.ProtocolDataPacket;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author masa
 */
public class HeyBalls implements ConnectionInterface{

    public final static int MAXSPEED = 2;
    public final static int LOSS = 1;
    public final static int GRAVITY = 1;
    public final static int MAXBALLS = 1000;
    public final static int MAXRADIUS = 50;
    public final static int MAXWEIGHT = 10;
    public final static int MINRADIUS = 10;
    public final static int MAXFPS = 60;
    public final static double MINWEIGHT = 0.1;
    public CommunicationController controller;
    public HashMap<String,Integer> connections = new HashMap();
    private ScreenManager managment;
    public static int frames = 0;
    private int nextDirection = 0;
    
    public HeyBalls(){
        // Call screen manager to begin program
        // Screen manager might be 1 gravity toggle button, boundary button, number of balls and maybe randomizer
        long time1 = 0;
        long time2 = 0;
        float mspf = 1000/MAXFPS;
        controller = new CommunicationController(42069,2);
        Graphics g;
        ImageIcon pink = new ImageIcon("/home/masa/Downloads/pink.jpg");
        JFrame frame = new JFrame("Hey balls");
        frame.setLayout(new BorderLayout());
        managment = new ScreenManager(this);
        frame.setIconImage(pink.getImage());
        frame.add(managment);
        frame.pack();
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
    
    public String getInfo(int direction){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                return e;
            }
        }
        return null;
    }
    
    public boolean haveDirection(int direction){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                return true;
            }
        }
        return false;
    }
    
    public void connectTo(String ip, int direction){
        if(!haveDirection(direction)){
            System.out.println("Connecting to: " + ip);
            nextDirection = direction;
            controller.connectToIp(ip);
        }else{
            System.out.println("Not connecting");
        }
    }
    
    public void disconnect(String mac){
        controller.disconnect(mac);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HeyBalls main = new HeyBalls();
    }
    
    public void sendBall(int direction, Ball ball){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                controller.sendMessage(controller.createPacket(e,69,ball));
            }
        }
    }
    
    public void sendPosition(String mac, int direction){
        controller.sendMessage(controller.createPacket(mac, 420, direction));
    }
    
    public boolean isDirectionValid(int direction){
        for(String e : connections.keySet()){
            if(connections.get(e) == direction){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        switch(packet.getId()){
            case 69:
                managment.addBall((Ball)packet.getObject());
                break;
            case 420:
                connections.replace(packet.getSourceID(), (int)packet.getObject());
                break;
            default:
                
                break;
        }
    }

    @Override
    public void onConnectionAccept(String mac) {
        connections.put(mac, nextDirection);
        if(nextDirection != 0){
            this.sendPosition(mac,nextDirection);
        }
        nextDirection = 0;
    }

    @Override
    public void onConnectionClosed(String mac) {
        connections.remove(mac);
    }
}
