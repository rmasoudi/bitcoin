package ir.googooli.magooli;

import java.util.ArrayList;
import java.util.HashMap;

public class ExtractorConfig {
    private HashMap<String, Object> request;
    private int threads;
    private int scrollers;
    private ArrayList<String> elastic;
    private String scroll;
    private String metaIndex;
    private int size;
    private String from;
    private String interval;
    private String txIndex;
    private String addressIndex;

    public HashMap<String, Object> getRequest() {
        return request;
    }

    public void setRequest(HashMap<String, Object> request) {
        this.request = request;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public ArrayList<String> getElastic() {
        return elastic;
    }

    public void setElastic(ArrayList<String> elastic) {
        this.elastic = elastic;
    }

    public String getScroll() {
        return scroll;
    }

    public void setScroll(String scroll) {
        this.scroll = scroll;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMetaIndex() {
        return metaIndex;
    }

    public void setMetaIndex(String metaIndex) {
        this.metaIndex = metaIndex;
    }

    public int getScrollers() {
        return scrollers;
    }

    public void setScrollers(int scrollers) {
        this.scrollers = scrollers;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(String txIndex) {
        this.txIndex = txIndex;
    }

    public String getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(String addressIndex) {
        this.addressIndex = addressIndex;
    }
}
