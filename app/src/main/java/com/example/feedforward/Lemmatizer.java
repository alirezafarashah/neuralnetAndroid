package com.example.feedforward;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class Lemmatizer {


    public static Lemmatizer instance;
    private HashMap verbs;
    private HashSet<String> words;
    private MainActivity mainActivity;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Lemmatizer(MainActivity mainActivity) throws IOException {

        this("resources/data/words.dat", "resources/data/verbs.dat", true, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Lemmatizer(boolean joinedVerbParts, MainActivity mainActivity) throws IOException {
        this("resources/data/words.dat", "resources/data/verbs.dat", joinedVerbParts, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Lemmatizer(String wordsFile, String verbsFile, MainActivity mainActivity) throws IOException {
        this(wordsFile, verbsFile, true, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Lemmatizer(String wordsFile, String verbsFile, boolean joinedVerbParts, MainActivity mainActivity)
            throws IOException {
        this.words = new HashSet<>();
        InputStream inputStream = mainActivity.getAssets().open("words.dat");
        for (String line : new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF8"))).lines().collect(Collectors.toList())) {
            this.words.add(line.trim());
        }
        WordTokenizer tokenizer = new WordTokenizer(verbsFile,mainActivity);
        inputStream = mainActivity.getAssets().open("verbs.dat");
        List<String> pureVerbs = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF8"))).lines().collect(Collectors.toList());

        this.verbs = new HashMap();
        this.verbs.put("است", "#است");
        for (String verb : pureVerbs) {
            for (String tense : conjugations(verb)) {
                if (!this.verbs.containsKey(tense))
                    this.verbs.put(tense, verb);
            }
        }

        if (joinedVerbParts) {
            for (String verb : pureVerbs) {
                String bon = verb.split("#")[0];
                for (String afterVerb : tokenizer.getAfterVerbs()) {
                    this.verbs.put(bon + "ه " + afterVerb, verb);
                    this.verbs.put("ن" + bon + "ه " + afterVerb, verb);
                }
                for (String beforeVerb : tokenizer.getBeforeVerbs()) {
                    this.verbs.put(beforeVerb + " " + bon, verb);
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Lemmatizer i(MainActivity mainActivity) throws IOException {
        if (instance != null) return instance;
        instance = new Lemmatizer(mainActivity);
        return instance;
    }

    public String lemmatize(String word) {
        return lemmatize(word, "");
    }

    public String lemmatize(String word, String pos) {
        if (pos.length() == 0 && this.words.contains(word))
            return word;

        if ((pos.length() == 0 || pos.equals("V")) && this.verbs.containsKey(word))
            return this.verbs.get(word).toString();

        if (pos.startsWith("AJ") && word.charAt(word.length() - 1) == 'ی')
            return word;

        if (pos.equals("PRO"))
            return word;

        if (this.words.contains(word))
            return word;

        String stem = new Stemmer().stem(word);
        if (this.words.contains(stem))
            return stem;

        return word;
    }

    public List<String> conjugations(String verb) {
        String[] endsList = new String[]{"م", "ی", "", "یم", "ید", "ند"};
        List<String> ends = new ArrayList<>(Arrays.asList(endsList));

        if (verb.equals("#هست")) {
            List<String> conjugate1 = new ArrayList<>();
            List<String> conjugate2 = new ArrayList<>();
            for (String end : ends) {
                conjugate1.add("هست" + end);
                conjugate2.add("نیست" + end);
            }
            conjugate1.addAll(conjugate2);
            return conjugate1;
        }

        HashSet<String> conjugates = new HashSet<>();
        String[] parts = verb.split("#");
        String past = parts[0];
        String present = parts[1];

        for (String end : ends) {
            String conj = past + end;
            String nconj;

            // pastSimples
            conj = getRefinement(conj);
            conjugates.add(conj);
            nconj = getRefinement(getNot(conj));
            conjugates.add(nconj);


            conj = "می‌" + conj;

            // pastImperfects
            conj = getRefinement(conj);
            conjugates.add(conj);
            nconj = getRefinement(getNot(conj));
            conjugates.add(nconj);
        }

        endsList = new String[]{"ه‌ام", "ه‌ای", "ه", "ه‌ایم", "ه‌اید", "ه‌اند"};
        ends = new ArrayList<>(Arrays.asList(endsList));

        // pastNarratives
        for (String end : ends) {
            String conj = past + end;
            conjugates.add(getRefinement(conj));
            conjugates.add(getRefinement(getNot(conj)));
        }

        conjugates.add(getRefinement("ب" + present));
        conjugates.add(getRefinement("ن" + present));

        if (present.endsWith("ا") || Arrays.asList(new String[]{"آ", "گو"}).contains(present))
            present = present + "ی";

        endsList = new String[]{"م", "ی", "د", "یم", "ید", "ند"};
        ends = new ArrayList<>(Arrays.asList(endsList));

        List<String> presentSimples = new ArrayList<>();
        for (String end : ends) {
            String conj = present + end;
            presentSimples.add(conj);

            conjugates.add(getRefinement(conj));
            conjugates.add(getRefinement(getNot(conj)));
        }

        for (String item : presentSimples) {
            String conj;

            // presentImperfects
            conj = "می‌" + item;
            conjugates.add(getRefinement(conj));
            conjugates.add(getRefinement(getNot(conj)));

            // presentSubjunctives
            conj = "ب" + item;
            conjugates.add(getRefinement(conj));

            // presentNotSubjunctives
            conj = "ن" + item;
            conjugates.add(getRefinement(conj));
        }

        return new ArrayList(conjugates);
    }


    //
    // Helper Methods
    //

    private String getRefinement(String text) {
        return text.replace("بآ", "بیا").replace("نآ", "نیا");
    }

    private String getNot(String text) {
        return "ن" + text;
    }
}