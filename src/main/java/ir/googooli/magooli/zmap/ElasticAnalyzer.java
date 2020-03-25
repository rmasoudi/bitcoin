package ir.googooli.magooli.zmap;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class ElasticAnalyzer {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        Stream<String> lines = Files.lines(Paths.get("D:\\rasoul\\scan\\inter"));

        lines.forEach(s -> {
            RestClient restClient = null;
            try {
                String[] split = s.split(" ");
                String ip = split[0];
                restClient = RestClient.builder(new HttpHost(ip, 9200)).build();
                Request request = new Request("GET", "/");
                Response response = restClient.performRequest(request);
                String text = EntityUtils.toString(response.getEntity());
                HashMap<String, Object> hashMap = objectMapper.readValue(text, HashMap.class);
                hashMap = (HashMap<String, Object>) hashMap.get("version");
                String version = hashMap.get("number").toString();

                request = new Request("GET", "/_cat/indices");
                response = restClient.performRequest(request);
                text = EntityUtils.toString(response.getEntity());
                String[] rows = text.split("\n");
                String biggestIndex = "";
                int maxDocCount = 0;
                for (int i = rows.length - 1; i >= 0; i--) {
                    if (rows[i].isEmpty()) {
                        continue;
                    }
                    String[] cols = rows[i].replaceAll(" +", " ").split(" ");
                    String indexName = cols[2];
                    if (!indexName.startsWith(".")) {
                        int docCount = Integer.parseInt(cols[6]);
                        if (docCount >= maxDocCount) {
                            maxDocCount = docCount;
                            biggestIndex = indexName;
                        }
                    }
                }
                if (biggestIndex.isEmpty()) {
                    biggestIndex = "-";
                }
                if (!biggestIndex.startsWith(".")) {
                    System.out.println(String.join("\t", ip, version, biggestIndex, maxDocCount + "", getSampleRecord(restClient, biggestIndex, maxDocCount)));
                }


                restClient.close();
            } catch (Exception e) {
                if (restClient != null) {
                    try {
                        restClient.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();

            }
        });
    }

    private static String getSampleRecord(RestClient restClient, String index, int count) throws IOException {
        if (count == 0) {
            return "-";
        }
        Request request = new Request("GET", "/" + index + "/_search");
        Response response = restClient.performRequest(request);
        HashMap<String, Object> hashMap = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);
        hashMap = (HashMap<String, Object>) hashMap.get("hits");
        ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) hashMap.get("hits");
        hashMap = list.get(0);
        hashMap = (HashMap<String, Object>) hashMap.get("_source");
        return objectMapper.writeValueAsString(hashMap);
    }
}
