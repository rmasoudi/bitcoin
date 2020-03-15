package ir.googooli.magooli;

public class QueryGenerator {
    public static void main(String[] args) {
        String address = "تهران میدان رسالت خیابان کرمان جنوبی کوچه عادل زاده شرقی";
        address = address.replace("بن بست", "بنبست");
        address = address.replace("اتوبان", "بزرگراه");
        address = address.replace("بزرگ راه", "بزرگراه");
        address = address.replace("آزاد راه", "آزادراه");
        address = address.replace(" خ ", " خیابان ");
        address = address.replace(" ک ", " کوچه ");
        address = address.replace("نرسیده به", " ");
        address = address.replace("ثبل از", " ");
        address = address.replace("حد فاصل", " ");
        address = address.replace("نبش ", " ");
        address = address.replace("  ", " ");
        String[] split = address.split(" ");
        String orClause = "";
        String andClause = "";
        for (String item : split) {
            int weight = 2;
            if (item.equals("کوچه") || item.equals("خیابان") || item.equals("بنبست") || item.equals("بزرگراه") || item.equals("آزادراه") || item.equals("میدان")) {
                weight = 1;
            }
            orClause+="("+item+"^"+weight+")~1 OR ";
            andClause+="("+item+"^"+weight+")~1 AND ";
        }
        String query = "(" + andClause + ")^2 OR (" + orClause + ")^1";
        System.out.println(query);
    }
}
