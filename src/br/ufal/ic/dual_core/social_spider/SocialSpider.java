package br.ufal.ic.dual_core.social_spider;

public class SocialSpider implements Comparable<SocialSpider> {
    int n;
    double[] values;
    double fitness, weight;

    SocialSpider(int n) {
        this.n = n;
        this.values = new double[n];
    }

    double euclidianDistance(SocialSpider other) {
        assert values.length == other.values.length;

        double distance = 0;

        for (int j = 0; j < n; j++) {
            double diff = values[j] - other.values[j];
            distance += diff * diff;
        }

        return Math.sqrt(distance);
    }

    @Override
    public int compareTo(SocialSpider other) {
        return Double.compare(fitness, other.fitness);
    }
}
