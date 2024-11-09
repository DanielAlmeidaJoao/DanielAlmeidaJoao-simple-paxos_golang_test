package org.simplePaxos.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;

public class PromiseMessage extends ProtoMessage {
    public static final short ID = 220;

    public int acceptedNum;
    public int promisedNum;
    public int term;

    public PaxosMessage acceptedValue;

    public PromiseMessage(int acceptedNum, int promisedNum, int term, PaxosMessage acceptedValue) {
        super(ID);
        this.acceptedNum = acceptedNum;
        this.promisedNum = promisedNum;
        this.term = term;
        this.acceptedValue = acceptedValue;
    }

    public static ISerializer<PromiseMessage> serializer = new ISerializer<PromiseMessage>() {

        @Override
        public void serialize(PromiseMessage promiseMessage, ByteBuf byteBuf) throws IOException {
            byteBuf.writeInt(promiseMessage.acceptedNum);
            byteBuf.writeInt(promiseMessage.promisedNum);
            byteBuf.writeInt(promiseMessage.term);
            PaxosMessage.serializer.serialize(promiseMessage.acceptedValue,byteBuf);
        }

        @Override
        public PromiseMessage deserialize(ByteBuf byteBuf) throws IOException {
            return new PromiseMessage(
                    byteBuf.readInt(),
                    byteBuf.readInt(),
                    byteBuf.readInt(),
                    PaxosMessage.serializer.deserialize(byteBuf)
            );
        }
    };

}
