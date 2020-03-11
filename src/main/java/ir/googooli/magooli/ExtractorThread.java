package ir.googooli.magooli;

import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ExtractorThread extends Thread {

    private static final String OUT_WALLET = "out_wallet";
    private static final String IN_WALLET = "in_wallet";
    private volatile boolean finished = false;
    private BlockingQueue<HashMap<String, Object>> inputQueue;
    private String txIndex;
    private String addressIndex;
    private RestClient restClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ExtractorThread(BlockingQueue<HashMap<String, Object>> inputQueue, String txIndex, String addressIndex, RestClient restClient) {
        this.inputQueue = inputQueue;
        this.txIndex = txIndex;
        this.addressIndex = addressIndex;
        this.restClient = restClient;
    }

    @Override
    public void run() {
        while (!finished || !inputQueue.isEmpty()) {
            try {
                HashMap<String, Object> item = inputQueue.take();
                String index = (String) item.get("index");
                String id = (String) item.get("id");
                String txId = (String) item.get("txid");
                ArrayList<String> outputAddress = (ArrayList<String>) item.get("output_address");
                HashMap<String, Object> tx = null;
                HashMap<String, Object> partial = new HashMap<String, Object>();
                if (txId != null) {
                    tx = getTxById(txId);
                }
                if (tx != null) {
                    if (tx.get(OUT_WALLET) != null) {
                        partial.put(OUT_WALLET, tx.get(OUT_WALLET));
                    }
                    if (tx.get(IN_WALLET) != null) {
                        partial.put(IN_WALLET, tx.get(IN_WALLET));
                    }
                }
                if (tx == null && outputAddress != null) {
                    ArrayList<String> outWallets = new ArrayList<String>();
                    for (String address : outputAddress) {
                        String wallet = getAddressWallet(address);
                        if (wallet != null) {
                            outWallets.add(wallet);
                        }
                    }
                    partial.put("out_wallet", outWallets);
                }
                if (partial.size() > 0) {
                    partial.put("index", index);
                    partial.put("id", id);
                    update(partial);
                }

            } catch (InterruptedException e) {
                finished = true;
                run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void update(HashMap<String, Object> partial) {
        //TODO send to rabbit queue
    }


    private HashMap<String, Object> getTxById(String txId) {
        Request request = new Request("GET", "/" + txIndex + "/_doc/" + txId);
        try {
            Response response = restClient.performRequest(request);
            HashMap<String, Object> map = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);
            return (HashMap<String, Object>) map.get("_source");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getAddressWallet(String address) {
        Request request = new Request("GET", "/" + addressIndex + "/_doc/" + address);
        try {
            Response response = restClient.performRequest(request);
            HashMap<String, Object> map = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);
            map = (HashMap<String, Object>) map.get("_source");
            return (String) map.get("wallet");
        } catch (Throwable e) {
            return null;
        }
    }

    public void finish() {
        finished = true;
    }

}
