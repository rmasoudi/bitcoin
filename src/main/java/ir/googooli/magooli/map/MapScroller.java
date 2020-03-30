package ir.googooli.magooli.map;

import ir.googooli.magooli.ScrollAndSize;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class MapScroller extends Thread {
    private volatile boolean finished = false;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;
    private int counter=0;


    private String index;
    private BlockingQueue<HashMap<String, Object>> outputQueue;

    public MapScroller(RestClient restClient, String index, BlockingQueue<HashMap<String, Object>> outputQueue) {
        this.restClient = restClient;
        this.index = index;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            Request request = new Request("GET", "/" + index + "/_search");
            request.addParameter("scroll", "10m");
            request.addParameter("size", "5000");
            Response response = restClient.performRequest(request);
            ScrollAndSize scrollAndSize = processResults(response);

            while (scrollAndSize != null && scrollAndSize.getScroll() != null && scrollAndSize.getSize() > 0) {
                Request scrollRequest = new Request("GET", "/_search/scroll");
                HashMap<String, Object> scrollBody = new HashMap<>();
                scrollBody.put("scroll", "10m");
                scrollBody.put("scroll_id", scrollAndSize.getScroll());
                scrollRequest.setJsonEntity(objectMapper.writeValueAsString(scrollBody));
                Response scrollResponse = restClient.performRequest(scrollRequest);
                processResults(scrollResponse);
            }


        } catch (InterruptedException e) {
            finished = true;
            run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private ScrollAndSize processResults(Response scrollResponse) throws IOException, InterruptedException {
        HashMap<String, Object> res = objectMapper.readValue(EntityUtils.toString(scrollResponse.getEntity()), HashMap.class);
        if (res.get("_scroll_id") == null) {
            return null;
        }
        String scrollId = (String) res.get("_scroll_id");
        res = (HashMap<String, Object>) res.get("hits");
        ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) res.get("hits");
        for (HashMap<String, Object> item : list) {
            HashMap<String, Object> source = (HashMap<String, Object>) item.get("_source");
            source.put("id", item.get("_id").toString());
            source.put("index", item.get("_index").toString());
            counter++;
            System.out.println(counter);
            outputQueue.put(source);
        }
        return new ScrollAndSize(list.size(), scrollId);
    }


    public void finish() {
        finished = true;
    }
}
