package org.simplePaxos.timers;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class HashResultPrinterTimer extends ProtoTimer {
    public static final short ID = 454;

    public HashResultPrinterTimer(){
        super(ID);
    }
    @Override
    public ProtoTimer clone() {
        return null;
    }
}
