#!/bin/bash

function usage
{
    echo "./integration-tests.sh [list] [help] [clean|compile|start|sc] [scenario_1.ini ... scenario_n.ini]"
    echo ""
    echo "build-clean         clean the project build"
    echo "compile             compile the integration-test project"
    echo "help                prints this help; if passed as an argument all other arguments will be ignored"
    echo "list                lists all available test scenarios; if passed as an argument clean, compile, start and sc arguments will be ignored"
    echo "sc                  clean, compile and start in one command"
    echo "scenario_n.ini      the tests to run if the start argument is passed; if no test is specified all available tests will run"
    echo "start               start the integration tests"
}

function get_property
{
    PROP_FILE=$1
    PROP_KEY=$2
    PROP_VALUE=`cat $PROP_FILE | grep "$PROP_KEY" | cut -d'=' -f2`

    echo $PROP_VALUE
}

function output_available_scenarios
{
  for f in `ls $SCENARIO`;
  do
    echo $f | grep "\.ini$"
  done
}

#EXTERNAL_PROPERTIES=`get_property ./src/main/resources/integration-tests.properties "external-properties-file"`

#if [ -f $EXTERNAL_PROPERTIES ]; then
#  SCENARIO_PATH=`get_property $EXTERNAL_PROPERTIES "integration-test-scenarios"`
#else
SCENARIO_PATH=`get_property ./src/main/resources/integration-tests.properties "integration-test-scenarios"`
#fi

if [ -z $SCENARIO_PATH ] || [ ! -d $SCENARIO_PATH ] || [ `ls -1 $SCENARIO_PATH | grep "\.ini$" | wc -l` -eq 0 ]; then
  SCENARIO_PATH='./src/main/resources/integration-test-scenarios/'
fi

VERSION=`get_property ./gradle.properties "version"`

cleann=false
compile=false
start=false
print_help=false
list=false

args=("$@")

for i in `seq 0 ${#args}`; do
  case "${args[$i]}" in
    build-clean)
      cleann=true
      args[$i]="";;
    compile) 
      compile=true
      args[$i]="";;
    start) 
      start=true
      args[$i]="";;
    sc) 
      cleann=true
      compile=true
      start=true
      args[$i]="";;
    list) 
      list=true;;
    help)
      print_help=true;;
  esac 
done

if [ $print_help == true ]; then
  usage
  exit 0
fi

if [ $list == true ]; then
  output_available_scenarios
fi

if [ $cleann == true ]; then
  ./gradlew clean
fi

if [ $compile == true ]; then
  ./gradlew build -x test
fi

if [ $start == true ]; then
  java -jar ./build/libs/integration-tests-$VERSION.jar ${args[*]}
  exit $?
fi

if [ $print_help == false -a $list == false -a $cleann == false -a $compile == false -a $start == false ]; then
  usage
fi
