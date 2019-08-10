package br.ufal.ic.dual_core;

import br.ufal.ic.dual_core.social_spider.Colony;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Pair<Double, Double>> ranges = new ArrayList<>();

        ranges.add(new Pair<>(-1.0, 1.0));
        ranges.add(new Pair<>(-1.0, 1.0));
        ranges.add(new Pair<>(-1.0, 1.0));

        Colony colony = new Colony(25, ranges,
                (values) -> Math.random(), // Fitness
                (values) -> System.out.println(Arrays.toString(values)) // Decode
        );

        colony.nextGeneration(); // Call this as many times as needed
    }
}
