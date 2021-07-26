package com.example.feedforward;
import java.util.ArrayList;

public class Main {
    public static void main(int numberOfLayers , int neurons) {
        ActivationFunction af = new ActivationFunction();
        ArrayList<Double> features = new ArrayList<>();
        for (int i = 0; i < neurons; i++) {
            features.add(Math.random());
        }
        ArrayList<Integer> numberOfLayersNeuron = new ArrayList<>();
        for (int i = 0; i < numberOfLayers; i++) {
            numberOfLayersNeuron.add(neurons);
        }
        ArrayList<ArrayList<ArrayList<Double>>> weights = new ArrayList<>();
        for (int i = 0; i < numberOfLayers; i++) {
            ArrayList<ArrayList<Double>> layerWeights = new ArrayList<>();
            for (int j = 0; j < numberOfLayersNeuron.get(i); j++) {
                ArrayList<Double> temp = new ArrayList<>();
                for (int k = 0; k < neurons; k++) {
                    temp.add(Math.random() * 2 + -1);
                }
                layerWeights.add(temp);
            }
            weights.add(layerWeights);
        }
        FeedForward feedForward = new FeedForward(features, weights, numberOfLayersNeuron, numberOfLayers, af);
        feedForward.forward();

    }
}
