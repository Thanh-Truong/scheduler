package tct.app.scheduler;

import tct.app.scheduler.impl.SimpleTask;
import tct.app.scheduler.impl.SimpleClock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.text.ParseException;
import java.time.Instant;

import static java.util.Arrays.asList;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-configuration.xml")
public class IntegrationTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SimpleClock clock;

    @Autowired
    private SimpleTask task;

    @Before
    public void setup() {
        cleanUp();
    }
    
    private void cleanUp() {
        scheduler.removeAllTasks();
        task.reset();
    }
    
    private Instant shiftTime(SimpleClock clock, int period, TimeUnit unit) {
        long current = clock.instant().toEpochMilli();
        long newTime = current + (unit.toMillis(period));
        return Instant.ofEpochMilli(newTime);
    }
    
    @Test
    public void scheduleSingleTask() throws ParseException, InterruptedException {
        cleanUp();
        clock.setTime(Instant.now());
        scheduler.schedule("task-1", shiftTime(clock, 20, HOURS));
        clock.forward(20, HOURS);
        assertTasks("task-1");
    }

    @Test
    public void scheduleMultipleTaks() throws ParseException, InterruptedException {
        cleanUp();
        clock.setTime(Instant.now());
        scheduler.schedule("task-1", shiftTime(clock, 10, DAYS));
        scheduler.schedule("task-2", shiftTime(clock, 20, DAYS));
        clock.forward(20, DAYS);
        assertTasks("task-1", "task-2");
    }

    @Test
    public void rescheduleTask() throws ParseException, InterruptedException {
        cleanUp();
         clock.setTime(Instant.now());
        scheduler.schedule("task-1", shiftTime(clock, 50, HOURS));
        scheduler.schedule("task-1", shiftTime(clock, 11, HOURS));
        clock.forward(11, HOURS);
        assertTasks("task-1");
    }
 
    @Test
    public void unscheduleTask() throws ParseException, InterruptedException {
        cleanUp();
         clock.setTime(Instant.now());
        scheduler.schedule("task-1", shiftTime(clock, 1, HOURS));
        scheduler.schedule("task-2", shiftTime(clock, 1, HOURS));
        scheduler.cancel("task-2");
        clock.forward(2, HOURS);
        assertTasks("task-1");
    }

    private void assertTasks(String... tasks) throws InterruptedException {
        // sleeps some times untill all task been executed.
        Thread.sleep(1000*tasks.length);
        
        assertThat("Tasks count", task.getExecutedTasks().size(), is(tasks.length));
        assertThat("Tasks", task.getExecutedTasks(), is(asList(tasks)));
    }

}
