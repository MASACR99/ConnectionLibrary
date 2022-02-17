/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balls;

import static balls.HeyBalls.GRAVITY;
import static balls.HeyBalls.MAXRADIUS;
import static balls.HeyBalls.MINRADIUS;
import static balls.HeyBalls.frames;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author masa
 */
public class Simulation extends JPanel {
    
    private ArrayList<Ball> ballArray = new ArrayList();
    private int ballNum = 0;
    private boolean grav = false;
    private boolean border = false;
    private boolean drag = false;
    private HeyBalls main;
    private int lastFrame = 0;
    
    private final Color colors[] = {Color.red,Color.black, Color.blue, Color.green, Color.MAGENTA, Color.orange, Color.YELLOW};
    
    public Simulation(HeyBalls main){
        super();
        this.main = main;
    }

    public void setGrav(boolean grav) {
        this.grav = grav;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }
    
    public void setBalls(int num){
        if(num < ballArray.size()){
            while(num < ballArray.size()){
                ballArray.remove(ballArray.size()-1);
            }
        }else{
            while(num > ballArray.size()){
                ballArray.add(new Ball(randomColor(),randomRadius()));
            }
        }
        ballNum = num;
    }
    
    private Color randomColor(){
        Random ran = new Random();
        return colors[ran.nextInt(colors.length)];
    }
    
    private int randomRadius(){
        Random ran = new Random();
        return (ran.nextInt(MAXRADIUS-MINRADIUS)+MINRADIUS);
    }
    
    private void physics(int limitX, int limitY, int accX, int accY){
        int auxVar = 0;
        for(Ball ball : ballArray){
            if(accX !=0 || accY !=0){
                ball.accelerate(accX,accY);
            }
            auxVar = ball.move(border, drag, limitX, limitY);
            if(main.isDirectionValid(auxVar)){
                main.sendBall(auxVar,ball);
                ballArray.remove(ball);
            }else{
                boolean found = false;
                for(Ball ballin : ballArray){
                    if(ballin != null){
                        if(found){
                            double dist = Math.sqrt((Math.pow((ballin.getX()-ball.getX()),2))+(Math.pow((ballin.getY()-ball.getY()),2)));
                            if (dist<=(ball.getRadius()/2)+(ballin.getRadius()/2)){
                                //WE HAVE IMPAKT, some hard calculations YUUUUJUUU
                                //change this shit
                                int speedX1 = ballin.getSpeedX();
                                int speedY1 = ballin.getSpeedY();
                                if((ball.getSpeedX() >= 0 && speedX1 >= 0) || (ball.getSpeedX() >= 0 && speedX1 >= 0)){
                                    ballin.setSpeedX(-ball.getSpeedX());
                                    ball.setSpeedX(-speedX1);
                                }else{
                                    ballin.setSpeedX(ball.getSpeedX());
                                    ball.setSpeedX(speedX1);
                                }
                                if((ball.getSpeedY() >= 0 && speedY1 >= 0) || (ball.getSpeedY() >= 0 && speedY1 >= 0)){
                                    ballin.setSpeedY(-ball.getSpeedY());
                                    ball.setSpeedY(-speedY1);
                                }else{
                                    ballin.setSpeedY(ball.getSpeedY());
                                    ball.setSpeedY(speedY1);
                                }
                                ball.move(border, drag, limitX, limitY);
                                ballin.move(border, drag, limitX, limitY);
                            }
                        }
                        if(ballin == ball){
                            found = true;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.clearRect(0, 0, WIDTH, HEIGHT);
        if(frames != lastFrame){
            if(ballNum > 0){
                if (grav){
                    this.physics(this.getWidth(), this.getHeight(), 0, GRAVITY);
                }else{
                    this.physics(this.getWidth(), this.getHeight(), 0, 0);
                }

                for(Ball ball : ballArray){
                    g2d.setColor(ball.getColor());
                    g2d.fillOval(ball.getOriginalX(), ball.getOriginalY(), ball.getRadius(), ball.getRadius());
                }
            }
        }
    }
    
}
