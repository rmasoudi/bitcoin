package ir.googooli.magooli;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

public class ScrollManager {
    private Long cursor;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Long interval;
    private BlockingQueue<Interval> queue;
    private volatile boolean finished = false;

    public ScrollManager(ExtractorConfig config, BlockingQueue<Interval> queue) {
        cursor = LocalDateTime.parse(config.getFrom(), timeFormatter).toInstant(ZoneOffset.UTC).toEpochMilli();
        this.queue = queue;
        loadInterval(config);
    }

    private void loadInterval(ExtractorConfig config) {
        if (config.getInterval().endsWith("s")) {
            interval = Long.parseLong(config.getInterval().replace("s", "")) * 1000;
        } else if (config.getInterval().endsWith("m")) {
            interval = Long.parseLong(config.getInterval().replace("m", "")) * 60 * 1000;
        } else if (config.getInterval().endsWith("h")) {
            interval = Long.parseLong(config.getInterval().replace("h", "")) * 60 * 60 * 1000;
        } else if (config.getInterval().endsWith("d")) {
            interval = Long.parseLong(config.getInterval().replace("m", "")) * 24 * 60 * 60 * 1000;
        } else {
            interval = Long.parseLong(config.getInterval());
        }
    }

    public void start() {
        while (!finished) {
            try {
                waitForData();
                Interval item = new Interval(cursor, cursor + interval);
                queue.put(item);
                cursor += interval;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void waitForData() {
        if (cursor >= System.currentTimeMillis()) {
            try {
                Thread.sleep(3 * 60 * 60 * 1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void finish() {
        finished = true;
    }
}
