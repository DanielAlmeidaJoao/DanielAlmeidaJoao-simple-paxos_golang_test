package org.simplePaxos.internalCommunicationMessages;

import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;


public class ProposeRequest extends ProtoRequest {

    public final static short ID = 435;
    private PaxosMessage paxosMessage;
    public int proposalNum;
    public int term;

    public ProposeRequest(PaxosMessage paxosMessage, int proposalNum, int term){
        super(ID);
        this.paxosMessage = paxosMessage;
        this.proposalNum = proposalNum;
        this.term = term;
    }
    public PaxosMessage getPaxosMessage(){
        return paxosMessage;
    }


}
