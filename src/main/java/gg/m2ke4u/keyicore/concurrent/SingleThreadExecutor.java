package gg.m2ke4u.keyicore.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.LockSupport;

public class SingleThreadExecutor implements Executor {
    private final LinkedBlockingQueue<TaskEntry> tasks = new LinkedBlockingQueue<>();
    private static final Logger logger = LogManager.getLogger();

    public SingleThreadExecutor(ThreadFactory factory){
        Thread worker = factory.newThread(()->{
            for (;;){
                try {
                   this.tasks.take().runTask();
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        });

        Thread cleaner = factory.newThread(()->{
            for (;;){
                try {
                    this.tasks.removeIf(task -> (System.currentTimeMillis() - task.getCommitTime()) > 50);
                    LockSupport.parkNanos(this,10000);
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        });

        cleaner.setName(cleaner.getName()+"@Worker-Cleaner");
        cleaner.start();

        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void execute(Runnable runnable) {
        this.tasks.add(new TaskEntry(runnable));
    }

    private static final class TaskEntry{
        private final Runnable task;
        private final long commitTime;

        public TaskEntry(Runnable task){
            this.task = task;
            this.commitTime = System.currentTimeMillis();
        }

        public void runTask(){
            this.task.run();
        }

        public long getCommitTime(){
            return this.commitTime;
        }
    }
}
