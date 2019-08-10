package br.ufal.ic.dual_core.social_spider;

import br.ufal.ic.dual_core.social_spider.util.Util;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Colony {
    private int N, Nf, Nm;
    private double rangeOfMating, PF = 0.7;
    private SocialSpider[] spiders;
    private FitnessFunction fitness;
    private DecoderFunction decoder;

    public Colony(int N, List<Pair<Double, Double>> minMaxRanges, FitnessFunction fitnessFunc, DecoderFunction decoderFunc) {
        this.N = N;
        spiders = new SocialSpider[N];
        fitness = fitnessFunc;
        decoder = decoderFunc;

        // Step 1
        Nf = (int) Math.floor((0.9 - Math.random() * 0.25) * N);
        Nm = N - Nf;

        // Step 2
        double sum = 0;
        for (Pair<Double, Double> range : minMaxRanges) {
            double pLow = range.getKey();
            double pHigh = range.getValue();

            sum += pHigh - pLow;
        }
        rangeOfMating = sum / (2 * minMaxRanges.size());

        for (int i = 0; i < N; i++) {
            spiders[i] = new SocialSpider(minMaxRanges.size());

            for (int j = 0; j < minMaxRanges.size(); j++) {
                double pLow = minMaxRanges.get(j).getKey();
                double pHigh = minMaxRanges.get(j).getValue();

                spiders[i].values[j] = pLow + Math.random() * (pHigh - pLow);
            }
        }

        // Step 3
        calculateWeights(true);
    }

    public void nextGeneration() {
        // Step 3
        calculateWeights(false);

        // Step 4
        SocialSpider[] females = moveFemaleSpiders();

        // Step 5
        SocialSpider[] males = moveMaleSpiders();

        // Step 4 + 5
        System.arraycopy(females, 0, spiders, 0, Nf);
        System.arraycopy(males, 0, spiders, Nf, Nm);

        // Step 6
        copulateSpiders();

        // Decode
        SocialSpider bestSpider = getSpiderB();
        decoder.decode(bestSpider.values);
    }

    private void calculateWeights(boolean calculateFitness) {
        assert spiders.length > 0;

        final double firstFitness = calculateFitness ? fitness.calculate(spiders[0].values) : spiders[0].fitness;
        double bestS = firstFitness, worstS = firstFitness;

        for (SocialSpider spider : spiders) {
            if (calculateFitness) {
                spider.fitness = fitness.calculate(spider.values);
            }

            if (spider.fitness > bestS) {
                bestS = spider.fitness;
            } else if (spider.fitness < worstS) {
                worstS = spider.fitness;
            }
        }

        for (SocialSpider spider : spiders) {
            spider.weight = (spider.fitness - worstS) / (bestS - worstS);
            assert !Double.isNaN(spider.weight);
        }
    }

    private int getWorstSpiderIdx() {
        assert spiders.length > 0;

        int worstIdx = 0;
        double worstFitness = spiders[0].fitness;

        for (int i = 1; i < N; i++) {
            if (spiders[i].fitness < worstFitness) {
                worstFitness = spiders[i].fitness;
                worstIdx = i;
            }
        }

        return worstIdx;
    }

    private SocialSpider getSpiderC(int i) {
        SocialSpider spiderC = spiders[i];
        double shortestDistance = Double.MAX_VALUE;

        for (int c = 0; c < N; c++) {
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
        double shortestDistance = spiders[i].euclidianDistance(spiders[0]);

        for (int f = 1; f < Nf; f++) {
            double distance = spiders[i].euclidianDistance(spiders[f]);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                spiderF = spiders[f];
            }
        }

        return spiderF;
    }

    private SocialSpider getMedianMale() {
        assert spiders.length > 0;

        SocialSpider[] males = Arrays.copyOfRange(spiders, Nf, N);
        assert males.length == Nm;

        Arrays.sort(males);
        int m = (males.length - 1) / 2;
        return males[m];
    }

    private SocialSpider[] moveFemaleSpiders() {
        SocialSpider[] newFemales = new SocialSpider[Nf];

        SocialSpider spiderB = getSpiderB();

        for (int i = 0; i < Nf; i++) {
            newFemales[i] = new SocialSpider(spiders[0].n);

            SocialSpider spiderC = getSpiderC(i);
            double distanceC = spiders[i].euclidianDistance(spiderC);
            double vibC = spiderC.weight * Math.pow(Math.E, -distanceC * distanceC);

            double distanceB = spiders[i].euclidianDistance(spiderB);
            double vibB = spiderB.weight * Math.pow(Math.E, -distanceB * distanceB);

            double alpha = Math.random(), beta = Math.random(), delta = Math.random(), rand = Math.random();

            double[] alphaOp = Util.multiply(alpha * vibC, Util.subtract(spiderC.values, spiders[i].values));
            double[] betaOp = Util.multiply(beta * vibB, Util.subtract(spiderB.values, spiders[i].values));

            if (Math.random() < PF) {
                newFemales[i].values =
                        Util.add(
                                Util.add(
                                        Util.add(
                                                spiders[i].values,
                                                alphaOp),
                                        betaOp),
                                delta * (rand - 0.5)
                        );
            } else {
                newFemales[i].values =
                        Util.add(
                                Util.subtract(
                                        Util.subtract(
                                                spiders[i].values,
                                                alphaOp),
                                        betaOp),
                                delta * (rand - 0.5)
                        );
            }

            newFemales[i].fitness = fitness.calculate(newFemales[i].values);
        }

        return newFemales;
    }

    private SocialSpider[] moveMaleSpiders() {
        SocialSpider[] newMales = new SocialSpider[Nm];

        SocialSpider medianMale = getMedianMale();

        double[] dividend = new double[spiders[0].n];
        double divisor = 0;
        for (int h = Nf; h < N; h++) {
            divisor += spiders[h].weight;

            dividend = Util.add(dividend, Util.multiply(spiders[h].values, spiders[h].weight));
        }
        double[] weightedSumOfWeights = Util.multiply(dividend, 1 / divisor);

        for (int i = 0; i < Nm; i++) {
            newMales[i] = new SocialSpider(spiders[0].n);

            double alpha = Math.random();

            if (spiders[Nf + i].weight > medianMale.weight) {
                SocialSpider spiderF = getSpiderF(Nf + i);
                double distanceF = spiders[Nf + i].euclidianDistance(spiderF);
                double vibF = spiderF.weight * Math.pow(Math.E, -distanceF * distanceF);

                double delta = Math.random(), rand = Math.random();

                double[] alphaOp = Util.multiply(alpha * vibF, Util.subtract(spiderF.values, spiders[Nf + i].values));

                newMales[i].values =
                        Util.add(
                                Util.add(
                                        spiders[Nf + i].values,
                                        alphaOp
                                ),
                                delta * (rand - 0.5)
                        );
            } else {
                newMales[i].values =
                        Util.add(
                                spiders[Nf + i].values,
                                Util.multiply(
                                        alpha,
                                        Util.subtract(
                                                weightedSumOfWeights,
                                                spiders[Nf + i].values
                                        )
                                )
                        );
            }

            newMales[i].fitness = fitness.calculate(newMales[i].values);
        }

        return newMales;
    }

    private List<SocialSpider> findFemalesToCopulate(SocialSpider male) {
        List<SocialSpider> females = new ArrayList<>();

        for (int i = 0; i < Nf; i++) {
            double distance = male.euclidianDistance(spiders[i]);
            if (distance < rangeOfMating) {
                females.add(spiders[i]);
            }
        }

        return females;
    }

    private double[] getRouletteWheelProbabilities(SocialSpider[] adults) {
        double[] probabilities = new double[adults.length];

        double sum = 0;
        for (SocialSpider s : adults) {
            sum += s.fitness;
        }

        for (int i = 0; i < adults.length; i++) {
            probabilities[i] = adults[i].fitness / sum;
        }

        return probabilities;
    }

    private void copulateSpiders() {
        SocialSpider medianMale = getMedianMale();

        for (int i = Nf; i < N; i++) {
            if (spiders[i].fitness > medianMale.fitness) {
                List<SocialSpider> females = findFemalesToCopulate(spiders[i]);

                if (females.size() > 0) {
                    females.add(spiders[i]);
                    SocialSpider[] adults = females.toArray(new SocialSpider[0]);
                    SocialSpider sNew = new SocialSpider(adults[0].n);

                    double[] probabilities = getRouletteWheelProbabilities(adults);

                    for (int j = 0; j < sNew.n; j++) {
                        double rand = Math.random(), accumulatedProb = 0;
                        for (int s = 0; s < adults.length; s++) {
                            accumulatedProb += probabilities[s];

                            if (accumulatedProb >= rand) {
                                sNew.values[j] = adults[s].values[j];
                                break;
                            }
                        }
                    }

                    sNew.fitness = fitness.calculate(sNew.values);

                    int worstIdx = getWorstSpiderIdx();
                    if (sNew.fitness > spiders[worstIdx].fitness) {
                        spiders[worstIdx] = sNew;
                    }
                }
            }
        }
    }
}
