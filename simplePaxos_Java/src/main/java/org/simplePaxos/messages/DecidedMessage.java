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

    public static ISerializer<DecidedMessage> serializer = new ISerializer<DecidedMessage>() {

        @Override
        public void serialize(DecidedMessage acceptMessage, ByteBuf byteBuf) throws IOException {
            byteBuf.writeInt(acceptMessage.proposalNum);
            byteBuf.writeInt(acceptMessage.term);
            PaxosMessage.serializer.serialize(acceptMessage.paxosMessage,byteBuf);
        }

        @Override
        public DecidedMessage deserialize(ByteBuf byteBuf) throws IOException {
            return new DecidedMessage(byteBuf.readInt(),byteBuf.readInt(),PaxosMessage.serializer.deserialize(byteBuf));
        }
    };

}
