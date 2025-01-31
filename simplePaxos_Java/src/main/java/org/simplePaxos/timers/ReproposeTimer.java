package org.simplePaxos.timers;

import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class ReproposeTimer extends ProtoTimer {
    public static final short ID = 132;
    private PaxosMessage paxosMessage;
    public ReproposeTimer(PaxosMessage paxosMessage){
        super(ID);
        this.paxosMessage = paxosMessage;
    }

    public PaxosMessage getPaxosMessage() {
        return paxosMessage;
    }

    @Override
    public ProtoTimer clone() {
        return null;
    }
}
