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

public class LocFinder {
    //80
    //9001
    //8081
    //8001
    //8080
    public static void main(String[] args) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient target = RestClient.builder(new HttpHost("192.168.16.150", 9200)).build();
        RestClient lookup = RestClient.builder(new HttpHost("208.95.112.1", 80)).build();


        Request request = new Request("GET", "/elastic/_search");
        request.addParameter("size", "10000");
        request.addParameter("q", "NOT country:*");
        Response response = target.performRequest(request);
        HashMap<String, Object> hashMap = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);
        hashMap = (HashMap<String, Object>) hashMap.get("hits");
        ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) hashMap.get("hits");
        for (HashMap<String, Object> map : list) {
            String id = map.get("_id").toString();
            String index = map.get("_index").toString();
            map = (HashMap<String, Object>) map.get("_source");
            String ip = map.get("ip").toString();

            Request ipReq = new Request("GET", "/json/" + ip);
            Response response1;
            while (true) {
                try {
                    response1 = lookup.performRequest(ipReq);
                    break;
                } catch (Throwable e) {
                    e.printStackTrace();
                    Thread.sleep(30000);
                    lookup = RestClient.builder(new HttpHost("208.95.112.1", 80)).build();
                }
            }
            HashMap<String, Object> ipRec = objectMapper.readValue(EntityUtils.toString(response1.getEntity()), HashMap.class);
            String country = ipRec.get("countryCode").toString();
            String loc = ipRec.get("lat").toString() + "," + ipRec.get("lon").toString();
            Request up=new Request("POST","/"+index+"/_update/"+id);
            HashMap<String,Object> partial=new HashMap<>();
            partial.put("country",country);
            partial.put("loc",loc);
            HashMap<String,Object> doc=new HashMap<>();
            doc.put("doc",partial);
            up.setJsonEntity(objectMapper.writeValueAsString(doc));
            target.performRequest(up);
            System.out.println();
        }
    }
}
