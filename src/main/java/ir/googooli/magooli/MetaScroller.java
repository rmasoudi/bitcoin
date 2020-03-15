package ir.googooli.magooli;

import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class MetaScroller extends Thread {
    private static final DateTimeFormatter indexFormatter = DateTimeFormat.forPattern("yyyy_MM_dd");
    private volatile boolean finished = false;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;


    private ExtractorConfig config;
    private BlockingQueue<Interval> inputQueue;
    private BlockingQueue<HashMap<String, Object>> outputQueue;

    public MetaScroller(RestClient restClient, ExtractorConfig config, BlockingQueue<Interval> inputQueue, BlockingQueue<HashMap<String, Object>> outputQueue) {
        this.restClient = restClient;
        this.config = config;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while (!finished || !inputQueue.isEmpty()) {
            try {
                Interval interval = inputQueue.take();
                String datePart = indexFormatter.print(interval.getFrom());
                datePart = datePart.substring(0, datePart.length() - 1);
                String indexName = config.getMetaIndex() + datePart;
                Request request = new Request("GET", "/" + indexName + "/_search");
                request.addParameter("scroll", config.getScroll());
                request.addParameter("size", config.getSize() + "");
                request.setJsonEntity(objectMapper.writeValueAsString(config.getRequest()));
                Response response = restClient.performRequest(request);
                ScrollAndSize scrollAndSize = processResults(response);

                while (scrollAndSize != null && scrollAndSize.getScroll() != null && scrollAndSize.getSize() > 0) {
                    Request scrollRequest = new Request("GET", "/_search/scroll");
                    HashMap<String, Object> scrollBody = new HashMap<>();
                    scrollBody.put("scroll", config.getScroll());
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
            outputQueue.put(source);
        }
        return new ScrollAndSize(list.size(), scrollId);
    }


    public void finish() {
        finished = true;
    }
}
