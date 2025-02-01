package org.simplePaxos.internalCommunicationMessages;

import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;


public class ProposeRequest extends ProtoRequest {

    public final static short ID = 435;
    private PaxosMessage paxosMessage;

    public ProposeRequest(PaxosMessage paxosMessage){
        super(ID);
        this.paxosMessage = paxosMessage;
    }
    public PaxosMessage getPaxosMessage(){
        return paxosMessage;
    }


}
