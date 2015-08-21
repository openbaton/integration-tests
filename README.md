Integration Test
======

### Description
The Integration Test project can be used to test a tree or a graph of consecutive tasks. Each task may obtain/return an object before/after the execution.
To implement a new task, you need to extend the SubTask abstract class and implement the method doWork() with your logic.
In the doWork() method you can get the result of the father's execution as an object by the method getParam(). Since the SubTask
class implements Callable<Object> you can return an object at the end of the method doWork().
You can describe the tree/graph of the tasks through the ini file.

### Simple example

We have four tasks to execute in the following order:

![operations flow](flowExample.png)

We will create the classes:
- Task1
- Task2
- Task3
- Task4
- Task5
- Task6

All of them extend SubTask and implement the method doWork(). The costructor of each classes accept only Properties.
Example of Task2:
```java
public class Task2 extends SubTask{

public Task2(Properties properties){
  //..
}

@Override
protected Object doWork() throws Exception {
  //Get parameter from Task1
  Object param = getParam();

  //.. do work ..

  // return an object that Task3 will get with getParam()...
  return anObject;
}
}
```

Then we create the following ini file:
```
[it]
;set the maximum time (in seconds) of the Integration test. e.g. 10 min = 600 seconds
max-integration-test-time = 600
;set the maximum number of concurrent successors (max number of active child threads)
max-concurrent-successors = 10

[it/task1-1]
class-name = Task1
successor-remover = task6-1

[it/task1-1/task6-1]
class-name = Task6

[it/task1-1/task2-1]
class-name = Task2
num_instances = 2
successor-remover = task5-1

[it/task1-1/task2-1/task5-1]
class-name = Task5

[it/task1-1/task2-1/task3-1]
class-name = Task3
num_instances = 2
specific-parameter = 14.3

[it/task1-1/task2-1/task3-1/task4-1]
class-name = Task4
```

In the main you can use the class IntegrationTestManager to start the scenario:
```java
// File f = loadFileIni("file.ini");
// Properties properties = loadProperties();

IntegrationTestManager itm = new IntegrationTestManager("basepath of the tasks") {
	@Override
	protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
			if(subTask instanceof Task3){
			  Task3 t3 = (Task3) subTask;
			  // set a parameter specificated in the ini file in the section [it/task1-1/task2-1/task3-1]
			  // If "specific-parameter" is not present in the ini file, "5" will be the default
			  t3.setSpecificParameter(Double.parseDouble(currentSection.get("specific-parameter", "5")));
			}
	}};
boolean result = itm.runTestScenario(properties, file);
```

### Integration Test for Openbaton

### Prerequisites

* The related project [OpenBatonNFVO](https://gitlab.fokus.fraunhofer.de/neutrino-dev/nfvo) must be installed, please see [the README](https://gitlab.fokus.fraunhofer.de/neutrino-dev/nfvo/blob/master/README.md) file

### Description

The Integration Test project is used to test the correctness of the operations carried out by NFVO and VNFM.
The main operations to be tested are the creation and deletion of entities.
The entities are:
* network service description (nsd)
* network service record (nsr)
* vim instance (vim)

To create a realistic scenario the above operations must follow an order. Example:
- vim create
  - nsd create (one or more)
    - nsr create (one or more)
    - waiting for the end of creation/s
    - nsr delete (all)
    - waiting for the end of deletion/s
  - nsd delete (all)
- vim delete

As you can see the flow of operations is a graph that begin with the vim creation and ends with the vim deletion.
To create your own test, you must specify the flow of operations in the configuration file (.ini).


In the ini file you can specify your own flow of operations as a graph.
Example of ini file:
```
[it]
;set the maximum time (in seconds) of the Integration test. e.g. 10 min = 600 seconds
max-integration-test-time = 600
;set the maximum number of concurrent successors (max number of active child threads)
max-concurrent-successors = 10

;vimInstance-create
[it/vim-c-1]
class-name = VimInstanceCreate
name-file = vim-instance.json

;nsd-create
[it/vim-c-1/nsd-c-1]
class-name = NetworkServiceDescriptorCreate
num_instances = 2
successor-remover = nsd-d-1
name-file = NetworkServiceDescriptor.json

;nsd-delete
[it/vim-c-1/nsd-c-1/nsd-d-1]
class-name = NetworkServiceDescriptorDelete

;nsr-create
[it/vim-c-1/nsd-c-1/nsr-c-1]
class-name = NetworkServiceRecordCreate
num_instances = 2

;nsr-wait
[it/vim-c-1/nsd-c-1/nsr-c-1/nsr-w-1]
class-name = NetworkServiceRecordWait
;the default timeout is 5 seconds
timeout = 500
action = INSTANTIATE_FINISH

;nsr-delete
[it/vim-c-1/nsd-c-1/nsr-c-1/nsr-w-1/nsr-d-1]
class-name = NetworkServiceRecordDelete

;nsr-wait
[it/vim-c-1/nsd-c-1/nsr-c-1/nsr-w-1/nsr-d-1/nsr-w-1]
class-name = NetworkServiceRecordWait
;the default timeout is 5 seconds
timeout = 500
action = RELEASE_RESOURCES_FINISH
```
You will obtain the following flow of operations:

![operations flow](flow.png)

The tasks that create entities from json file (i.e. VimInstanceCreate, NetworkServiceDescriptorCreate) use a parser. The static
parser class Parser is described below.

### Parser
The class Parser looks for a configuration file with this sintax:

old_value = new_value

In the json file, passed to the method Parser.randomize(), all the old_value will be replace with new_value.
IMPORTANT: in the json file, the old_value must have the following sintax:

"some_parameter" = "<::old_value::>"

If we want to put random values:

old_value = new_value***

In the json file, passed to the method Parser.randomize(), all the old_value will be replace
with new_value plus 3 random characters (e.g. new_valuezxd).

### Simple parser example
Parser config file (parser.config):
```
admin=admin***
```
Json file:
```
{
"username":"<::admin::>"
}
```
Use of Parser class:
```java
String newJson = Parser.randomize(oldJson, "parser.config");
```
The string newJson will be:
```
{
"username":"adminxkz"
}
```
### Development

* Want to contribute? Great!

### News and Website

* Information about OpenBaton can be found on our website. Follow us on Twitter @openbaton.

### License