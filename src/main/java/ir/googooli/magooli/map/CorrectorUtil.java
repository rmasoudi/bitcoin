package ir.googooli.magooli.map;

import java.util.*;
import java.util.regex.Pattern;

public class CorrectorUtil {


    private static Pattern SEP_PATTERN = Pattern.compile("[ادذرزژ,و]");

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("مشهد رضا ۳۰ - ابوذر غفاری ۳۰");
        Object o = correctFullText(list);
        System.out.println(o);

    }

    public static Set<String> correctTerms(String address) {
        String[] split = address.split(" ");
        Set<String> set = new HashSet<>(Arrays.asList(split));
        for (String word : split) {
            List<Integer> indices = getSplitIndices(word);
            for (Integer index : indices) {
                String s = splitAt(word, index);
                set.add(s.trim());
            }
        }
        for (int i = 0; i < split.length - 1; i++) {
            set.add(split[i] + split[i + 1]);
        }
        return set;
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

    public static Object correctFullText(Object o) {
        if (o instanceof String) {
            String text = o.toString();
            return createAddress(CorrectorUtil.correctTerms(text));
        } else {
            ArrayList<String> list = (ArrayList<String>) o;
            Set<String> set = new HashSet<>();
            for (String s : list) {
                Set<String> terms = CorrectorUtil.correctTerms(s);
                set.addAll(terms);
            }
            return createAddress(set);
        }
    }

    private static String createAddress(Set<String> terms) {
        StringBuilder acc = new StringBuilder();
        for (String s : terms) {
            acc.append(s).append(" ");
        }
        return acc.toString().trim();
    }
}
