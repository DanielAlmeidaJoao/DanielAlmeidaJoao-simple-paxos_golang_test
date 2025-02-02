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

    public PaxosMessage(String msgValue, String msgId) {
        super(ID);
        this.msgValue = msgValue;
        this.msgId = msgId;
    }
    public PaxosMessage() {
        super(ID);
    }

    public PaxosMessage(short id) {
        super(id);
    }
}
