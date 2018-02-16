package tct.app.scheduler.impl;

import java.time.*;
import java.util.concurrent.TimeUnit;
/***
 * 
 * SimpleClock 
 */
public class SimpleClock extends Clock {

    private Instant presentTime = Instant.now();

    public Instant instant() {
        return presentTime;
    }

    public void forward(int period, TimeUnit unit) {
        long currentTime = presentTime.toEpochMilli();
        long newTime = currentTime + (unit.toMillis(period));
        Instant.ofEpochMilli(newTime);
        setTime(Instant.ofEpochMilli(newTime));
    }

    public void setTime(Instant stubbedTime) {
        presentTime = stubbedTime;
    }

    public ZoneId getZone() {
        return null;  
    }

    public Clock withZone(ZoneId zone) {
        return null;
    }
}
