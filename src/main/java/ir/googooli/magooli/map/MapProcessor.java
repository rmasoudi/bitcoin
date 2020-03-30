package ir.googooli.magooli.map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class MapProcessor extends Thread {

    private BlockingQueue<HashMap<String, Object>> queue;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;
    private String replace;

    public MapProcessor(BlockingQueue<HashMap<String, Object>> queue, RestClient restClient, String replace) {
        this.queue = queue;
        this.restClient = restClient;
        this.replace = replace;
    }

    @Override
    public void run() {
        while (true) {
            try {
                HashMap<String, Object> take = queue.take();
                Object full_text = take.get("full_text");
                if (full_text == null) {
                    continue;
                }
                take.put("full_text", CorrectorUtil.correctFullText(full_text));
                String index = take.remove("index").toString();
                String id = take.remove("id").toString();
                index = index.replace("2", replace);
                Request request = new Request("PUT", "/" + index + "/_doc/" + id);
                request.setJsonEntity(objectMapper.writeValueAsString(take));
                restClient.performRequest(request);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
