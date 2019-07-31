package br.ufal.ic.dual_core.social_spider;

import javafx.util.Pair;

import java.util.List;

public class Colony {
    private int N, Nf, Nm;
    private SocialSpider[] spiders;
    private List<Pair<Double, Double>> ranges;
    private FitnessFunction fitness;

    public Colony(int N, List<Pair<Double, Double>> minMaxRanges, FitnessFunction fitnessFunc) {
        this.N = N;
        spiders = new SocialSpider[N];
        ranges = minMaxRanges;
        fitness = fitnessFunc;

        // Step 1
        Nf = (int) Math.floor((0.9 - Math.random() * 0.25) * N);
        Nm = N - Nf;

        // Step 2
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < minMaxRanges.size(); j++) {
                double pLow = minMaxRanges.get(j).getKey();
                double pHigh = minMaxRanges.get(j).getValue();

                spiders[i].values[j] = pLow + Math.random() * (pHigh - pLow);
            }
        }

        // Step 3
        calculateWeights();
    }

    public void nextGeneration() {
        // Step 3
        calculateWeights();

        // Step 4
        moveFemaleSpiders();
    }

    private void calculateWeights() {
        assert spiders.length > 0;

        final double firstFitness = fitness.calculate(spiders[0].values);
        double bestS = firstFitness, worstS = firstFitness;

        for (SocialSpider spider : spiders) {
            spider.fitness = fitness.calculate(spider.values);

            if (spider.fitness > bestS) {
                bestS = spider.fitness;
            } else if (spider.fitness < worstS) {
                worstS = spider.fitness;
            }
        }

        for (SocialSpider spider : spiders) {
            spider.weight = (spider.fitness - worstS) / (bestS - worstS);
        }
    }

    private SocialSpider getSpiderC(int i) {
        SocialSpider spiderC = spiders[i];
        double shortestDistance = Double.MAX_VALUE;

        for (int c = 0; c < N; c++) {
            if (c == i) {
                continue;
            }

            if (spiders[c].weight > spiders[i].weight) {
                double distance = spiders[i].euclidianDistance(spiders[c]);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    spiderC = spiders[c];
                }
            }
        }

        return spiderC;
    }

    private SocialSpider getSpiderB() {
        assert spiders.length > 0;

        SocialSpider spiderB = spiders[0];
        double bestFitness = spiders[0].fitness;

        for (int b = 1; b < N; b++) {
            if (spiders[b].fitness > bestFitness) {
                bestFitness = spiders[b].fitness;
                spiderB = spiders[b];
            }
        }

        return spiderB;
    }

    private SocialSpider getSpiderF(int i) {
        assert spiders.length > 0;

        SocialSpider spiderF = spiders[0];
        double shortestDistance = Double.MAX_VALUE;

        for (int f = 1; f < Nf; f++) {
            double distance = spiders[i].euclidianDistance(spiders[f]);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                spiderF = spiders[f];
            }
        }

        return spiderF;
    }

    private void moveFemaleSpiders() {
        for (int i = 0; i < Nf; i++) {

        }
    }
}
