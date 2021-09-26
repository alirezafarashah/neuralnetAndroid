package com.example.feedforward;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class FileReader {
    public static MainActivity mainActivity;
    public static HashMap<String, Integer> token2ID;
    public static ArrayList<String> stopWords;
    public static ArrayList<ArrayList<ArrayList<Double>>> weights;
    public static ArrayList<ArrayList<Double>> biases;
    public static HashMap<Integer, ArrayList<Double>> ID2Vectors;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void loadUtils() throws IOException {
        token2ID = readTokens2ID();
        stopWords = loadStopWords();
        weights = getWeights();
        biases = getBiases();
        ID2Vectors = readID2Vectors();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int main(String input) throws IOException {
        return run(preProcess(input));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static ArrayList<Integer> preProcess(String input) throws IOException {
        WordTokenizer tokenizer = new WordTokenizer(mainActivity);
        List<String> words = tokenizer.tokenize(input);
        Stemmer stemmer = new Stemmer();
        Lemmatizer lemmatizer = new Lemmatizer(mainActivity);
        //ArrayList<String> words = new ArrayList<>(Arrays.asList(input.split(" ")));
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<Integer> sentence = new ArrayList<>();
        String stem;
        for (String word : words) {
            if (!stopWords.contains(word)) {
                stem = lemmatizer.lemmatize(word);
                System.out.println(stem);
                if (stem.contains("#")) {
                    stem = stem.split("#")[1];
                }
                tokens.add(stem);
            }
        }
        for (String token : tokens) {
            if (token2ID.containsKey(token)) {
                sentence.add(token2ID.get(token));
            }
        }
        /*
        for (int i = 0; i < 15; i++) {
            if (i < tokens.size() && token2ID.containsKey(tokens.get(i))) {
                sentence.add(token2ID.get(tokens.get(i)));
            } else {
                sentence.add(0);
            }
        }
        */

        return sentence;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int run(ArrayList<Integer> sentence) throws IOException {
        ArrayList<ArrayList<Double>> vectors;
        vectors = getId2Vector2(sentence);
        int numberOfLayers = 2;
        int[] numberOfNeurons = {50, 39};
        ArrayList<Integer> numberOfLayersNeuron = new ArrayList<>();
        numberOfLayersNeuron.add(50);
        numberOfLayersNeuron.add(39);
        //ArrayList<Double> features = getFeatures();
        ArrayList<Double> features = calcStatistics(vectors);

        FeedForward feedForward = new FeedForward(features, weights, biases, numberOfLayersNeuron,
                numberOfLayers, new ActivationFunction());
        double[] probabilities = feedForward.forward();
        int maxIndex = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }

        return maxIndex;
    }


    private static ArrayList<Double> calcStatistics(ArrayList<ArrayList<Double>> vectors) {
        ArrayList<Double> mean = getMeanVector(vectors);
        ArrayList<Double> secondMoment = getMoment(vectors, mean, 2);
        ArrayList<Double> thirdMoment = getMoment(vectors, mean, 3);
        ArrayList<Double> res = new ArrayList<>();
        res.addAll(mean);
        res.addAll(secondMoment);
        res.addAll(thirdMoment);
        return res;

    }

    private static ArrayList<Double> getMeanVector(ArrayList<ArrayList<Double>> vectors) {
        ArrayList<Double> mean = new ArrayList<>();
        int vecSize = vectors.get(0).size();
        for (int i = 0; i < vecSize; i++) {
            double d = 0;
            for (ArrayList<Double> vector : vectors) {
                d += vector.get(i);
            }
            mean.add(d / vectors.size());
        }
        return mean;
    }

    private static ArrayList<Double> getMoment(ArrayList<ArrayList<Double>> vectors, ArrayList<Double> mean, int k) {
        ArrayList<Double> moment = new ArrayList<>();
        int vecSize = vectors.get(0).size();
        for (int i = 0; i < vecSize; i++) {
            double d = 0;
            for (ArrayList<Double> vector : vectors) {
                d += Math.pow(vector.get(i) - mean.get(i), k);
            }
            moment.add(d / vectors.size());
        }
        return moment;
    }


    private static ArrayList<Double> getFeatures() throws IOException {
        InputStream featuresFile = mainActivity.getAssets().open("features.txt");
        Scanner scannerFeatures = new Scanner(featuresFile);
        return lineToArray(scannerFeatures.nextLine());
    }


    private static ArrayList<Double> lineToArray(String line) {
        String[] s = line.split(", ");
        ArrayList<Double> array = new ArrayList<>();
        for (String s1 : s) {
            array.add(Double.parseDouble(s1));
        }
        return array;
    }

    private static ArrayList<Double> lineToArray2(String line) {
        String[] s = line.split(",");
        ArrayList<Double> array = new ArrayList<>();
        for (String s1 : s) {
            array.add(Double.parseDouble(s1));
        }
        return array;
    }

    private static HashMap<String, Integer> readTokens2ID() throws IOException {
        InputStream token2ID = mainActivity.getAssets().open("token2id.txt");
        //File token2ID = new File("token2id.txt");
        HashMap<String, Integer> tokens2ID = new HashMap<>();
        Scanner scannerToken2ID = new Scanner(token2ID);
        String line = scannerToken2ID.nextLine();
        String[] items = line.split(", ");
        for (String s : items) {
            String[] item = s.split(": ");
            String key = item[0].replace("{", "").substring(1, item[0].length() - 1);
            int value = Integer.parseInt(item[1].replace("}", ""));
            tokens2ID.put(key, value);
        }
        scannerToken2ID.close();
        token2ID.close();
        return tokens2ID;

    }

    private static ArrayList<ArrayList<ArrayList<Double>>> getWeights() throws IOException {
        InputStream weightsFile = mainActivity.getAssets().open("weights.txt");
        Scanner scannerWeights = new Scanner(weightsFile);
        ArrayList<ArrayList<ArrayList<Double>>> res = new ArrayList<>();
        String line;
        ArrayList<ArrayList<Double>> weightsOfLayer = new ArrayList<>();
        while (scannerWeights.hasNextLine()) {
            line = scannerWeights.nextLine();
            if (line.equals("end")) {
                res.add(weightsOfLayer);
                weightsOfLayer = new ArrayList<>();
            } else {
                weightsOfLayer.add(lineToArray(line));
            }
        }
        scannerWeights.close();
        weightsFile.close();
        return res;
    }

    private static ArrayList<ArrayList<Double>> getBiases() throws IOException {
        InputStream biasesFile = mainActivity.getAssets().open("biases.txt");
        Scanner scannerBiases = new Scanner(biasesFile);

        ArrayList<Double> b1 = lineToArray(scannerBiases.nextLine());
        ArrayList<Double> b2 = lineToArray(scannerBiases.nextLine());

        ArrayList<ArrayList<Double>> res = new ArrayList<>();
        res.add(b1);
        res.add(b2);
        scannerBiases.close();
        biasesFile.close();
        return res;
    }


    private static HashMap<Integer, ArrayList<Double>> readID2Vectors() throws IOException {
        InputStream vectorFile = mainActivity.getAssets().open("emb3.txt");
        Scanner scannerVectors = new Scanner(vectorFile);
        String line = null;
        int i;
        HashMap<Integer, ArrayList<Double>> result = new HashMap<>();
        while (scannerVectors.hasNextLine()) {
            line = scannerVectors.nextLine();
            i = Integer.parseInt(line.split(":")[0]);
            line = line.split(":")[1];
            result.put(i, lineToArray2(line));

        }
        scannerVectors.close();
        vectorFile.close();
        return result;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static ArrayList<ArrayList<Double>> getId2Vector(ArrayList<Integer> sentence) throws IOException {
        InputStream vectorFile = mainActivity.getAssets().open("emb.txt");
        Scanner scannerVectors = new Scanner(vectorFile);
        ArrayList<ArrayList<Double>> vectors = new ArrayList<>();
        HashMap<Integer, ArrayList<Double>> temp = new HashMap<>();
        int j = 0;
        ArrayList<Integer> sortedSentence = new ArrayList<>();
        Set<Integer> allIDs = new HashSet<>();
        allIDs.addAll(sentence);
        sortedSentence.addAll(allIDs);
        Collections.sort(sortedSentence);
        String line = null;
        for (int i = 0; i <= sortedSentence.get(sortedSentence.size() - 1); i++) {
            line = scannerVectors.nextLine();
            if (i == sortedSentence.get(j)) {
                temp.put(i, lineToArray2(line));
                j++;
            }
        }
        scannerVectors.close();
        vectorFile.close();
        for (Integer id : sentence) {
            vectors.add(temp.get(id));
        }
        return vectors;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static ArrayList<ArrayList<Double>> getId2Vector2(ArrayList<Integer> sentence) throws IOException {
        ArrayList<ArrayList<Double>> vectors = new ArrayList<>();
        for (Integer id : sentence) {
            if (ID2Vectors.containsKey(id)) {
                vectors.add(ID2Vectors.get(id));
            } else {
                vectors.add(ID2Vectors.get(0));
            }
        }
        return vectors;

    }

    private static ArrayList<String> loadStopWords() throws IOException {
        InputStream stopWords = mainActivity.getAssets().open("stopwords.txt");
        ArrayList<String> stopWordsArray = new ArrayList<>();
        Scanner scannerStopWords = new Scanner(stopWords);
        while (scannerStopWords.hasNextLine()) {
            stopWordsArray.add(scannerStopWords.nextLine());
        }
        stopWords.close();
        scannerStopWords.close();
        return stopWordsArray;
    }

}
