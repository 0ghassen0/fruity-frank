package engine.view;

import engine.core.Model;
import oop.tasks.Task;

public class Ticker {

    private final Model model;
    private final View view;
    private final Task task;
    private long last;

    public Ticker(Model model, View view, Task paintTask) {
        assert model != null;
        assert view != null;
        this.model = model;
        this.view = view;
        this.task = paintTask;
    }

 // Dans Ticker.start()
    public void start() {
        last = System.currentTimeMillis();
        task.post(new oop.tasks.Runnable() {
        	public void run() throws Exception {
        	    long now = System.currentTimeMillis();
        	    double elapsed = now - last;
        	    last = now;
        	    model.tick(elapsed);
        	    task.post(this, 16);
        	}
        }, 100); // délai initial de 100ms pour laisser le canvas s'initialiser
    }
    public void stop() {
        task.kill();
    }
}