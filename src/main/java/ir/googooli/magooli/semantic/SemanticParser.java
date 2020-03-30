/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.googooli.magooli.semantic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

/**
 *
 * @author fr
 */
public class SemanticParser {

    public static void main(String[] args) throws IOException {

        //1 2 8 9 15 16
        ObjectMapper mapper = new ObjectMapper();
        RestClient elasticClient = RestClient.builder(new HttpHost("192.168.16.150", 9200)).build();
        RestClient encoderClient = RestClient.builder(new HttpHost("localhost", 5000)).build();
        final List<String> lines = FileUtils.readLines(new File("/mnt/extra/masoudi/semantic_search/dataset/persica"), "utf-8");
        for (int i = 0; i < lines.size(); i += 7) {
            try {
                final String title = lines.get(i + 1);
                final String desc = lines.get(i + 2);
                Request encRequest = new Request("POST", "/embed");
                HashMap<String, Object> inp = new HashMap<>();
                inp.put("text", Collections.singletonList(title));
                encRequest.setJsonEntity(mapper.writeValueAsString(inp));
                final Response encResponse = encoderClient.performRequest(encRequest);
                HashMap<String, Object> res = mapper.readValue(EntityUtils.toString(encResponse.getEntity()), HashMap.class);
                Number took = (Number) res.get("took");
                System.out.println("took= " + took);
                final ArrayList encList = (ArrayList) res.get("vectors");

                HashMap<String, Object> doc = new HashMap<>();
                doc.put("title", title);
                doc.put("desc", desc);
                doc.put("title_vec", encList.get(0));
                Request put = new Request("POST", "/persica/_doc/");
                put.setJsonEntity(mapper.writeValueAsString(doc));
                elasticClient.performRequest(put);
            } catch (Throwable ex) {
                Logger.getLogger(SemanticParser.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
