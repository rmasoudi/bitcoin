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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class AntMiner {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Base64.Decoder decoder = Base64.getMimeDecoder();
        File out = new File("/mnt/extra/masoudi/net_scanner/antminer/list");
        File[] files = new File("/mnt/extra/masoudi/net_scanner/antminer/input/").listFiles();
        AtomicReference<Integer> counter= new AtomicReference<>(0);
        for (File file : files) {
            try (Stream<String> lines = Files.lines(Paths.get(file.getAbsolutePath()))) {
                lines.forEach(s -> {
                    try {
                        HashMap<String, Object> hashMap = objectMapper.readValue(s, HashMap.class);
                        String ip = hashMap.get("ip").toString();
                        String text = new String(decoder.decode(hashMap.get("data").toString()), StandardCharsets.UTF_8);
                        if (text.contains("antMiner Configuration")) {
                            FileUtils.writeStringToFile(out, ip + "\n", StandardCharsets.UTF_8, true);
                        }
                        counter.getAndSet(counter.get() + 1);
                        System.out.println(counter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
