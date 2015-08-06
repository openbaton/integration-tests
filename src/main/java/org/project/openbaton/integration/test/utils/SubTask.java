package org.project.openbaton.integration.test.utils;

import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.sdk.api.exception.SDKException;
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
    private int maxIntegrationTestTime;
    public Object param;
    protected static final Logger log = LoggerFactory.getLogger(SubTask.class);

    public void setParam(Object param){
        this.param = param;
    }

    public void setMaxIntegrationTestTime(int maxIntegrationTestTime){
        this.maxIntegrationTestTime = maxIntegrationTestTime;
    }

    public SubTask(int successors){
        this.successors = new LinkedList<>();
        this.f=new LinkedList<>();
        executorService = Executors.newFixedThreadPool(successors);
        successorRemover =null;
    }

    public void setSuccessorRemover(SubTask successorRemover){
        this.successorRemover = successorRemover;
    }

    protected Object getResult() throws SDKException, IntegrationTestException {
        return doWork();
    }

    protected abstract Object doWork() throws SDKException, IntegrationTestException;

    public void addSuccessor(SubTask e) {
       this.successors.add(e);
    }

    @Override
    public Object call() throws SDKException, IntegrationTestException {
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
            //log.debug("Executing successor: " + successor.getClass().getSimpleName());
            f.add(this.executorService.submit(successor));
        }
    }

    public boolean awaitTermination() {
        boolean result=true;
        try {
            for (Future future : f) {
                future.get(maxIntegrationTestTime, TimeUnit.SECONDS);
            }
            for (SubTask successor : successors)
                if(!successor.awaitTermination()){
                   result=false;
                   break;
                }
            if (successorRemover !=null && result) {
                log.debug("Executing successorRemover: " + successorRemover.getClass().getSimpleName());
                executorService.submit(successorRemover).get(60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.error("The thread was interrupted while waiting for successors");
            e.printStackTrace();
            result=false;
        } catch (ExecutionException e) {
            log.error("The computation of a successor threw an exception the message is: "+ e.getMessage(),e);
            result=false;
        } catch (TimeoutException e) {
            log.error("Max Integration Test timeout is finished");
            e.printStackTrace();
            result=false;
        }
        finally {
            shutdownAndAwaitTermination();
        }
        return result;
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
