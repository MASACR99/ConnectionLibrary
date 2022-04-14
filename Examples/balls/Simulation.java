/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
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
 * Stores multiple balls and has all method and variables to make the simulation work
 * @author Joan Gil
 */
public class Simulation extends JPanel {
    
    private ArrayList<Ball> ballArray = new ArrayList();
    private int ballNum = 0;
    private boolean grav = false;
    private boolean border = false;
    private boolean drag = false;
    private HeyBalls main;
    private int lastFrame = 0;
    private BallsPanel fatherPanel;
    
    //array of colors to grab a random one just by position
    private final Color colors[] = {Color.red,Color.black, Color.blue, Color.green, Color.MAGENTA, Color.orange, Color.YELLOW};
    
    public Simulation(HeyBalls main, BallsPanel fatherPanel){
        super();
        this.main = main;
        this.fatherPanel = fatherPanel;
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
    
    /**
     * Add balls or subtracts them based on the number given as a target
     * @param num Target number of balls
     */
    public void setBalls(int num){
        synchronized(ballArray){
            if(num < ballArray.size()){
                while(num < ballArray.size()){
                    ballArray.remove(ballArray.size()-1);
                }
            }else{
                while(num > ballArray.size()){
                    ballArray.add(new Ball(randomColor(),randomRadius()));
                }
            }
        }
        ballNum = num;
    }
    
    /**
     * Adds a ball based on an object Ball. Also constraints it to the screen size
     * @param ball Object ball already created
     */
    public void addBall(Ball ball){
        //constraint the ball to the local screen size
        if((ball.getX()+ball.getRadius()) > this.getWidth()){
            ball.setX(this.getWidth()-ball.getRadius());
        }
        if((ball.getY()+ball.getRadius()) > this.getHeight()){
            ball.setY(this.getHeight()-ball.getRadius());
        }
        synchronized(ballArray){
            ballArray.add(ball);
        }
        ballNum = ballArray.size();
    }
    
    private Color randomColor(){
        Random ran = new Random();
        return colors[ran.nextInt(colors.length)];
    }
    
    private int randomRadius(){
        Random ran = new Random();
        return (ran.nextInt(MAXRADIUS-MINRADIUS)+MINRADIUS);
    }
    
    /**
     * Has all the mathematical calculations based around contact of balls
     * it first makes the ball move, then looks for impacts with other balls,
     * if there was an impact move one further step. Finally return an arraylist
     * of balls that will be removed after the calculations (since they will
     * be sent to another socket)
     * @param limitX Limit of pixels of the X axis
     * @param limitY Limit of pixels of the Y axis
     * @param accX Acceleration of the X axis
     * @param accY Acceleration of the Y axis
     * @return ArrayList of balls that will be removed from the ballArray array list
     */
    private ArrayList physics(int limitX, int limitY, int accX, int accY){
        int auxVar = 0;
        ArrayList<Ball> removeBalls = new ArrayList();
        synchronized(ballArray){
            for(Ball ball : ballArray){
                if(accX !=0 || accY !=0){
                    ball.accelerate(accX,accY);
                }
                auxVar = ball.move(border, main, drag, limitX, limitY);
                if(main.haveDirection(auxVar)){
                    main.sendBall(auxVar,ball);
                    removeBalls.add(ball);
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
                                    if((ball.getSpeedX() >= 0 && speedX1 >= 0) || (ball.getSpeedX() <= 0 && speedX1 <= 0)){
                                        ballin.setSpeedX(-ball.getSpeedX());
                                        ball.setSpeedX(-speedX1);
                                    }else{
                                        ballin.setSpeedX(ball.getSpeedX());
                                        ball.setSpeedX(speedX1);
                                    }
                                    if((ball.getSpeedY() >= 0 && speedY1 >= 0) || (ball.getSpeedY() <= 0 && speedY1 <= 0)){
                                        ballin.setSpeedY(-ball.getSpeedY());
                                        ball.setSpeedY(-speedY1);
                                    }else{
                                        ballin.setSpeedY(ball.getSpeedY());
                                        ball.setSpeedY(speedY1);
                                    }
                                    ball.move(border, main, drag, limitX, limitY);
                                    ballin.move(border, main, drag, limitX, limitY);
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
        return removeBalls;
    }
    
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
        if(frames != lastFrame){
            ArrayList<Ball> returnVal;
            if(ballNum > 0){
                if (grav){
                    returnVal = this.physics(this.getWidth(), this.getHeight(), 0, GRAVITY);
                }else{
                    returnVal = this.physics(this.getWidth(), this.getHeight(), 0, 0);
                }
                if(!returnVal.isEmpty()){
                    for(Ball ball : returnVal){
                        this.ballArray.remove(ball);
                        fatherPanel.minusOne();
                    }
                }
                synchronized(ballArray){
                    for(Ball ball : ballArray){
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getOriginalX(), ball.getOriginalY(), ball.getRadius(), ball.getRadius());
                    }
                }
            }
        }
    }
    
}
