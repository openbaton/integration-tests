#!/bin/bash

function usage
{
    echo "./integration-tests.sh [[-n] [-i] [-g] | [-a]]"
    echo ""
    echo "-i        update integration test"
    echo "-n        update nfvo"
    echo "-g        update generic vnfm"
    echo "-a        update all"
}

function get_property
{
    PROP_FILE=$1
    PROP_KEY=$2
    PROP_VALUE=`cat $PROP_FILE | grep "$PROP_KEY" | cut -d'=' -f2`

    echo $PROP_VALUE
}

function update_integrations
{
    pushd "/etc/openbaton/integration-tests"
    git pull
    ./generic-vnfm.sh clean compile start
    popd
}

function update_vnfm
{
    pushd "/etc/openbaton/generic-vnfm"
    git pull
    ./generic-vnfm.sh clean compile start
    popd
}

function update_nfvo
{
    pushd "/etc/openbaton/nfvo"
    git pull
    ./openbaton.sh clean compile start
    popd
}

echo "Starting integration tests"

SCENARIO_PATH=`get_property ./src/main/resources/integration-test.properties "integration-test-scenarios"`

if [[ $SCENARIO_PATH == '' ]]; then
  echo "SCENARIO_PATH is unset";
  SCENARIO_PATH="/opt/github/openbaton/integration-tests/src/main/resources/integration-test-scenarios/*.ini"
else
  echo "SCENARIO_PATH is set to '$SCENARIO_PATH'";
  SCENARIO_PATH="$SCENARIO_PATH*.ini"
fi

echo "Executing scenarios:"
for f in `ls $SCENARIO_PATH`;
do
    echo " " $f
done

while getopts habcf: opt
do
    case "$opt" in
    (a) echo "Updating all"
        update_nfvo
        update_vnfm
        update_integrations;;
    (n) echo "Updating nfvo"
        update_nfvo;;
    (i) echo "Updating integration tests"
        update_integrations;;
    (g) echo "Updating generic vnfm"
        update_vnfm;;
    esac
done

VERSION=`get_property ./gradle.properties "version"`

echo $VERSION
java -jar ./build/libs/integration-tests-$VERSION.jar