package org.simplePaxos.internalCommunicationMessages;

import org.simplePaxos.messages.PaxosMessage;


public class ProposeRequest {

    public final static short ID = 435;
    private PaxosMessage paxosMessage;

    public ProposeRequest(PaxosMessage paxosMessage){
        this.paxosMessage = paxosMessage;
    }
    public PaxosMessage getPaxosMessage(){
        return paxosMessage;
    }


}
