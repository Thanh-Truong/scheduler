package tct.app.scheduler.impl;

import static java.lang.Thread.sleep;
import java.time.Clock;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import tct.app.scheduler.Task;

/**
 * TaskExecutor looks into database and executes due tasks if there are any.
 * TaskExecutor can be stopped by calling <code>makeUnavailable</code>
 *
 */
public class TaskExecutor extends Thread {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    private static final boolean SUCCESS = true;
     private static final boolean FAILURE = false;
    
    /**
     * Indicator on/off of this TaskExecutor
     */
    private boolean unavailable;
    /**
     * Max number of retries when connection to embedded database. After this,
     * error is logged.
     */
    private int maxRetries = 1;
    /**
     * Counter of number of retries
     */
    private int numRetries = 0;
    /**
     * Name of scheduler, which is assigned by the scheduler
     */
    private String schedulerName = "scheduler";
    /**
     * Clock, which will be initialized to a SimpleClock
     */
    private Clock clock = Clock.systemDefaultZone();
    /**
     * Database
     */
    private RedisTemplate database;
    /**
     * Scheduled task (or job)
     */
    private Task task;
    /**
     * Delay between two database lookups to find due task to execute
     */
    private int delay = 70;

    public TaskExecutor() {
        this.unavailable = false;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setDatabase(RedisTemplate database) {
        this.database = database;
    }

    public void setMaxRetries(int num) {
        this.maxRetries = num;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    /**
     * If called, TaskExecutor becomes unavailable.
     */
    public void makeUnavailable() {
        unavailable = true;
    }

    private boolean availableToRun() {
        return !unavailable && numRetries < maxRetries;
    }
    
    private boolean exceedsMaxRetries() {
        return numRetries >= maxRetries;
    }
    
    @Override
    public void run() {
        try {
            while (availableToRun()) {
                findAndExecuteTaskPeriodcally();
            }
        } catch (InterruptedException e) {
            logErrorWhileSchedulingTasks(e);
        } catch (Exception e) {
            /*TODO
              Option 1 : Do not catch any exception and let it crashes
              Option 2 : Log and retries ??
            */
        }

        if (exceedsMaxRetries()) {
            logErrorMaxRetries();
        }
    }

    private boolean executeTask(RedisOperations redisOps, String task) {
        redisOps.multi();
        redisOps.opsForZSet().remove(schedulerName, task);
        log.info(String.format("[%s] Executing [%s]... ", schedulerName, task));

        boolean executionSuccess = (redisOps.exec() != null);

        if (executionSuccess) {
            this.task.run(task);
            return SUCCESS;
        }
        return FAILURE;
    }

    @SuppressWarnings("unchecked")
    private boolean findAndExecuteTask() {

        return (Boolean) database.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOps)
                    throws DataAccessException {
                // Watch all modifications on this key    
                redisOps.watch(schedulerName);
                String taskId = findFirstTask(redisOps);

                if (taskId != null) {
                    return executeTask(redisOps, taskId);
                } 
                redisOps.unwatch();
                return FAILURE;
            }
        });
    }

    private String convertFoundTaskToString(RedisOperations ops, 
            Set<byte[]> taskInBinary) {
        byte[] binary = taskInBinary.iterator().next();
        Object object = ops.getValueSerializer().deserialize(binary);
        return (object != null) ? object.toString() : null;
    }

    /**
     * First task to be executed based on its score also known as timestamp
     *
     * @param ops
     * @return identifier of task
     */
    @SuppressWarnings("unchecked")
    private String findFirstTask(RedisOperations ops) {
        Set<byte[]> res = (Set<byte[]>) ops.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.zRangeByScore(schedulerName.getBytes(),
                        0, clock.millis(), 0, 1);
            }
        });

        if (res != null && !res.isEmpty()) {
            return convertFoundTaskToString(ops, res);
        }
        return null;
    }

    private void findAndExecuteTaskPeriodcally() throws InterruptedException {
        try {
            findAndExecuteTask();
            // Sleep until the next database lookup
            sleep(delay);
            
            numRetries = 0;
        } catch (RedisConnectionFailureException e) {
            numRetries++;
            logErrorConnectionFailureWhileScheduling();
        }
    }

    private void logErrorWhileSchedulingTasks(Exception e) {
        log.error(String.format(
                "[%s] Error while polling scheduled tasks."
                + " Please terminate to reset!!",
                schedulerName), e);
    }

    private void logErrorMaxRetries() {
        log.error(String.format("[%s] Maximum number of retries (%s) "
                + "has been reached."
                + " Please terminate to reset!!",
                schedulerName,
                maxRetries));
    }

    private void logErrorConnectionFailureWhileScheduling() {
        log.warn(String.format("[%s]Connection failure during scheduling ",
                schedulerName));
    }

}
