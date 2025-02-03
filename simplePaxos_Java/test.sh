#!/bin/bash

clear
# Start processes in the background and store their PIDs
java -cp simplePaxos.jar Main address=127.0.0.1 port=8080 &  
PID1=$!  
java -cp simplePaxos.jar Main address=127.0.0.1 port=8081 &  
PID2=$!  
java -cp simplePaxos.jar Main address=127.0.0.1 port=8082 &  
PID3=$!  

# Wait for user input
echo "Press Enter to terminate all processes"
read -r  

# Kill all background processes
kill $PID1 $PID2 $PID3

echo "All processes terminated."