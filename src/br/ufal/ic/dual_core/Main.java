package br.ufal.ic.dual_core;

import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import br.ufal.ic.dual_core.social_spider.Colony;
import br.ufal.ic.dual_core.social_spider.util.Util;
import javafx.util.Pair;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class Main extends JPanel{
    private static final double xMax = 625, yMax = 625;
    private static GeometryFactory geoFactory = new GeometryFactory();
    private static Geometry city;
    private Polygon container;
    private static List<Geometry> baseStations = new ArrayList<>();
    private static int useCity = 1;

    // making cities
    private void setup() {
        Point[] hexagons1 = {
                new Point(219, 287), new Point(219, 393),
                new Point(312, 234), new Point(312, 340), new Point(312, 448),
                new Point(405, 287), new Point(405, 393)
        };
        Point[] hexagons2 = {
                new Point(127, 124), new Point(127, 231), new Point(127, 340), new Point(127, 448),
                new Point(220, 178), new Point(220, 286), new Point(220, 394), new Point(220, 502),
                new Point(312, 124), new Point(312, 231), new Point(312, 340), new Point(312, 448),
                new Point(406, 178), new Point(406, 286), new Point(406, 394), new Point(406, 502),
                new Point(499, 124), new Point(499, 231), new Point(499, 340), new Point(499, 448)
        };
        Point[] hexagons3 = {
                new Point(127, 231), new Point(127, 340), new Point(127, 448),
                new Point(220, 178), new Point(220, 286), new Point(220, 394),
                new Point(312, 124), new Point(312, 231),
                new Point(406, 178), new Point(406, 286), new Point(406, 394),
                new Point(499, 231), new Point(499, 340), new Point(499, 448)
        };
        Point[] hexagonsCenters = useCity == 1 ? hexagons1 : useCity == 2 ? hexagons2 : hexagons3;
        ArrayList<Polygon> hexagons = new ArrayList<>();
        for (Point p : hexagonsCenters) {
            Coordinate[] cors = Util.polygonVertices(p.x, p.y, 63, 6);
            hexagons.add(geoFactory.createPolygon(cors));
        }

        city = hexagons.get(0);
        for (int i = 1; i < hexagons.size(); i++) {
            city = city.union(hexagons.get(i));
        }

        container = geoFactory.createPolygon(new Coordinate[]{new Coordinate(0, 0), new Coordinate(xMax, 0), new Coordinate(xMax, yMax), new Coordinate(0, yMax), new Coordinate(0, 0)});
    }

    @Override
    public void paint(Graphics g) {
        ShapeWriter sw = new ShapeWriter();
        Shape linShape = sw.toShape(container.difference(city));
        ((Graphics2D) g).setPaint(new Color(0, 130, 0));
        ((Graphics2D) g).fill(linShape);

        linShape = sw.toShape(city);
        ((Graphics2D) g).setPaint(new Color(190, 190, 190));
        ((Graphics2D) g).fill(linShape);

        ((Graphics2D) g).setPaint(Color.WHITE);
        baseStations.forEach((Geometry s) -> {
            if (s != null) {
                this.drawStation((Graphics2D) g, sw, s);
            }
        });
    }

    private void drawStation(Graphics2D g, ShapeWriter sw, Geometry s) {
        Shape circle = sw.toShape(s), center = sw.toShape(s.getCentroid());
        g.draw(circle);
        g.draw(center);
    }

    private static Geometry createCircle(Coordinate coordinate, double radius) {
        GeometricShapeFactory shape = new GeometricShapeFactory(geoFactory);
        shape.setCentre(coordinate);
        shape.setSize(2 * radius);
        shape.setNumPoints(32);
        return shape.createCircle();
    }

    private static double fitness(Geometry tot) {
        if (tot == null)
            return 0.0;
        return tot.intersection(city).getArea() / city.getArea();
    }

    public static void main(String[] args) {
        List<Pair<Double, Double>> ranges = new ArrayList<>();
        JFrame frame = new JFrame();
        Main main = new Main();
        main.setup();
        // There are K base stations, ranges.size has the value 2 * K
        /*
        * 1st instance: K = 7; R = 63
        * 2nd instance: K = 3; R = 170
        * 3nd instance: K = 6; R = 126
        */
        for (int k = 0; k < (useCity == 1 ? 7 : useCity == 2 ? 3 : 6); k++) {
            ranges.add(new Pair<>(0.0, xMax));
            ranges.add(new Pair<>(0.0, yMax));
        }

        /*JFRAME CONFIG*/
        frame.setTitle("Base Station Placement");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(main);
        frame.setSize(631, 654);
        frame.setVisible(true);

        AtomicReference<Double> bestFitness = new AtomicReference<>((double) 0);
        AtomicReference<Double> oldFitness = new AtomicReference<>((double) 0);
        AtomicReference<Integer> generation = new AtomicReference<>(0);

        Colony colony = new Colony(25, ranges,
                (values) -> { // Fitness
                    double fitness;
                    Geometry tot = null;
                    for (int i = 0; i < values.length; i+=2) {
                        Coordinate coord = new Coordinate(values[i], values[i+1]);
                        Geometry newStation = createCircle(coord, useCity == 1 ? 63 : useCity == 2 ? 170 : 126);
                        if (tot == null)
                            tot = new GeometryFactory().createGeometry(newStation);
                        else
                            tot = tot.union(newStation);
                    }
                    fitness = fitness(tot);

                    if (bestFitness.get() < fitness) {
                        bestFitness.set(fitness);
                    }

                    return fitness;
                },
                (values) -> { // Decode
                    if (bestFitness.get() > oldFitness.get()) {
                        oldFitness.set(bestFitness.get());
                        System.out.println("Generation: " + generation.get());
                        System.out.println("Fitness: " + bestFitness.get());
                        System.out.println(Arrays.toString(values));
                        baseStations.clear();
                        for (int i = 0; i < values.length; i+=2) {
                            baseStations.add(createCircle(new Coordinate(values[i], values[i+1]), useCity == 1 ? 63 : useCity == 2 ? 170 : 126));
                        }
                        frame.getContentPane().repaint();
                    }
                }
        );

        while (true) {
            generation.set(generation.get()+1);
            colony.nextGeneration();
        }
    }
}
