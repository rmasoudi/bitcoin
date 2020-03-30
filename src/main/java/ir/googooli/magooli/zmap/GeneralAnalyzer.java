package ir.googooli.magooli.zmap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

public class GeneralAnalyzer {

    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z]");

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Base64.Decoder decoder = Base64.getMimeDecoder();
        final GeneralConfig config = objectMapper.readValue(new File(args[0]), GeneralConfig.class);
        File[] files = new File(config.getInput()).listFiles();
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        final List<String> queries = config.getTerms();
        RestClient client = RestClient.builder(new HttpHost("192.168.16.150", 9200)).build();
        for (File file : files) {
            try (Stream<String> lines = Files.lines(Paths.get(file.getAbsolutePath()))) {
                lines.forEach(s -> {
                    try {
                        HashMap<String, Object> hashMap = objectMapper.readValue(s, HashMap.class);
                        String ip = hashMap.get("ip").toString();
                        String text = new String(decoder.decode(hashMap.get("data").toString()), StandardCharsets.UTF_8).toLowerCase();
                        text = PATTERN.matcher(text).replaceAll(" ");
                        boolean match = false;
                        final String[] split = text.split(" ");
                        Set<String> set = new HashSet<>();
                        for (String item : split) {
                            if (!item.trim().isEmpty()) {
                                set.add(item.trim());
                                if (queries.contains(item.trim())) {
                                    match = true;
                                }
                            }
                        }

                        if (match) {
                            Request request = new Request("PUT", "/" + config.getIndex() + "/_doc/" + ElasticAnalyzer.getIpId(ip));
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("ip", ip);
                            map.putAll(LocFinder2.getGeoInfo(ip));
//                            text=Jsoup.parse(text).body().text();
                            map.put("text", text);

                            map.put("terms", set);
                            request.setJsonEntity(objectMapper.writeValueAsString(map));
                            client.performRequest(request);
                        }
                        counter.getAndSet(counter.get() + 1);
                        System.out.println(counter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
