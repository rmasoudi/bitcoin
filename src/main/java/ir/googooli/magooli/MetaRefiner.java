package ir.googooli.magooli;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MetaRefiner {
    private static final Logger LOG = Logger.getLogger(MetaRefiner.class);
    private static List<ExtractorThread> extractors = new ArrayList<>();
    private static List<MetaScroller> scrollers = new ArrayList<>();
    private static ScrollManager scrollManager;

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure(args[1]);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scrollManager != null) {
                scrollManager.finish();
            }
            for (ExtractorThread extractor : extractors) {
                extractor.finish();
            }
            for (MetaScroller scroller : scrollers) {
                scroller.finish();
            }
        }));
        ObjectMapper objectMapper = new ObjectMapper();
        ExtractorConfig config = objectMapper.readValue(new File(args[0]), ExtractorConfig.class);
        BlockingQueue<HashMap<String, Object>> queue = new ArrayBlockingQueue<>(config.getThreads() * 3);
        BlockingQueue<Interval> intervalQueue = new ArrayBlockingQueue<>(config.getScrollers() * 3);

        HttpHost[] hosts = new HttpHost[config.getElastic().size()];
        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = new HttpHost(config.getElastic().get(i), 9200);
        }
        RestClient restClient = RestClient.builder(hosts).build();
        for (int i = 0; i < config.getThreads(); i++) {
            ExtractorThread extractorThread = new ExtractorThread(queue, config.getTxIndex(), config.getAddressIndex(), restClient);
            extractorThread.setName("Extractor_" + i);
            extractors.add(extractorThread);
            extractorThread.start();
        }
        for (int i = 0; i < config.getScrollers(); i++) {
            MetaScroller metaScroller = new MetaScroller(restClient, config, intervalQueue, queue);
            metaScroller.setName("Scroller_" + i);
            scrollers.add(metaScroller);
            metaScroller.start();
        }
        scrollManager = new ScrollManager(config, intervalQueue);
        scrollManager.start();
    }
}
