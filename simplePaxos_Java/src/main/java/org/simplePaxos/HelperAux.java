package org.simplePaxos;

import pt.unl.fct.di.novasys.babel.channels.events.OnStreamConnectionUpEvent;
import pt.unl.fct.di.novasys.network.data.Host;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

public class HelperAux {

    public static final MessageDigest md = getMessageDigest();

    public static MessageDigest getMessageDigest(){
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static String digest(String input){

        //Passing data to the created MessageDigest Object
        byte[] digest = md.digest(input.getBytes());

        //Converting the byte array in to HexString format
        StringBuffer hexString = new StringBuffer();

        for (int i = 0;i<digest.length;i++) {
            hexString.append(Integer.toHexString(0xFF & digest[i]));
        }

        return hexString.toString();
    }

    public static Set<Host> getNeighbors(String addresses) throws Exception{
        Set<Host> hosts = new HashSet<>();
        for (String fullAddress : addresses.split(",")) {
            String [] fulls = fullAddress.split(":");
            Host host = new Host(InetAddress.getByName(fulls[0]),Integer.parseInt(fulls[1]));
            hosts.add(host);
        }
        return hosts;
    }

    public static int getMajority(int peers){
        return (peers+1)/2;
    }
}
