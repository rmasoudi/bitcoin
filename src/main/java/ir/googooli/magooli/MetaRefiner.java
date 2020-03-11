package ir.googooli.magooli;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MetaRefiner {
    private static final Logger LOG = Logger.getLogger(MetaRefiner.class);

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure(args[1]);
        ObjectMapper objectMapper = new ObjectMapper();
        ExtractorConfig config = objectMapper.readValue(new File(args[0]), ExtractorConfig.class);
        BlockingQueue<HashMap<String, Object>> queue = new ArrayBlockingQueue<>(config.getThreads() * 3);

        HttpHost[] hosts = new HttpHost[config.getElastic().size()];
        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = new HttpHost(config.getElastic().get(i), 9200);
        }
        RestClient restClient = RestClient.builder(hosts).build();
        for (int i = 0; i < config.getThreads(); i++) {
            ExtractorThread extractorThread = new ExtractorThread(queue, config.getTxIndex(), config.getAddressIndex(), restClient);
            extractorThread.setName("Extractor_" + i);
            extractorThread.start();
        }
        ScrollManager scrollManager = new ScrollManager(config);
        scrollManager.start();
    }
}
