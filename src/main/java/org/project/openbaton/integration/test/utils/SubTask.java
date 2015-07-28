package org.project.openbaton.integration.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by mob on 24.07.15.
 */
public abstract class SubTask implements Callable<Object>{

    private ExecutorService executorService;
    private List<SubTask> successors;
    private List<Future> f;
    public Object param;
    protected static final Logger log = LoggerFactory.getLogger(SubTask.class);

    public void setParam(Object param){
        this.param = param;
    }

    public SubTask(int successors){
        this.successors = new LinkedList<>();
        this.f=new LinkedList<>();
        executorService = Executors.newFixedThreadPool(successors);
    }

    private Object getResult() throws Exception {
        try {
            return doWork();
        } catch (Exception e){
            this.handleException(e);
            throw e;
        }
    }

    protected abstract Object doWork() throws Exception;

    protected abstract void handleException(Exception e);

    public void addSuccessor(SubTask e) {
       this.successors.add(e);
    }


    @Override
    public Object call() throws Exception {
        Object res = getResult();
        for (SubTask successor : successors)
            successor.setParam(res);
        executeSuccessors();
        return res;
    }

    private void executeSuccessors() {
        for (SubTask successor : successors) {
            f.add(this.executorService.submit(successor));
        }
    }

    public void awaitTermination() {
        try {
            for (Future future : f) {
                future.get(60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.error("The thread was interrupted while waiting for successors");
            e.printStackTrace();
        } catch (ExecutionException e) {
            log.error("The computation of a successor threw an exception");
            e.printStackTrace();
        } catch (TimeoutException e) {
            log.error("The wait of the thread timed out");
            e.printStackTrace();
        }
        for (SubTask successor : successors)
            successor.awaitTermination();
        shutdownAndAwaitTermination();
    }
    private void shutdownAndAwaitTermination() {
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("ExecutorService did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
