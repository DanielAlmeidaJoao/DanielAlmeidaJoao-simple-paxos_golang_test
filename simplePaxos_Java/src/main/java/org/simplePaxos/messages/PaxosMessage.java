package org.simplePaxos.messages;

import io.netty.buffer.ByteBuf;
import org.simplePaxos.helperFiles.AuxiliaryMethods;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;

public class PaxosMessage extends ProtoMessage {
    public static final short ID = 200;

    public String msgId;
    public String msgValue;
    public int proposalNum;
    public int term;
    public int decidedCount;

    public PaxosMessage(String msgValue, String msgId, int proposalNum, int term, int decidedCount) {
        super(ID);
        this.msgValue = msgValue;
        this.msgId = msgId;
        this.proposalNum = proposalNum;
        this.term = term;
        this.decidedCount = decidedCount;
    }
    public PaxosMessage() {
        super(ID);
    }

    public PaxosMessage(short id) {
        super(id);
    }

    @Override
    public <V extends ProtoMessage> ProtoMessage getNewEmptyInstance() {
        return new PaxosMessage();
    }

}
