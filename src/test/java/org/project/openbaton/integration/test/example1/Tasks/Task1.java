package org.project.openbaton.integration.test.example1.Tasks;

import org.project.openbaton.integration.test.utils.SubTask;

import java.util.Properties;

/**
 * Created by mob on 18.08.15.
 */
public class Task1 extends SubTask {
    private Properties properties;

    public Task1(Properties properties){
        this.properties=properties;
    }

    @Override
    protected Object doWork() throws Exception {
        int i=0;
        i++;
        Thread.sleep(1000);
        System.out.println("Task1 id:"+Thread.currentThread().getId()+" , counter is: " + i);
        return i;
    }
}
