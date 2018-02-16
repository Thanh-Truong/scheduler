package tct.app.scheduler;

import java.time.Instant;


/**
 * Schedules tasks
 */
public interface Scheduler {

    /**
     * Schedules a task to be executed at a given time
     * @param taskId an arbitrary task identifier.
     * @param when the time at which the task will be executed. 
     * If <code>when</code> is null, an exception is thrown
     * If <code>when</code> is time in the past, the given task will be scheduled
     * for immediately execution.
     */
    void schedule(String taskId, Instant when);

    /**
     * Removes all currently scheduled tasks from the scheduler.
     */
    void removeAllTasks();

    /**
     * Removes a specific task from the scheduler
     * @param taskId The taskId to be removed.
     */
    void cancel(String taskId);
}
