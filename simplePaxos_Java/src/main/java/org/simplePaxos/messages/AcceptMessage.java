package org.simplePaxos.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;

public class AcceptMessage extends ProtoMessage {
    public static final short ID = 230;

    public int proposalNum;
    public int term;
    public PaxosMessage paxosMessage;

    public AcceptMessage(int proposalNum, int term, PaxosMessage paxosMessage) {
        super(ID);
        this.proposalNum = proposalNum;
        this.term = term;
        this.paxosMessage = paxosMessage;
    }
    public AcceptMessage() {
        super(ID);
        this.proposalNum = -1;
        this.term = -1;
        this.paxosMessage = new PaxosMessage();
    }

    @Override
    public <V extends ProtoMessage> ProtoMessage getNewEmptyInstance() {
        return new AcceptMessage();
    }
}
