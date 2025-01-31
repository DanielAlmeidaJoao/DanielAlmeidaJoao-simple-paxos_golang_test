package org.simplePaxos.protocols;

import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.LearnRequest;
import org.simplePaxos.messages.DecidedMessage;
import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.MessageInHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.handlers.ReplyHandler;
import pt.unl.fct.di.novasys.babel.internal.MessageInEvent;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class LearnProto extends GenericProtocolExtension {

    private static Logger logger = Logger.getLogger(LearnProto.class.getName());
    private PaxosMessage paxosMessage;
    private Host self;
    private int currentTerm;
    private int totalReceived;
    private Map<Integer,DecidedMessage> decidedMessages;
    private Set<Host> peers;


    public LearnProto(String protoName, short protoId) {
        super(protoName, protoId);

    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        totalReceived = 0;
        String port = properties.getProperty("NETWORK_PORT");
        String address = properties.getProperty("NETWORK_ADDRESS");
        self = new Host(InetAddress.getByName(address),Integer.parseInt(port));
        currentTerm = 1;
        decidedMessages = new HashMap<>();
        try {
            peers = HelperAux.getNeighbors(properties.getProperty("NETWORK_PEERS"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = DecidedMessage.ID)
    public void onDecided(MessageInEvent event, DecidedMessage decidedMessage){
        totalReceived++;
        if (decidedMessage.paxosMessage.term >= currentTerm){
            DecidedMessage aux = decidedMessages.get(decidedMessage.term);
            if (aux == null){
                decidedMessages.put(decidedMessage.term,decidedMessage);
                aux = decidedMessage;
            }

            while (aux != null && aux.term == decidedMessage.term) {
                decidedMessages.remove(aux.term);
                currentTerm++;
                final DecidedMessage notification = aux;
                LearnRequest learnRequest = new LearnRequest(notification);
                super.sendRequest(learnRequest,Client.ID);
                aux = decidedMessages.get(currentTerm);
            }

        }

    }

    @ChannelEventHandlerAnnotation(EVENT_ID = OnMessageConnectionUpEvent.EVENT_ID)
    public void onConnectionUp(OnMessageConnectionUpEvent event, int channelId){
        peers.add(event.getNode());
        logger.info( self + ". CONNECTION IS UP: "+event.getNode());
    }
    @ChannelEventHandlerAnnotation(EVENT_ID = OnConnectionDownEvent.EVENT_ID)
    public void onConnectionDown(OnConnectionDownEvent event, int channelId){
        peers.remove(event.getNode());
        logger.info( self + ". CONNECTION IS DOWN: "+event.getNode());
    }


}
