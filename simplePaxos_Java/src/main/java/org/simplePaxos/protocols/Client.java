package org.simplePaxos.protocols;

import org.simplePaxos.internalCommunicationMessages.LearnRequest;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;

import java.io.IOException;
import java.util.Properties;

public class Client extends GenericProtocolExtension {
    public static final short ID = 763;
    public Client(String protoName, short protoId) {
        super(protoName, protoId);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {

    }


    @RequestHandlerAnnotation(REQUEST_ID = ID)
    public void onRequest(LearnRequest request, short from){

    }
}
