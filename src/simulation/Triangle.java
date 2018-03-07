/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import physics.LineSegment;
import physics.Point;
import physics.Ray;

/**
 *
 * @author Owen
 */
public class Triangle implements Serializable{
    private ArrayList<LineSegment> walls;
    public int x;
    public int y;
    public int width;
    public int height;
    public int score;
    
    // Set outward to true if you want a box with outward pointed normals
    public Triangle(int x,int y,int width,int height,boolean outward)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        walls = new ArrayList<LineSegment>();
        score = 5;
        if(outward) {
            walls.add(new LineSegment(new Point(x,y),new Point(x+width,y)));
            walls.add(new LineSegment(new Point(x+width,y),new Point(x+width/2,y-height)));
            walls.add(new LineSegment(new Point(x+width/2,y-height),new Point(x,y)));
        } else {
            walls.add(new LineSegment(new Point(x+width,y),new Point(x,y)));
            walls.add(new LineSegment(new Point(x+width/2,y-height),new Point(x+width,y)));
            walls.add(new LineSegment(new Point(x,y), new Point(x+width/2,y-height)));
        }
    }
    
    public Ray bounceRay(Ray in,double time)
    {
        // For each of the walls, check to see if the Ray intersects the wall
        Point intersection = null;
        for(int n = 0;n < walls.size();n++)
        {
            
            LineSegment seg = in.toSegment(time);
            intersection = walls.get(n).intersection(seg);
            if(intersection != null)
            {
                if(n==0){
                    score--;
                }
                // If it intersects, find out when
                double t = in.getTime(intersection);
                // Reflect the Ray off the line segment
                Ray newRay = walls.get(n).reflect(seg,in.speed);
                // Figure out where we end up after the reflection.
                Point dest = newRay.endPoint(time-t);
                return new Ray(dest,newRay.v,in.speed);
            }
        }
        return null;
    }
    
    public void move(int deltaX,int deltaY)
    {
        for(int n = 0;n < walls.size();n++)
            walls.get(n).move(deltaX,deltaY);
        x += deltaX;
        y += deltaY;
    }
    
    public boolean contains(Point p) //FIX
    {
        
        if(p.x >= x && p.x <= x + width && p.y <= y + height ){
            if((p.y >= walls.get(1).getYGivenX(p.x)) || (p.y >= walls.get(2).getYGivenX(p.x))){
                //return true;
            }
        }
            
        return false;
    }
    
    public Shape getShape()
    {
        Polygon r = new Polygon();
        r.getPoints().addAll(new Double[]{
            (double)x,(double)y,
            (double)x+width,(double)y,
            (double)x+width/2,(double)y-height
        });
        r.setFill(Color.WHITE);
        r.setStroke(Color.BLACK);
        return r;
    }
    
    public void updateShape()
    {
        Point p1 = walls.get(0).a;
        Point p2 = walls.get(1).a;
        Point p3 = walls.get(2).a;
        Polygon r = new Polygon();
        r.getPoints().clear();
        r.getPoints().addAll(new Double[]{
            p1.x,p1.y,
            p2.x,p2.y,
            p3.x,p3.y
        });
    }
    
}
