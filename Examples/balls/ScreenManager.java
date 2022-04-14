/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package balls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * JPanel that has 2 panels on a tabbed pane
 * @author Joan Gil
 */
public class ScreenManager extends JPanel{

    private BallsPanel jPanel1;
    private ConnectionPanel jPanel2;
    private HeyBalls heyBalls;

    public ScreenManager(HeyBalls heyBalls){
        //Call super to also initialize default JPanel
        super();
        this.heyBalls = heyBalls;
        jPanel1 = new BallsPanel(heyBalls);
        jPanel2 = new ConnectionPanel(heyBalls);
        JTabbedPane tabs = new JTabbedPane();

        //Finally set the final config
        tabs.addTab("Balls", jPanel1);
        tabs.addTab("Connections", jPanel2);

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        this.add(tabs,c);
    }

    /**
     * Add a ball of object Ball to the simulation
     * @param ball Already defined object Ball
     */
    public void addBall(Ball ball){
        jPanel1.addBall(ball);
    }


}