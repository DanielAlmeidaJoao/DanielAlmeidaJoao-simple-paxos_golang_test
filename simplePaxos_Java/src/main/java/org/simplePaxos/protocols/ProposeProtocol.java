package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.ChannelCreatedRequest;
import org.simplePaxos.internalCommunicationMessages.ProposeRequest;
import org.simplePaxos.messages.*;
import org.simplePaxos.timers.ProposeTimer;
import org.simplePaxos.timers.ReproposeTimer;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.MessageInHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.TimerEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.internal.MessageInEvent;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class ProposeProtocol extends GenericProtocolExtension {

    private static final Logger logger = LogManager.getLogger(ProposeProtocol.class);

    public static final short ID = 123;

    private PaxosMessage currentValue;
    private PromiseMessage highestPromise;

    private Set<Host> peers;
    private int proposal_num;
    private int acks;
    private int promises;
    private int currentTerm;
    private Host self;
    private Random random;

    public ProposeProtocol(String protoName, short protoId) {
        super(protoName, protoId);
        currentTerm = 1;
        promises = 0;
        peers = new HashSet<>();
        random = new Random();
        proposal_num = 0;
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
    }

    @TimerEventHandlerAnnotation(TIMER_ID = ProposeTimer.ID)
    public void timeHandler(long timer){
        ProposeRequest request = new ProposeRequest(currentValue,proposal_num,currentTerm);
        onProposeRequest(request,ID);
    }
    @RequestHandlerAnnotation(REQUEST_ID = ProposeRequest.ID)
    private void onProposeRequest(ProposeRequest request, short from){
        PaxosMessage toPropose = request.getPaxosMessage();

        if(request.getPaxosMessage() == null || currentTerm > request.term){
            return;
        }
        setupTimer(ProposeTimer.ID,200);
        promises = 0;
        acks = 0;
        highestPromise = null;

        currentValue = toPropose;
        //logger.info(self+ "__ GOING TO PROPOSE "+HelperAux.gson.toJson(toPropose));

        currentTerm = request.term;

        proposal_num += (1+random.nextInt(10));

        PrepareMessage prepareMessage = new PrepareMessage(proposal_num,request.term);

        for (Host peer : peers) {
            sendMessage(prepareMessage,AcceptProtocol.PROTO_ID,peer);
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = PromiseMessage.ID)
    public void onPromiseMessage(MessageInEvent event, PromiseMessage promiseMessage){
        if (promiseMessage.term == currentTerm && proposal_num==promiseMessage.promisedNum){
            if (highestPromise == null || promiseMessage.acceptedNum>highestPromise.acceptedNum){
                highestPromise = promiseMessage;
            }
            promises++;
            if (promises == HelperAux.getMajority(peers.size())){
                promises=0;
                highestPromise.promisedNum = proposal_num;
                if(highestPromise.acceptedValue == null){
                    highestPromise.acceptedValue = currentValue;
                }

                AcceptMessage acceptMessage = new AcceptMessage(proposal_num,currentTerm,highestPromise.acceptedValue);
                for (Host peer : peers) {
                    sendMessage(acceptMessage,AcceptProtocol.PROTO_ID,peer);
                }
            }
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = AcceptMessage.ID)
    public void onAccepted(MessageInEvent event, AcceptMessage acceptMessage){
        if (currentTerm == acceptMessage.term && proposal_num == acceptMessage.proposalNum){
            acks++;
            if (acks == HelperAux.getMajority(peers.size())){
                acks = 0;
                DecidedMessage decidedMessage = new DecidedMessage(proposal_num,currentTerm,acceptMessage.paxosMessage);
                currentTerm++;
                if(currentValue != null &&  highestPromise.acceptedValue.msgId.equals(currentValue.msgId)){
                    currentValue=null;
                }
                highestPromise=null;
                for (Host peer : peers) {
                    sendMessage(decidedMessage,LearnProto.ID,peer);
                }
            }
        }
    }

    @ChannelEventHandlerAnnotation(EVENT_ID = OnConnectionDownEvent.EVENT_ID)
    private void uponConnectionDown(OnConnectionDownEvent event, int channelId) {
        logger.info("CONNECTION DOWN: {} {} {}",event.connectionId,event.getNode(),event.type);
        peers.remove(event.getNode());
    }

    @ChannelEventHandlerAnnotation(EVENT_ID = OnMessageConnectionUpEvent.EVENT_ID)
    private void uponMessageConnectionUp(OnMessageConnectionUpEvent event, int channelId) {
        logger.info("{} MESSAGE CONNECTION TO {} IS UP.",channelId,event.getNode());
        peers.add(event.getNode());
    }

    @RequestHandlerAnnotation(REQUEST_ID = ChannelCreatedRequest.ID)
    public void onChannelCreated(ChannelCreatedRequest request, short from){
        logger.info("CHANNEL CREATED "+request.channel);
        self = request.host;
        registerSharedChannel(request.channel);
    }

}


