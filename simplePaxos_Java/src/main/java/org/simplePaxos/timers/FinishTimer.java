package org.simplePaxos.timers;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class FinishTimer extends ProtoTimer {
    public static final short ID = 31;

    public FinishTimer(){
        super(ID);
    }

    @Override
    public ProtoTimer clone() {
        return null;
    }
}
