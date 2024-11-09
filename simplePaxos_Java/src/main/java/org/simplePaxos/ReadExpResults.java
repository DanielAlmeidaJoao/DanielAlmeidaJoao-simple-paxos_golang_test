package org.simplePaxos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class ReadExpResults {

    public static void main(String [] args) throws FileNotFoundException {
        String fileName = "results1NODE.txt";
        // clients TCP sendtype values
        Map<Integer, Map<String, Map<String, List<Float>>>> results = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] l = line.split(" ");
                int clients = Integer.parseInt(l[1]);
                String proto = l[0];
                String sendType = l[2];
                Float val = Float.parseFloat(l[3]);
                Map<String, Map<String, List<Float>>> protocolMap = results.computeIfAbsent(clients, key -> new HashMap<>());
                Map<String, List<Float>> sendTypes = protocolMap.computeIfAbsent(proto, k -> new HashMap<>());
                List<Float> values = sendTypes.computeIfAbsent(sendType, p -> new LinkedList<>());
                values.add(val);
            }

            for (Map.Entry<Integer, Map<String, Map<String, List<Float>>>> integerMapEntry : results.entrySet()) {
                Map<String, Map<String, List<Float>>> protoMap = integerMapEntry.getValue();
                for (Map.Entry<String, Map<String, List<Float>>> stringMapEntry : protoMap.entrySet()) {
                    Map<String, List<Float>> sendTypeMap = stringMapEntry.getValue();
                    for (Map.Entry<String, List<Float>> stringListEntry : sendTypeMap.entrySet()) {
                        String y = "";
                        if(stringListEntry.getKey().contains("StreamWithZeroCopy")){
                            y="y1 = ";
                        }
                        if(stringListEntry.getKey().contains("MessageConnection")){
                            y="y2 = ";
                        }
                        if(stringListEntry.getKey().contains("StreamWithMessages")){
                            y="y3 = ";
                        }
                        if(stringListEntry.getKey().contains("MessageConnection")){
                            y="y4 = ";
                        }
                        String name = y + stringMapEntry.getKey() + "_" + stringListEntry.getKey()+ integerMapEntry.getKey();
                        System.out.println(name + " = " + Arrays.toString(stringListEntry.getValue().toArray()));
                    }
                }
                System.out.println();
            }
        } catch (Exception e) {

        }
    }
}
