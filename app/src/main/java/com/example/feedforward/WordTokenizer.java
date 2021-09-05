package com.example.feedforward;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class WordTokenizer {
    public static WordTokenizer instance;
    List<String> verbs;
    private boolean joinVerbParts = true;
    private HashSet<String> beforeVerbs;
    private HashSet<String> afterVerbs;
    private RegexPattern pattern;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public WordTokenizer(MainActivity mainActivity) throws IOException {
        this(true, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public WordTokenizer(boolean joinVerbParts, MainActivity mainActivity) throws IOException {
        this("resources/data/verbs.dat", joinVerbParts, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public WordTokenizer(String verbsFile, MainActivity mainActivity) throws IOException {
        this(verbsFile, true, mainActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public WordTokenizer(String verbsFile, boolean joinVerbParts, MainActivity mainActivity)
            throws IOException {
        this.joinVerbParts = joinVerbParts;
        this.pattern = new RegexPattern("([؟!\\?]+|[:\\.،؛»\\]\\)\\}\"«\\[\\(\\{])", " $1 ");

        if (this.joinVerbParts) {
            String[] tokens;

            tokens = new String[]{
                    "ام", "ای", "است", "ایم", "اید", "اند",
                    "بودم", "بودی", "بود", "بودیم", "بودید", "بودند",
                    "باشم", "باشی", "باشد", "باشیم", "باشید", "باشند",
                    "شده ام", "شده ای", "شده است", "شده ایم", "شده اید", "شده اند",
                    "شده بودم", "شده بودی", "شده بود", "شده بودیم", "شده بودید", "شده بودند",
                    "شده باشم", "شده باشی", "شده باشد", "شده باشیم", "شده باشید", "شده باشند",
                    "نشده ام", "نشده ای", "نشده است", "نشده ایم", "نشده اید", "نشده اند",
                    "نشده بودم", "نشده بودی", "نشده بود", "نشده بودیم", "نشده بودید", "نشده بودند",
                    "نشده باشم", "نشده باشی", "نشده باشد", "نشده باشیم", "نشده باشید", "نشده باشند",
                    "شوم", "شوی", "شود", "شویم", "شوید", "شوند",
                    "شدم", "شدی", "شد", "شدیم", "شدید", "شدند",
                    "نشوم", "نشوی", "نشود", "نشویم", "نشوید", "نشوند",
                    "نشدم", "نشدی", "نشد", "نشدیم", "نشدید", "نشدند",
                    "می‌شوم", "می‌شوی", "می‌شود", "می‌شویم", "می‌شوید", "می‌شوند",
                    "می‌شدم", "می‌شدی", "می‌شد", "می‌شدیم", "می‌شدید", "می‌شدند",
                    "نمی‌شوم", "نمی‌شوی", "نمی‌شود", "نمی‌شویم", "نمی‌شوید", "نمی‌شوند",
                    "نمی‌شدم", "نمی‌شدی", "نمی‌شد", "نمی‌شدیم", "نمی‌شدید", "نمی‌شدند",
                    "خواهم شد", "خواهی شد", "خواهد شد", "خواهیم شد", "خواهید شد", "خواهند شد",
                    "نخواهم شد", "نخواهی شد", "نخواهد شد", "نخواهیم شد", "نخواهید شد", "نخواهند شد"
            };

            this.afterVerbs = new HashSet<>(Arrays.asList(tokens));

            tokens = new String[]{
                    "خواهم", "خواهی", "خواهد", "خواهیم", "خواهید", "خواهند",
                    "نخواهم", "نخواهی", "نخواهد", "نخواهیم", "نخواهید", "نخواهند"
            };

            this.beforeVerbs = new HashSet<>(Arrays.asList(tokens));
            InputStream inputStream = mainActivity.getAssets().open("verbs.dat");
            this.verbs = new BufferedReader(new InputStreamReader(inputStream,
                    Charset.forName("UTF8"))).lines().collect(Collectors.toList());
            Collections.reverse(this.verbs);
            for (int i = 0; i < this.verbs.size(); i++) {
                String verb = this.verbs.get(i);
                this.verbs.set(i, verb.trim().split("#")[0] + "ه");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static WordTokenizer i(MainActivity mainActivity) throws IOException {
        if (instance != null) return instance;
        instance = new WordTokenizer(mainActivity);
        return instance;
    }

    public HashSet<String> getBeforeVerbs() {
        return beforeVerbs;
    }

    public HashSet<String> getAfterVerbs() {
        return afterVerbs;
    }

    public List<String> getVerbs() {
        return verbs;
    }

    public List<String> tokenize(String sentence) {
        sentence = this.pattern.apply(sentence).trim();
        List<String> tokens = Arrays.asList(sentence.split(" +"));
        if (this.joinVerbParts)
            tokens = this.joinVerbParts(tokens);
        return tokens;
    }

    private List<String> joinVerbParts(List<String> tokens) {
        Collections.reverse(tokens);
        List<String> newTokens = new ArrayList<>();

        for (String token : tokens) {
            if (newTokens.size() > 0) {
                String lastWord = newTokens.get(newTokens.size() - 1);
                if (this.beforeVerbs.contains(token) ||
                        (this.afterVerbs.contains(lastWord) && this.verbs.contains(token))) {
                    lastWord = token + " " + lastWord;
                    newTokens.set(newTokens.size() - 1, lastWord);
                } else
                    newTokens.add(token);
            } else
                newTokens.add(token);
        }

        Collections.reverse(newTokens);
        return newTokens;
    }
}