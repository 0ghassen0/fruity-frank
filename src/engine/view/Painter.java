package engine.view;

import oop.tasks.Runtime;
import oop.tasks.Task;

public class Painter {

    private final View view;
    private final Task task;
    private long last;
    private static final int TARGET_MS = 33; // 30fps

    public Painter(View view) {
        assert view != null;
        this.view = view;
        this.task = Runtime.newTask("painter");
    }

    public void start() {
        last = System.currentTimeMillis();
        task.post(new oop.tasks.Runnable() {
            public void run() throws Exception {
                long now = System.currentTimeMillis();
                long elapsed = now - last;
                last = now;
                if (view.canvas() != null)
                    view.canvas().repaint();
                long skew = elapsed - TARGET_MS;
                long delay = Math.max(0, TARGET_MS - skew);
                task.post(this, (int) delay);
            }
        });
    }

    public void stop() {
        task.kill();
    }
}