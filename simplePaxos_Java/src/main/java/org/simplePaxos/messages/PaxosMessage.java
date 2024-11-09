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

    public PaxosMessage(short id) {
        super(id);
    }

    public static ISerializer<PaxosMessage> serializer = new ISerializer<PaxosMessage>() {

        @Override
        public void serialize(PaxosMessage paxosMessage, ByteBuf byteBuf) throws IOException {

            AuxiliaryMethods.writeString(paxosMessage.msgValue,byteBuf);
            AuxiliaryMethods.writeString(paxosMessage.msgId,byteBuf);
            byteBuf.writeInt(paxosMessage.proposalNum);
            byteBuf.writeInt(paxosMessage.term);
            byteBuf.writeInt(paxosMessage.decidedCount);

        }

        @Override
        public PaxosMessage deserialize(ByteBuf byteBuf) throws IOException {
            //String msgValue, String msgId, int proposalNum, int term, int decidedCount
            return new PaxosMessage(
                    AuxiliaryMethods.readString(byteBuf),
                    AuxiliaryMethods.readString(byteBuf),
                    byteBuf.readInt(),
                    byteBuf.readInt(),
                    byteBuf.readInt()
            );
        }
    };


}
