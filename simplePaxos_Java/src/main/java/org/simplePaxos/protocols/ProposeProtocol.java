package org.simplePaxos.protocols;

import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;

import java.io.IOException;
import java.util.Properties;

public class ProposeProtocol extends GenericProtocolExtension {
    public ProposeProtocol(String protoName, short protoId) {
        super(protoName, protoId);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {

    }
}
