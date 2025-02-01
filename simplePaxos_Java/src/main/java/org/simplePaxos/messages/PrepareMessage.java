package org.simplePaxos.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;

public class PrepareMessage extends ProtoMessage {
    public static final short ID = 210;

    public int proposalNum;
    public int term;

    public PrepareMessage(int proposalNum, int term) {
        super(ID);
        this.proposalNum = proposalNum;
        this.term = term;
    }

    public PrepareMessage(){
        super(ID);
        proposalNum = 0;
        term = 0;
    }
}
