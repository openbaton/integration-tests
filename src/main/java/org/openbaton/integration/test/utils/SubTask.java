/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openbaton.integration.test.utils;

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



    public SubTask(){
        this.successors = new LinkedList<>();
        this.f=new LinkedList<>();
        successorRemover =null;
    }

    public void setParam(Object param){
        this.param = param;
    }
    public Object getParam(){
        return param;
    }
    public void setMaxIntegrationTestTime(int maxIntegrationTestTime){
        this.maxIntegrationTestTime = maxIntegrationTestTime;
    }

    public void setMaxConcurrentSuccessors(int maxConcurrentSuccessors){
        executorService = Executors.newFixedThreadPool(maxConcurrentSuccessors);
    }
    public void setSuccessorRemover(SubTask successorRemover){
        this.successorRemover = successorRemover;
    }

    protected abstract Object doWork() throws Exception;

    public void addSuccessor(SubTask e) {
       this.successors.add(e);
    }

    @Override
    public Object call() throws Exception {
        Object res = doWork();
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
                    result=false;break;
                }
            if (successorRemover !=null && result) {
                executorService.submit(successorRemover).get(60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();result=false;
        } catch (ExecutionException e) {
            e.printStackTrace();result=false;
        } catch (TimeoutException e) {
            e.printStackTrace();result=false;
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
