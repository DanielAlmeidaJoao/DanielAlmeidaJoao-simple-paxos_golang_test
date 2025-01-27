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

    public static ISerializer<AcceptMessage> serializer = new ISerializer<AcceptMessage>() {

        @Override
        public void serialize(AcceptMessage acceptMessage, ByteBuf byteBuf) throws IOException {
            byteBuf.writeInt(acceptMessage.proposalNum);
            byteBuf.writeInt(acceptMessage.term);
            PaxosMessage.serializer.serialize(acceptMessage.paxosMessage,byteBuf);
        }

        @Override
        public AcceptMessage deserialize(ByteBuf byteBuf) throws IOException {
            return new AcceptMessage(byteBuf.readInt(),byteBuf.readInt(),PaxosMessage.serializer.deserialize(byteBuf));
        }
    };

    @Override
    public <V extends ProtoMessage> ProtoMessage getNewEmptyInstance() {
        return new AcceptMessage();
    }
}
