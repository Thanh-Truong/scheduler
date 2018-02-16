package tct.app.scheduler;

/**
 * Callback interface that will get executed when a previously-scheduled task is due for execution.
 */
public interface Task {

    /**
     * Called by the scheduler once a task is due for execution.
     * @param taskId
     */
    void run(String taskId);
}
