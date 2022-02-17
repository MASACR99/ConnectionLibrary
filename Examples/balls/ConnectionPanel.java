/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author masa
 */
public class ConnectionPanel extends JPanel{
    
    private final int diameter = 25;
    private HeyBalls main;
    private JFrame ipPanel;
    private JDialog dataDialog;
    private JFrame dataPanel;
    private JLabel ipLabel;
    private JButton button;
    
    public ConnectionPanel(HeyBalls main){
        super();
        this.main = main;
        dataPanel = new JFrame();
        dataDialog = new JDialog(dataPanel);
        dataDialog.setLayout(new GridLayout(2,1));
        dataDialog.setSize(250, 250);
        ipLabel = new JLabel();
        button = new JButton("Disconnect");
        dataDialog.add(ipLabel);
        dataDialog.add(button);
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                main.disconnect(ipLabel.getText());
            }
        });
        ipPanel = new JFrame();
        this.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {}  
            @Override
            public void mouseEntered(MouseEvent e) {}  
            @Override
            public void mouseExited(MouseEvent e) {}  
            @Override
            public void mousePressed(MouseEvent e) {
                //check if mouse position is inside one of the circles
                //if it is, show either the connection logic and a disconnect button
                //if the connection is live
                //or if it isn't show a menu asking for an ip address
                //check bottom mid circle
                if(e.getX() <= getWidthLocal()/2+diameter/2 && e.getX() >= getWidthLocal()/2-diameter/2 && 
                        e.getY() <= getHeightLocal() && e.getY() >= getHeightLocal()-diameter){
                    System.out.println("Pressed bottom");
                    if(main.haveDirection(3)){
                        //show panel with connection info and disconnect button
                        ipLabel.setText(main.getInfo(3));
                        dataDialog.setVisible(true);
                    }else{
                        //show a panel asking for an ip
                        String ip = JOptionPane.showInputDialog(ipPanel,
                        "Ip to connect to?", null);
                        if(ip != null && !ip.isEmpty()){
                            main.connectTo(ip,3);
                        }
                    }
                }//then check top mid
                else if(e.getX() <= getWidthLocal()/2+diameter/2 && e.getX() >= getWidthLocal()/2-diameter/2 && 
                        e.getY() <= 0+diameter && e.getY() >= 0){
                    System.out.println("Pressed top");
                    if(main.haveDirection(1)){
                        //show panel with connection info and disconnect button
                        ipLabel.setText(main.getInfo(1));
                        dataDialog.setVisible(true);
                    }else{
                        //show a panel asking for an ip
                        String ip = JOptionPane.showInputDialog(ipPanel,
                        "Ip to connect to?", null);
                        if(ip != null && !ip.isEmpty()){
                            main.connectTo(ip,1);
                        }
                    }
                }//then check right
                else if(e.getX() <= getWidthLocal() && e.getX() >= getWidthLocal()-diameter && 
                        e.getY() <= getHeightLocal()/2+diameter/2 && e.getY() >= getHeightLocal()/2-diameter/2){
                    System.out.println("Pressed right");
                    if(main.haveDirection(2)){
                        //show panel with connection info and disconnect button
                        ipLabel.setText(main.getInfo(2));
                        dataDialog.setVisible(true);
                    }else{
                        //show a panel asking for an ip
                        String ip = JOptionPane.showInputDialog(ipPanel,
                        "Ip to connect to?", null);
                        if(ip != null && !ip.isEmpty()){
                            main.connectTo(ip,2);
                        }
                    }
                }//finally check left
                else if(e.getX() <= 0+diameter && e.getX() >= 0 && 
                        e.getY() <= getHeightLocal()/2+diameter/2 && e.getY() >= getHeightLocal()/2-diameter/2){
                    System.out.println("Pressed left");
                    if(main.haveDirection(4)){
                        //show panel with connection info and disconnect button
                        ipLabel.setText(main.getInfo(4));
                    }else{
                        //show a panel asking for an ip
                        String ip = JOptionPane.showInputDialog(ipPanel,
                        "Ip to connect to?", null);
                        if(ip != null && !ip.isEmpty()){
                            main.connectTo(ip,4);
                        }
                    }
                }
            }  
            @Override
            public void mouseReleased(MouseEvent e) {}  
        });
    }
    
    private int getWidthLocal(){
        return this.getWidth();
    }
    
    private int getHeightLocal(){
        return this.getHeight();
    }
    
    @Override
    public void paintComponent(Graphics g){
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        if(!main.haveDirection(1)){
            g.setColor(Color.red);
        }else{
            g.setColor(Color.green);
        }
        g.fillOval(this.getWidth()/2-diameter/2, 0, diameter, diameter);
        if(!main.haveDirection(4)){
            g.setColor(Color.red);
        }else{
            g.setColor(Color.green);
        }
        g.fillOval(0, this.getHeight()/2-diameter/2, diameter, diameter);
        if(!main.haveDirection(2)){
            g.setColor(Color.red);
        }else{
            g.setColor(Color.green);
        }
        g.fillOval(this.getWidth()-diameter, this.getHeight()/2-diameter/2, diameter, diameter);
        if(!main.haveDirection(4)){
            g.setColor(Color.red);
        }else{
            g.setColor(Color.green);
        }
        g.fillOval(this.getWidth()/2-diameter/2, this.getHeight()-diameter, diameter, diameter);
    }
}
