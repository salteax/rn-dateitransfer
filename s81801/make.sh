#!/bin/bash

# bash script to compile java file of server & client

# compilation of server
echo "Compiling 'Server.java'.."
javac $(dirname "$0")/src/Server.java -d $(dirname "$0")/bin/
if [ $? -ne 0 ];
then
    echo "Could not compile 'Server.java', exiting.."
    exit 1
else
    echo "Compilation successful, binary file created at '$(dirname "$0")/bin/'.."
fi

# compilation of client
echo "Compiling 'Client.java'.."
javac $(dirname "$0")/src/Client.java -d $(dirname "$0")/bin/
if [ $? -ne 0 ];
then
    echo "Could not compile 'Client.java', exiting.."
    exit 1
else
    echo "Compilation successful, binary file created at '$(dirname "$0")/bin/'.."
fi