/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communications;

/**
 *
 * @author masa
 */
public class CommunicationController {
    
    public final int PORT;
    public final int SERVERHEALTHMAXWAIT;
    public final int ACKMAXWAIT;
    public static final int PC = 37;
    public static final int MVL = 38;
    
    public CommunicationController(){
        PORT = 42069;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
    }
    
    public CommunicationController(int port){
        PORT = port;
        SERVERHEALTHMAXWAIT = 1500;
        ACKMAXWAIT = 2500;
    }
    
    public CommunicationController(int port, int serverWait, int ackWait){
        PORT = port;
        SERVERHEALTHMAXWAIT = serverWait;
        ACKMAXWAIT = ackWait;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
        // TODO code application logic here
        // TODO crear classe controladora per tot es programa, e interficie perque
        // sigui aplicada a nes programa que empleara aquesta biblioteca per tal de sobrescrigui el metode, el cual
        //sera l'encarregat de rebre coses del comands del protocol creats per l'usuari
    }
    
}
