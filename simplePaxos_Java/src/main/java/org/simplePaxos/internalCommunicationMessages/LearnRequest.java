package org.simplePaxos.internalCommunicationMessages;

import org.simplePaxos.messages.DecidedMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

public class LearnRequest extends ProtoRequest {
    public DecidedMessage decidedMessage;
    public static final short REQUEST_ID = 200;
    public LearnRequest(DecidedMessage message) {
        super(REQUEST_ID);
        decidedMessage = message;
    }
}
