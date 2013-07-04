package org.alogvinov.affprop.test;

import java.util.Scanner;

public class TestSim {

    private static class Point {
        
        double x;
        double y;

        public Point(String point) {
            Scanner s = new Scanner(point).useDelimiter(",");
            x = s.nextDouble();
            y = s.nextDouble();
        }
        
    }

    public static double point2dSim(String point1str, String point2str) {
        Point point1 = new Point(point1str);
        Point point2 = new Point(point2str);
        double dx = point1.x - point2.x;
        double dy = point1.y - point2.y;
        return -Math.sqrt(dx * dx + dy * dy);
    }

    public static double testSimExclude(String p1, String p2) {
        double s = point2dSim(p1, p2);
        return s < -10 ? Double.NEGATIVE_INFINITY : s; 
    }

}

