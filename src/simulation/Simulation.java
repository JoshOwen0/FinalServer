package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import physics.*;

public class Simulation {
    private Box outer;
    private Ball ball;
    private Triangle[] inner;
    private Lock lock;
    private int[] scores;
    
    public Simulation(int width,int height,int dX,int dY)
    {
        outer = new Box(0,0,width,height,false);
        ball = new Ball(width/2,height/2,dX,dY);
        inner = new Triangle[2];
        inner[0] = new Triangle(width - 500,height - 100, 60,40,true);
        inner[1] = new Triangle(500,100, 60,-40,false);
        lock = new ReentrantLock();
        scores = new int[2];
    }
    public int[] getScore(){
        int score1= inner[0].score;
        int score2=inner[1].score;
        int[] scores=new int[2];
        scores[0] = score1;
        scores[1] = score2;
        
        return scores;
    }
    public synchronized void evolve(double time)
    {
        lock.lock();
        Ray newLoc = inner[0].bounceRay(ball.getRay(), time);
        Ray newLoc2 = inner[1].bounceRay(ball.getRay(),time);
        if(newLoc != null)
            ball.setRay(newLoc);
        if(newLoc2 != null){
            ball.setRay(newLoc2);
        }
        else {
            newLoc = outer.bounceRay(ball.getRay(), time);
            if(newLoc != null)
                ball.setRay(newLoc);
            else
                ball.move(time);
        } 
        lock.unlock();
    }
    public synchronized List<Point> getPaddlePosition(){
        Point p = new Point(inner[0].x,inner[0].y);
        Point p2 = new Point(inner[1].x,inner[1].y);
        List<Point> list = new ArrayList<Point>();
        list.add(p);
        list.add(p2);
        return list;
    }
    public synchronized Point getBallPosition(){
        return this.ball.getRay().origin;
    }

    public synchronized void moveInner(int deltaX,int deltaY,int player)
    {
        lock.lock();
        int dX = deltaX;
        int dY = deltaY;
        if(inner[player].x + deltaX < 0)
          dX = -inner[player].x;
        if(inner[player].x + inner[player].width + deltaX > outer.width)
          dX = outer.width - inner[player].width - inner[player].x;
       
        if(inner[player].y - inner[player].height + deltaY < 0)
           dY = 0;
        if(inner[player].y + deltaY > outer.height)
           dY = outer.height - inner[player].y;
        
        inner[player].move(dX,dY);
        if(inner[player].contains(ball.getRay().origin)) {
            // If we have discovered that the box has just jumped on top of
            // the ball, we nudge them apart until the box no longer
            // contains the ball.
            
            int bumpX = -1;
            if(dX < 0) bumpX = 1;
            int bumpY = -1;
            if(dY < 0) bumpY = 1;
            do {
            inner[player].move(bumpX, bumpY);
            ball.getRay().origin.x += -bumpX;
            ball.getRay().origin.y += -bumpY;
            } while(inner[player].contains(ball.getRay().origin));
        }
        lock.unlock();
    }
    
    public List<Shape> setUpShapes()
    {
        ArrayList<Shape> newShapes = new ArrayList<Shape>();
        newShapes.add(outer.getShape());
        newShapes.add(inner[0].getShape());
        newShapes.add(inner[1].getShape());
        newShapes.add(ball.getShape());
        return newShapes;
    }
    
    public void updateShapes()
    {
        inner[0].updateShape();
        inner[1].updateShape();
        ball.updateShape();
    }
}
