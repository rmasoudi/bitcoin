package ir.googooli.magooli.zmap;


import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Stream;

public class BitCrawler {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Base64.Decoder decoder = Base64.getMimeDecoder();
        File file = new File("/mnt/extra/masoudi/net_scanner/out");
        try (Stream<String> lines = Files.lines(Paths.get("/mnt/extra/masoudi/net_scanner/2020-02-12-1581526848-http_get_9200.json"))) {
            lines.forEach(s -> {
                try {
                    HashMap<String, Object> hashMap = objectMapper.readValue(s, HashMap.class);
                    String ip = hashMap.get("ip").toString();
                    String text = new String(decoder.decode(hashMap.get("data").toString()), StandardCharsets.UTF_8);
                    if (text.contains("You Know")) {
                        FileUtils.writeStringToFile(file, ip + "\n", StandardCharsets.UTF_8, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
