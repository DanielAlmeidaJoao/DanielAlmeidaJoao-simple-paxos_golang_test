package org.simplePaxos.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;

public class DecidedMessage extends ProtoMessage {
    public static final short ID = 240;

    public int proposalNum;
    public int term;
    public PaxosMessage paxosMessage;

    public DecidedMessage(int proposalNum, int term, PaxosMessage paxosMessage) {
        super(ID);
        this.proposalNum = proposalNum;
        this.term = term;
        this.paxosMessage = paxosMessage;
    }
    public DecidedMessage() {
        super(ID);
    }

    @Override
    public <V extends ProtoMessage> ProtoMessage getNewEmptyInstance() {
        return null;
    }
}
