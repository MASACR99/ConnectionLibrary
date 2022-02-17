/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balls;

import static balls.HeyBalls.MAXBALLS;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author masa
 */
public class BallsPanel extends JPanel{
    
    private HeyBalls heyBalls;
    private Simulation sim;
    private JSpinner balls;
    
    public BallsPanel(HeyBalls main){
        super();
        heyBalls = main;
        construct();
    }
    
    public void construct(){
        //Define everything related to the panel
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        
        //Define the different buttons and thingies
        sim = new Simulation(this.heyBalls);
        JCheckBox gravity = new JCheckBox("Gravity?");
        JCheckBox borders = new JCheckBox("Borders?");
        JCheckBox dragg = new JCheckBox("Drag?");
        
        //Add change listener to keep gravity and border booleans as the checkboxes
        //without having to check the checkboxes
        gravity.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                sim.setGrav(gravity.isSelected());
            }
        });
        borders.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                sim.setBorder(borders.isSelected());
            }
        });
        dragg.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                sim.setDrag(dragg.isSelected());
            }
        });
        balls = new JSpinner(new SpinnerNumberModel(0,0,MAXBALLS,1));
        balls.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                sim.setBalls((int)balls.getValue());
            }
        });
        
        //Attach everything together
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 5;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        this.add(sim,c);
        
        c.gridx = 5;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        this.add(gravity,c);
        
        c.gridx = 5;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        this.add(borders,c);
        
        c.gridx = 5;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        this.add(dragg,c);
        
        c.gridx = 5;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        this.add(balls,c);
    }

    void addBall(Ball ball) {
        balls.setValue((int)balls.getValue()+1);
        sim.setBalls((int)balls.getValue());
    }
    
    @Override
    public void paintComponent(Graphics g){
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        sim.paintComponent(g);
    }
    
}
