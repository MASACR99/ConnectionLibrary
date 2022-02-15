package vista;

import communications.CommunicationController;
import communications.ConnectionInterface;
import communications.ProtocolDataPacket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Jaume Fullana, Joan Gil
 */
public class View extends JFrame{
    
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
 
        CommunicationController con = new CommunicationController();
        con.connectToIp(JOptionPane.showInputDialog("Inserta la IP: "));
        View view=new View(con);
    }
    
    private CommunicationController controller;
    private ChatPanel chatPanel;

    public ChatPanel getChatPanel() {
        return chatPanel;
    }
    
    public JComboBox getChatPanelMacs() {
        return chatPanel.getMacs();
    }

    public View(CommunicationController controller) {
        this.controller=controller;
        this.setBounds(400,300,280,350);
        this.chatPanel=new ChatPanel(controller);
        this.add(chatPanel); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}

class ChatPanel extends JPanel implements ConnectionInterface{
    
    private JTextField campo;
    private JLabel labelLocalMac;
    private JComboBox macs;
    private JButton botonEnviar;
    private JTextArea chat;
    private CommunicationController controller;

    public JComboBox getMacs() {
        return macs;
    }
    
    public ChatPanel(CommunicationController controller){
        
        this.controller=controller;
        this.controller.addOnPacketListener(this);
        JLabel texto=new JLabel("Online: ");
        JLabel n_nick=new JLabel("Nick: ");
        this.labelLocalMac=new JLabel(controller.getLocalMAC());
        this.macs=new JComboBox();
        this.chat=new JTextArea(12,25);
        this.campo=new JTextField(20);
        this.botonEnviar=new JButton("Enviar");
        
        botonEnviar.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ProtocolDataPacket datos=new ProtocolDataPacket (controller.getLocalMAC(),
                        macs.getSelectedItem().toString(),666,campo.getText());
                controller.sendMessage(datos);
                chat.append("\n - "+campo.getText());
            }
        });
        this.add(n_nick);
        this.add(labelLocalMac);
        this.add(texto);
        this.add(macs);
        this.add(chat);
        this.add(campo);
        this.add(botonEnviar);
    }

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        if (packet.getTargetID().equals(controller.getLocalMAC())){
            if (packet.getId()==666){
                chat.append("\n"+packet.getSourceID()+": "+((String)packet.getObject()));
            }
        } 
    }

    @Override
    public void onConnectionAccept(String mac) {
        boolean repeated=false;
        int i=0;
        while (repeated==false && i<macs.getItemCount()){
            if (mac.equals(macs.getItemAt(i).toString())){
                repeated=true;
            }
            i++;
        }
        
        if (!repeated){
            macs.addItem(mac);
        }
    }
}