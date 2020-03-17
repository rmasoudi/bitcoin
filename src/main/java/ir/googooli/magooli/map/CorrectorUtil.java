package ir.googooli.magooli.map;

import java.util.*;
import java.util.regex.Pattern;

public class CorrectorUtil {


    private static Pattern SEP_PATTERN = Pattern.compile("[ادذرزژ,و]");
    private static final List<String> COMMON_PREFIX = Arrays.asList("کوچه", "خیابان", "بنبست", "بزرگراه", "آزادراه", "میدان", "جاده", "چهارراه", "فلکه", "بلوار", "سهراه");
    private static final List<String> COMMON_POSTFIX = Arrays.asList("شرقی", "غربی", "شمالی", "جنوبی");
    private static final List<String> COMMON_FIX = Arrays.asList("شرقی", "غربی", "شمالی", "جنوبی", "کوچه", "خیابان", "بنبست", "بزرگراه", "آزادراه", "میدان", "جاده", "چهارراه", "فلکه", "بلوار", "سهراه");
    private static final Pattern COMMON_FIX_PATTERN = Pattern.compile("شرقی|غربی|شمالی|جنوبی|کوچه|خیابان|بنبست|بزرگراه|آزادراه|میدان|جاده|چهارراه|فلکه|بلوار|سهراه");


    private static final Pattern BON_BAST = Pattern.compile("بن بست");
    private static final Pattern BOZORG_RAH = Pattern.compile("بزرگ راه");
    private static final Pattern AZAR_RAH = Pattern.compile("آزاد راه");
    private static final Pattern CHAHAR_RAH = Pattern.compile("چهار راه|چارراه");
    private static final Pattern SE_RAH = Pattern.compile("سه راه");
    private static final Pattern SE_RAHI = Pattern.compile("سه راهی");
    private static final Pattern BOLVAR = Pattern.compile("بولوار");

    private static String normalize(String text) {
        text = BON_BAST.matcher(text).replaceAll("بنبست");
        text = BOZORG_RAH.matcher(text).replaceAll("بزرگراه");
        text = AZAR_RAH.matcher(text).replaceAll("آزادراه");
        text = CHAHAR_RAH.matcher(text).replaceAll("چهارراه");
        text = SE_RAHI.matcher(text).replaceAll("سهراه");
        text = SE_RAH.matcher(text).replaceAll("سهراه");
        text = BOLVAR.matcher(text).replaceAll("بلوار");
        return text;
    }


    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("تهران خیابان سردار جنگل شمالی میدان نبوت تجریش فدک گوگولی مگولی شمالی");
        Object o = correctFullText(list);
        System.out.println(o);

    }

    public static Set<String> correctTerms(String address) {
        address = normalize(address);
        Set<String> results = new HashSet<>();
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
        List<List<String>> list = new ArrayList<>();
        List<String> acc = new ArrayList<>();
        for (String s : split) {
            if (COMMON_FIX.contains(s)) {
                list.add(acc);
                acc = new ArrayList<>();
                acc.add(s);
                list.add(acc);
                acc = new ArrayList<>();
            } else {
                acc.add(s);
            }
        }
        if (!acc.isEmpty()) {
            list.add(acc);
        }
        List<List<String>> varList = new ArrayList<>();
        for (List<String> tokens : list) {
            List<String> vars = new ArrayList<>();
            if (tokens.size() > 1) {
                for (int i = 0; i < tokens.size() - 1; i++) {
                    vars.add(tokens.get(i) + tokens.get(i + 1));
                    results.add(tokens.get(i) + tokens.get(i + 1));
                }
            } else if (tokens.size() == 1) {
                vars.add(tokens.get(0));
                if (!COMMON_FIX.contains(tokens.get(0))) {
                    results.add(tokens.get(0));
                }
            }
            if (vars.size() > 0) {
                varList.add(vars);
            }
        }
        for (int i = 0; i < varList.size(); i++) {
            if (isPrefix(varList.get(i)) && i < varList.size() - 1) {
                results.add(varList.get(i).get(0) + varList.get(i + 1).get(0));
            }
            if (isPostfix(varList.get(i)) && i > 0) {
                results.add(varList.get(i - 1).get(0) + varList.get(i).get(0));
            }
        }
        return results;
    }

    private static boolean isPrefix(List<String> list) {
        return list.size() == 1 && COMMON_PREFIX.contains(list.get(0));
    }

    private static boolean isPostfix(List<String> list) {
        return list.size() == 1 && COMMON_POSTFIX.contains(list.get(0));
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
