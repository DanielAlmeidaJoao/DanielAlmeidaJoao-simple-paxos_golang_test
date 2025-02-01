package org.simplePaxos.timers;

import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class ProposeTimer extends ProtoTimer {
    public static final short ID = 324;

    public ProposeTimer(){
        super(ID);
    }

    @Override
    public ProtoTimer clone() {
        return null;
    }
}
