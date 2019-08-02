package br.ufal.ic.dual_core.social_spider.util;

public abstract class Util {
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
}
