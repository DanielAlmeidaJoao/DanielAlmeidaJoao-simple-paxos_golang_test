package org.simplePaxos;

import pt.unl.fct.di.novasys.babel.channels.events.OnStreamConnectionUpEvent;
import pt.unl.fct.di.novasys.network.data.Host;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class HelperAux {

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
