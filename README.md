scheduler
===============

Keywords
-------------------
- distributed 
- multi-threaded 
- key-value stored database
- unit test
- configurable

What is this ?
-------------------
It is a simple distributed `Scheduler`, which can schedule `Task`(s) to be run at 
their specified times. The ultimate goal is creating an extensible library to 
build a configurable network of tasks /jobs running in distributed environment.

`Scheduler` and `Task` are identified by globally 
unique string identifiers. Several instances of Scheduler(s) can run and 
schedule/un-schedule multiple `Task`(s). Each `Task` is executed in its own thread.


This application uses embedded database Redis as central backend database. 
Technically, information about tasks and their expected time-to-run are stored
in key-value fashion. The centralized database helps to prevent a single unique
task to be executed by another instance of the Scheduler.


Highlighted frameworks
-------------------
- Database:             embedded-redis
- Database connection:  org.springframework
- Logging:              slf4j-api 
- Unit test:            junit

Files
-------------------

#### `Scheduler`
Abstract interface of a scheduler

#### `Task`
Abstract interface of a task

#### `TaskScheduler`
It is responsible for scheduling / un-scheduling tasks by writing to database:
taskId and its expected time to run.

#### `TaskScheduler`
A helper class that periodically looks up from the database for the first 
next due task and executes it. 

It can be stopped if there is explicitly request to stop. When error occurred,
the application needs to be restart. Thus, it exposes room for improvement.

#### `SimpleTask`
A simple implementation of `Task`. It does nothing but some prints. Here, one may
implement actual work


#### `SimpleClock`
A simple implementation of `Clock`, which allows to rewind forward the clock. Its
main purpose is for testing.


#### `IntegrationTest`
Some simple integration test

Building the project
--------------------

``` bash
    mvn clean install
```

Configurations
-----------------
This application is shipped with `application-context.xml` that allows to configure
some application parameters

For examples:

``` xml
     <bean id="taskExecutor" class="tct.app.scheduler.impl.TaskExecutor">
        <property name="delayMillis" value="50"/>
        ..
       <property name="database" ref="databaseConnection"/>
        <property name="task" ref="task"/>
        <property name="maxRetries" value="1"/>
     </bean>
     
    <bean id="scheduler" class="tct.app.scheduler.impl.TaskScheduler">
        ...
        <property name="schedulerName" value="scheuler-1"/>
    </bean>
```
