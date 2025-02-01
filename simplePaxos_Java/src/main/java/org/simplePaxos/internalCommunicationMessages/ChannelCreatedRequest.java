package org.simplePaxos.internalCommunicationMessages;

import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;
import pt.unl.fct.di.novasys.network.data.Host;

public class ChannelCreatedRequest extends ProtoRequest {
    public static final short ID = 907;

    public final int channel;
    public final Host host;

    public ChannelCreatedRequest(int chan, Host host1){
        super(ID);
        channel = chan;
        this.host = host1;
    }
}
