package tct.app.scheduler.impl;


import java.util.ArrayList;
import java.util.List;
import tct.app.scheduler.Task;

/**
 * Task implementor.
 * Scheduler executes tasks due to their registered timestamp. However, they may
 * take different times to complete.
 */
public class SimpleTask implements Task {

    @SuppressWarnings("FieldMayBeFinal")
    private List<String> executedTasks;
    
    public SimpleTask() {
        executedTasks = new ArrayList<String>();
    }
    
    @Override
    public void run(String taskId) {
        executedTasks.add(taskId);
        System.out.println(String.format("[%s] begins", taskId));
        System.out.println(String.format("[%s] is running", taskId));
        System.out.println(String.format("[%s] ends", taskId));
    }

    public List<String> getExecutedTasks() {
        return executedTasks;
    }

    public void reset() {
        executedTasks.clear();
    }
}
