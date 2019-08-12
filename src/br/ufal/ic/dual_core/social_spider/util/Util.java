package br.ufal.ic.dual_core.social_spider.util;

import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;

public abstract class Util {
    private final static double TWO_PI = 2 * 3.14159265359;

    public static double[] add(double[] a, double[] b) {
        assert a.length == b.length;

        double[] result = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }

        return result;
    }

    private static double[] add(double n, double[] array) {
        double[] result = new double[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = n + array[i];
        }

        return result;
    }

    public static double[] add(double[] array, double n) {
        return add(n, array);
    }

    public static double[] subtract(double[] a, double[] b) {
        assert a.length == b.length;

        double[] result = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }

    public static double[] multiply(double n, double[] array) {
        double[] result = new double[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = n * array[i];
        }

        return result;
    }

    public static double[] multiply(double[] array, double n) {
        return multiply(n, array);
    }

    public static Coordinate[] polygonVertices(int x, int y, double rad, int npoints) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        double angle = TWO_PI / npoints;
        double a = 0;
        while (a < TWO_PI) {
            double sx = (x + Math.cos(a) * rad);
            double sy = (y + Math.sin(a) * rad);
            a += angle;
            coordinates.add(new Coordinate(Math.floor(sx), Math.floor(sy)));
        }
        coordinates.add(coordinates.get(0));
        return coordinates.toArray(new Coordinate[0]);
    }
}
