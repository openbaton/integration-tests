package org.project.openbaton.integration.test.example1.Tasks;

import org.project.openbaton.integration.test.utils.SubTask;

import java.util.Properties;

/**
 * Created by mob on 18.08.15.
 */
public class Task3 extends SubTask {
    private double specificParameter;
    private Properties properties;

    public Task3(Properties properties){
        this.properties=properties;
        this.specificParameter=0;
    }

    @Override
    protected Object doWork() throws Exception {
        int i = (int) getParam();
        i++;
        Thread.sleep(1000);
        System.out.println("Task3 id:" + Thread.currentThread().getId() + ", counter is: " + i);
        System.out.println("Task3 id:"+Thread.currentThread().getId()+", specificParameter is: "+specificParameter);
        return i;
    }
    public void setSpecificParameter(double specificParameter){
        this.specificParameter=specificParameter;
    }
}
