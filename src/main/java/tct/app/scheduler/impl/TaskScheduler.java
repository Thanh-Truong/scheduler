package tct.app.scheduler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import tct.app.scheduler.Scheduler;

/**
 * Schedules/un-schedules tasks
 * 
 */
public class TaskScheduler implements Scheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    /**
     * Embedded database
     */
    private RedisTemplate database;

    /**
     * Set schedulerName to differentiate multiple schedulers.
     */
    private String schedulerName = "scheduler";
    /**
    * Executing tasks when they are due
    */
    private TaskExecutor taskExecutor;

    @SuppressWarnings("unchecked")
    public void schedule(String taskId, Instant when) {
        if (when == null) {
            throw new IllegalArgumentException("A trigger time must be provided.");
        }
        database.opsForZSet().add(schedulerName, taskId, when.toEpochMilli());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void cancel(String taskId) {
        database.opsForZSet().remove(schedulerName, taskId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeAllTasks() {
        database.delete(schedulerName);
    }

    @PostConstruct
    public void initialize() {
        taskExecutor.setName(schedulerName);
        taskExecutor.setSchedulerName(schedulerName);
        taskExecutor.setDatabase(database);
        taskExecutor.start();

        log.info(String.format("[%s] Started Scheduler . . .", schedulerName));
    }


    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.makeUnavailable();
            log.info(String.format("[%s] Stopped Scheduler . . .", schedulerName));
        }
    }

    public void setDatabase(RedisTemplate database) {
        this.database = database;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
}
