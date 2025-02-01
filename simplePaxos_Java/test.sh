clear

java -cp simplePaxos.jar Main address=127.0.0.1 port=8080 &
java -cp simplePaxos.jar Main address=127.0.0.1 port=8081 &
java -cp simplePaxos.jar Main address=127.0.0.1 port=8082 &

# Wait for user input
echo "Press Enter to terminate all processes"
read -r

# Terminate all background processes
pkill -f "java 127.0.0.1"
