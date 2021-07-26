package com.example.feedforward;

import java.util.ArrayList;
import java.util.Collections;

public class FeedForward {
    private ArrayList<Double> features;
    private final ArrayList<ArrayList<ArrayList<Double>>> weights;
    private final ArrayList<Integer> numberOfLayersNeuron;
    private final int numberOfLayers;
    private final ActivationFunction af;

    FeedForward(ArrayList<Double> features, ArrayList<ArrayList<ArrayList<Double>>> weights,
                ArrayList<Integer> numberOfLayersNeuron, int numberOfLayers, ActivationFunction af) {
        this.features = features;
        this.weights = weights;
        this.numberOfLayers = numberOfLayers;
        this.numberOfLayersNeuron = numberOfLayersNeuron;
        this.af = af;

    }

    public double[] forward() {
        ArrayList<Double> nextFeature = new ArrayList<>();
        for (int i = 0; i < numberOfLayers; i++) {
            for (int j = 0; j < numberOfLayersNeuron.get(i); j++) {
                ArrayList<Double> weightsOfLayer = weights.get(i).get(j);
                double neuronValue = 0;
                int k = 0;
                for (Double feature : features) {
                    neuronValue = neuronValue + (feature * weightsOfLayer.get(k));
                    k++;
                }
                nextFeature.add(af.activation(neuronValue));
            }
            features.clear();
            System.gc();
            features.addAll(nextFeature);
            nextFeature.clear();
            System.gc();

        }
        return softMax(features);
    }

    public static double[] softMax(ArrayList<Double> arr) {
        int length = arr.size();
        double max = Collections.max(arr);
        double[] exp_a = new double[arr.size()];
        for (int i = 0; i < length; i++) {
            exp_a[i] = Math.pow(Math.E, arr.get(i) - max);
        }
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += exp_a[i];
        }
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = exp_a[i] / sum;
        }
        return result;
    }

}
