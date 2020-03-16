package ir.googooli.magooli.map;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NameCorrector {
    public static void main(String[] args) throws InterruptedException {
        RestClient restClient = RestClient.builder(new HttpHost(args[0], 9200)).build();
        BlockingQueue<HashMap<String, Object>> queue = new ArrayBlockingQueue<>(100);
        MapScroller mapScroller = new MapScroller(restClient, args[1], queue);
        int threads = Integer.parseInt(args[2]);
        String replace = args[3];
        for (int i = 0; i < threads; i++) {
            MapProcessor mapProcessor = new MapProcessor(queue, restClient,replace);
            mapProcessor.setName("Processor_" + i);
            mapProcessor.start();
        }
        mapScroller.start();
        while (true) {
            Thread.sleep(1);
        }

    }
}
