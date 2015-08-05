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
    protected List<SubTask> successors;
    private SubTask successorRemover;
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
        successorRemover =null;
    }
    public void setSuccessorRemover(SubTask sr){
        successorRemover = sr;
    }
    protected Object getResult() {
        Object result=null;
        try {
            result=doWork();
        } catch (Exception e){
            this.handleException(e);
        }
        return result;
    }

    protected abstract Object doWork() throws Exception;

    protected abstract void handleException(Exception e);

    public void addSuccessor(SubTask e) {
       this.successors.add(e);
    }

    @Override
    public Object call() {
        Object res = getResult();
        for (SubTask successor : successors)
            successor.setParam(res);
        if (successorRemover !=null)
        {
            successorRemover.setParam(res);
        }
        executeSuccessors();
        return res;
    }

    protected void executeSuccessors() {
        for (SubTask successor : successors) {
            log.debug("Executing successor: " + successor.getClass().getSimpleName());
            f.add(this.executorService.submit(successor));
        }
    }

    public void awaitTermination(int seconds) {
        try {
            for (Future future : f) {
                future.get(seconds, TimeUnit.SECONDS);
            }
            for (SubTask successor : successors)
                successor.awaitTermination(seconds);
            if (successorRemover !=null)
            {
                log.debug("Executing successorRemover: " + successorRemover.getClass().getSimpleName());
                executorService.submit(successorRemover).get(60, TimeUnit.SECONDS);
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
