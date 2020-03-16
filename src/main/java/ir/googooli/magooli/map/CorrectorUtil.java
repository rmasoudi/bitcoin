package ir.googooli.magooli.map;

import java.util.*;
import java.util.regex.Pattern;

public class CorrectorUtil {


    private static Pattern SEP_PATTERN = Pattern.compile("[ادذرزژ,و]");

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("تهران خیابان سردارجنگل کوچه عادل شرقی");
        Object o = correctFullText(list);
        System.out.println(o);

    }

    public static Set<String> correctTerms(String address) {
        Set<String> results=new HashSet<>();
        String[] split = address.split(" ");
        Set<String>[] varArray = new Set[split.length];
        List<Integer> preIndices = new ArrayList<>();
        List<Integer> postIndices = new ArrayList<>();
        List<Integer> otherIndices = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {  //سردارجنگل سردار جنگل
            String word = split[i];
            Set<String> variations = new HashSet<>();
            if (COMMON_POSTFIX.contains(word)) {
                variations.add(word);
                postIndices.add(i);
            } else if (COMMON_PREFIX.contains(word)) {
                variations.add(word);
                preIndices.add(i);
            } else {
                variations = getWordVariations(word);
                otherIndices.add(i);
            }
            varArray[i] = variations;
        }

        for (Integer index : preIndices) {//خیابانسردار
            if (index < split.length - 1) {
                Set<String> candidates = varArray[index + 1];
                for (String candidate : candidates) {
                    results.add(split[index] + candidate);
                }
            }
        }
        for (Integer index : postIndices) {//عادلشرقی
            if (index > 0) {
                Set<String> candidates = varArray[index - 1];
                for (String candidate : candidates) {
                    results.add(candidate + split[index]);
                }

            }
        }
        for (Integer index : otherIndices) {
            Set<String> candidates = varArray[index];
            results.addAll(candidates);
        }
        for (int i = 0; i < split.length - 1; i++) {  //سردارجنگل from سردار جنگل
            results.add(split[i] + split[i + 1]);
        }
        return results;
    }

    private static Set<String> getWordVariations(String word) {
        List<Integer> indices = getSplitIndices(word);
        Set<String> wordSamples = new HashSet<>();
        wordSamples.add(word);
        for (Integer index : indices) {
            String s = splitAt(word, index);
            wordSamples.add(s.trim());
        }
        return wordSamples;
    }

    private static List<Integer> getSplitIndices(String word) {
        String s = SEP_PATTERN.matcher(word).replaceAll("!");
        List<Integer> indices = new ArrayList<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '!') {
                indices.add(i);
            }
        }
        return indices;
    }

    private static String splitAt(String text, int index) {
        String part1 = text.substring(0, index + 1);
        String part2 = text.substring(index + 1);
        return part1 + " " + part2;
    }

    public static String getText(Object o) {
        if (o instanceof String) {
            return o.toString();
        } else {
            ArrayList<String> list = (ArrayList<String>) o;
            String maxText = null;
            int maxLength = 0;
            for (String s : list) {
                if (s.length() > maxLength) {
                    maxLength = s.length();
                    maxText = s;
                }
            }
            return maxText;
        }
    }

    private static final List<String> COMMON_PREFIX = Arrays.asList("کوچه", "خیابان", "بنبست", "بزرگراه", "آزادراه", "میدان");
    private static final List<String> COMMON_POSTFIX = Arrays.asList("شرقی", "غربی", "شمالی", "جنوبی");

    public static Object correctFullText(Object o) {
        String text = getText(o);
        Set<String> terms = correctTerms(text);
        String address = createAddress(terms);
        terms.addAll(getFixTerms(address));
        return createAddress(terms);
    }

    private static Set<String> getFixTerms(String text) {
        String[] split = text.split(" ");
        Set<String> result = new HashSet<>();
        for (int i = 0; i < split.length; i++) {
            if (COMMON_PREFIX.contains(split[i]) && i < split.length - 1) {
                result.add(split[i] + split[i + 1]);
            } else if (COMMON_POSTFIX.contains(split[i]) && i > 0) {
                result.add(split[i - 1] + split[i]);
            }
        }
        return result;
    }

    private static String createAddress(Set<String> terms) {
        StringBuilder acc = new StringBuilder();
        for (String s : terms) {
            acc.append(s).append(" ");
        }
        return acc.toString().trim();
    }
}
