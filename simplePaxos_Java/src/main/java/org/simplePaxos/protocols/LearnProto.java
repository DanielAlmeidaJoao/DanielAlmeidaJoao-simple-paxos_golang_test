package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.ChannelCreatedRequest;
import org.simplePaxos.internalCommunicationMessages.LearnRequest;
import org.simplePaxos.messages.DecidedMessage;
import org.simplePaxos.messages.PaxosMessage;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.MessageInHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.handlers.ReplyHandler;
import pt.unl.fct.di.novasys.babel.internal.MessageInEvent;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class LearnProto extends GenericProtocolExtension {

    private static final Logger logger = LogManager.getLogger(LearnProto.class);
    public static final short ID = 972;

    private PaxosMessage paxosMessage;
    private Host self;
    private int currentTerm;
    private int totalReceived;
    private Map<Integer,DecidedMessage> decidedMessages;
    private Set<Host> peers;


    public LearnProto(String protoName, short protoId) {
        super(protoName, protoId);
        peers = new HashSet<>();
        currentTerm = 1;
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        totalReceived = 0;
        currentTerm = 1;
        decidedMessages = new HashMap<>();
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = DecidedMessage.ID)
    public void onDecided(MessageInEvent event, DecidedMessage decidedMessage){
        totalReceived++;
        if (decidedMessage.term >= currentTerm){
            DecidedMessage aux = decidedMessage;
            decidedMessages.put(decidedMessage.term,decidedMessage);

            while (aux != null && aux.term == currentTerm) {
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

    @RequestHandlerAnnotation(REQUEST_ID = ChannelCreatedRequest.ID)
    public void onChannelCreated(ChannelCreatedRequest request, short from){
        logger.info("CHANNEL CREATED "+request.channel);
        self = request.host;
        registerSharedChannel(request.channel);
    }


}
