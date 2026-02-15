package manager.queue;

import manager.core.DownloadTask;
import java.util.LinkedList;
import java.util.Queue;

public class DownloadQueue {

    private final Queue<DownloadTask> queue = new LinkedList<>();
    private boolean running = false;

    public void add(DownloadTask task) {
        queue.add(task);
        process();
    }

    private void process() {
        if (running) return;

        DownloadTask task = queue.poll();
        if (task == null) return;

        running = true;

        new Thread(() -> {
            task.start();
            running = false;
            process();
        }).start();
    }
}
