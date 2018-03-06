package physics;

import java.io.Serializable;

/* A simple Point class. **/
public class Point implements Serializable{
    public double x;
    public double y;
    
    public Point(double x,double y)
    {
        this.x = x;
        this.y = y;
    }

}
