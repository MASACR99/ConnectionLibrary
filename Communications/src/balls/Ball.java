/*
 * This project is given as is with license GNU/GPL-3.0. For more info look
 * on github
 */
package balls;

import static balls.HeyBalls.LOSS;
import static balls.HeyBalls.MAXRADIUS;
import static balls.HeyBalls.MAXSPEED;
import static balls.HeyBalls.MAXWEIGHT;
import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

/**
 * A class ball stores all properties of it and has a method to calculate the movement
 * @author Joan Gil
 */
public class Ball implements Serializable{
    
    private int x;
    private int y;
    private double speedX;
    private double speedY;
    private Color color;
    private int radius;
    private double weight;
    
    public Ball(Color color, int radi){
        Random ran = new Random();
        this.color = color;
        radius = radi;
        weight = (radi*MAXWEIGHT)/MAXRADIUS;
        x = ran.nextInt(250)+radi;
        y = ran.nextInt(250)+radi;
        speedX = ran.nextInt(10);
        speedY = ran.nextInt(10);
    }
    
    /**
     * Change speed X and Y by a defined amount
     * @param accX Acceleration X axis
     * @param accY Acceleration Y axis
     */
    public void accelerate(double accX, double accY){
        speedX = speedX + accX;
        speedY = speedY + accY;
        if (speedX > MAXSPEED){
            speedX = MAXSPEED;
        }else if (speedX < -MAXSPEED){
            speedX = -MAXSPEED;
        }
        if (speedY > MAXSPEED){
            speedY = MAXSPEED;
        }else if (speedY < -MAXSPEED){
            speedY = -MAXSPEED;
        }
    }
    
    /**
     * Reverse the X direction and loose some speed
     * @param loss If there's loos on impact
     */
    public void reverseX(boolean loss){
        if (loss){
            if(speedX>0){
                speedX = -(speedX-LOSS);
            }else{
                speedX = -(speedX+LOSS);
            }
        }else{
            speedX = -speedX;
        }
    }
    
    /**
     * Reverse the Y direction and loose some speed
     * @param loss If there's loos on impact
     */
    public void reverseY(boolean loss){
        if (loss){
            if(speedY>0){
                speedY = -(speedY-LOSS);
            }else{
                speedY = -(speedY+LOSS);
            }
        }else{
            speedY = -speedY;
        }
    }
    
    public double getSpeedX(){
        return this.speedX;
    }
    
    public double getSpeedY(){
        return this.speedY;
    }
    
    public void setSpeedX(double speed){
        this.speedX = speed;
    }
    
    public void setSpeedY(double speed){
        this.speedY = speed;
    }
    
    public void setX(int x){
        this.x = x-this.radius/2;
    }
    
    public void setY(int y){
        this.y = y-this.radius/2;
    }
    
    public int getRadius(){
        return this.radius;
    }
    
    public int getX(){
        return (this.x+this.radius/2);
    }
    
    public int getY(){
        return (this.y+this.radius/2);
    }
    
    public int getOriginalX(){
        return this.x;
    }
    
    public int getOriginalY(){
        return this.y;
    }
    
    /**
     * Logic for the movement of a ball. Also does the logic for setting up the
     * position of the ball before sending it via socket.
     * @param border Boolean if there's borders around where there aren't any connections
     * @param main Main class to read the live connections
     * @param drag Boolean if there's drag, which means balls loose speed on impact against borders
     * @param limitX Max X axis in pixels
     * @param limitY Max Y axis in pixels
     * @return Int 0 if the ball doesn't leave the screen, a number from 1-4 based on which direction it leaves
     */
    public int move(boolean border, HeyBalls main, boolean drag, int limitX, int limitY){
        int retVal = 0;
        if(border){
            if(this.getX() + this.getRadius() + this.getSpeedX() >= limitX){
                if(!main.haveDirection(2)){
                    this.reverseX(drag);
                }else{
                    this.setX(0);
                    retVal = 2;
                }
            }else if(this.getX()+this.getSpeedX() <= 0){
                if(!main.haveDirection(4)){
                    this.reverseX(drag);
                }else{
                    this.setX(limitX-this.getRadius());
                    retVal = 4;
                }
            }
            if(this.getY() + this.getRadius() + this.getSpeedY() >= limitY){
                if(!main.haveDirection(3)){
                    this.reverseY(drag);
                }else{
                    this.setY(0);
                    retVal = 3;
                }
            }else if(this.getY()+this.getSpeedY() <= 0){
                if(!main.haveDirection(1)){
                    this.reverseY(drag);
                }else{
                    this.setY(limitY-this.getRadius());
                    retVal = 1;
                }
            }
            this.setX(this.getX() + (int) this.getSpeedX());
            this.setY(this.getY() + (int) this.getSpeedY());
        }else{
            if(this.getX() + this.getRadius() + this.getSpeedX() >= limitX){
                this.setX(0);
                retVal = 2;
            }else if(this.getX() + this.getRadius() + this.getSpeedX() <= 0){
                this.setX(limitX-this.getRadius());
                retVal = 4;
            }
            this.setX(this.getX() + (int) this.getSpeedX());
            if(this.getY() + this.getRadius() + this.getSpeedY() >= limitY){
                this.setY(0);
                retVal = 3;
            }else if(this.getY() + this.getRadius() + this.getSpeedY() <= 0){
                this.setY(limitY-this.getRadius());
                retVal = 1;
            }
            this.setY(this.getY() + (int) this.getSpeedY());
        }
        return retVal;
    }
    
    
    public void setColor(Color color){
        this.color = color;
    }
    
    public Color getColor(){
        return this.color;
    }
}
