/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balls;

import static balls.HeyBalls.LOSS;
import static balls.HeyBalls.MAXRADIUS;
import static balls.HeyBalls.MAXSPEED;
import static balls.HeyBalls.MAXWEIGHT;
import java.awt.Color;
import java.util.Random;

/**
 *
 * @author masa
 */
public class Ball {
    
    private int x;
    private int y;
    private int speedX;
    private int speedY;
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
    
    public void accelerate(int accX, int accY){
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
    
    public int getSpeedX(){
        return this.speedX;
    }
    
    public int getSpeedY(){
        return this.speedY;
    }
    
    public void setSpeedX(int speed){
        this.speedX = speed;
    }
    
    public void setSpeedY(int speed){
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
    
    public int move(boolean border, boolean drag, int limitX, int limitY){
        int retVal = 0;
        if(border){
            if(this.getX() + this.getRadius() + this.getSpeedX() >=limitX || this.getX()+this.getSpeedX() <= 0){
                this.reverseX(drag);
            }
            if(this.getY() + this.getRadius() + this.getSpeedY() >=limitY || this.getY()+this.getSpeedY() <= 0){
                this.reverseY(drag);
            }
            this.setX(this.getX() + this.getSpeedX());
            this.setY(this.getY() + this.getSpeedY());
        }else{
            if(this.getX() + this.getRadius() + this.getSpeedX() >=limitX){
                this.setX(0);
                retVal = 4;
            }else if(this.getX() + this.getRadius() + this.getSpeedX() <= 0){
                this.setX(limitX-this.getRadius());
                retVal = 2;
            }
            this.setX(this.getX() + this.getSpeedX());
            if(this.getY() + this.getRadius() + this.getSpeedY() >= limitY){
                this.setY(0);
                retVal = 1;
            }else if(this.getY() + this.getRadius() + this.getSpeedY() <= 0){
                this.setY(limitY-this.getRadius());
                retVal = 3;
            }
            this.setY(this.getY() + this.getSpeedY());
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
