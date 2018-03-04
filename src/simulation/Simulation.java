package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import physics.*;
import java.awt.Point;

public class Simulation {
    private Box outer;
    private Ball ball;
    private Triangle inner;
    private Lock lock;
    
    public Simulation(int width,int height,int dX,int dY)
    {
        outer = new Box(0,0,width,height,false);
        ball = new Ball(width/2,height/2,dX,dY);
        inner = new Triangle(width - 60,height - 40, 60,40,true);
        lock = new ReentrantLock();
    }
    
    public void evolve(double time)
    {
        lock.lock();
        Ray newLoc = inner.bounceRay(ball.getRay(), time);
        if(newLoc != null)
            ball.setRay(newLoc);
        else {
            newLoc = outer.bounceRay(ball.getRay(), time);
            if(newLoc != null)
                ball.setRay(newLoc);
            else
                ball.move(time);
        } 
        lock.unlock();
    }
    public Point getPosition(){
        Point p = new Point(inner.x+inner.width/2,inner.y);
        return p;
    }
    public void moveInner(int deltaX,int deltaY)
    {
        lock.lock();
        int dX = deltaX;
        int dY = deltaY;
        if(inner.x + deltaX < 0)
          dX = -inner.x;
        if(inner.x + inner.width + deltaX > outer.width)
          dX = outer.width - inner.width - inner.x;
       
        if(inner.y - inner.height + deltaY < 0)
           dY = 0;
        if(inner.y + deltaY > outer.height)
           dY = outer.height - inner.y;
        
        inner.move(dX,dY);
        if(inner.contains(ball.getRay().origin)) {
            // If we have discovered that the box has just jumped on top of
            // the ball, we nudge them apart until the box no longer
            // contains the ball.
            
            inner.setColor(Color.CYAN);
            int bumpX = -1;
            if(dX < 0) bumpX = 1;
            int bumpY = -1;
            if(dY < 0) bumpY = 1;
            do {
            inner.move(bumpX, bumpY);
            ball.getRay().origin.x += -bumpX;
            ball.getRay().origin.y += -bumpY;
            } while(inner.contains(ball.getRay().origin));
        }
        lock.unlock();
    }
    
    public List<Shape> setUpShapes()
    {
        ArrayList<Shape> newShapes = new ArrayList<Shape>();
        newShapes.add(outer.getShape());
        newShapes.add(inner.getShape());
        newShapes.add(ball.getShape());
        return newShapes;
    }
    
    public void updateShapes()
    {
        inner.setColor(Color.WHITE);
        inner.updateShape();
        ball.updateShape();
    }
}
