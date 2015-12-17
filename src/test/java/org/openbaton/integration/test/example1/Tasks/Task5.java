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
package org.openbaton.integration.test.example1.Tasks;

import org.openbaton.integration.test.utils.SubTask;

import java.util.Properties;

/**
 * Created by mob on 18.08.15.
 */
public class Task5 extends SubTask {

    private Properties properties;

    public Task5(Properties properties){
        this.properties=properties;
    }

    @Override
    protected Object doWork() throws Exception {
        int i = (int) getParam();
        i++;
        Thread.sleep(1000);
        System.out.println("Task5 id:"+Thread.currentThread().getId()+", counter is: "+i);
        return i;
    }
}
