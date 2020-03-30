package ir.googooli.magooli.zmap;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LocFinder2 {

    //80
    //9001
    //8081
    //8001
    //8080
    public static void main(String[] args) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient target = RestClient.builder(new HttpHost("192.168.16.150", 9200)).build();
        Request request = new Request("GET", "/elastic/_search");
        request.addParameter("size", "10000");
        request.addParameter("q", "NOT country:*");
        Response response = target.performRequest(request);
        HashMap<String, Object> hashMap = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);
        hashMap = (HashMap<String, Object>) hashMap.get("hits");
        ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) hashMap.get("hits");
        int counter = 0;
        for (HashMap<String, Object> map : list) {
            String id = map.get("_id").toString();
            String index = map.get("_index").toString();
            map = (HashMap<String, Object>) map.get("_source");
            String ip = map.get("ip").toString();

            String res = execCmd("geoiplookup -f /usr/share/GeoIP/GeoLiteCity.dat " + ip);
            if (!res.contains("Address not found")) {
                String[] split = res.split(",");
                if (split.length >= 8) {
                    String country = split[1].split(":")[1].trim();
                    String loc = split[6].trim() + "," + split[7].trim();
                    Request up = new Request("POST", "/" + index + "/_update/" + id);
                    HashMap<String, Object> partial = new HashMap<>();
                    partial.put("country", country);
                    partial.put("loc", loc);
                    HashMap<String, Object> doc = new HashMap<>();
                    doc.put("doc", partial);
                    up.setJsonEntity(objectMapper.writeValueAsString(doc));
                    target.performRequest(up);
                }
            }
            counter++;
            System.out.println(counter);
        }
    }

    public static HashMap<String, Object> getGeoInfo(String ip) throws IOException {
        String res = execCmd("geoiplookup -f /usr/share/GeoIP/GeoLiteCity.dat " + ip);
        if (res.contains("Address not found")) {
            return new HashMap<>();
        }
        String[] split = res.split(",");
        String country = split[1].split(":")[1].trim();
        String loc = split[6].trim() + "," + split[7].trim();
        HashMap<String, Object> partial = new HashMap<>();
        partial.put("country", country);
        partial.put("loc", loc);
        return partial;
    }

    public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
