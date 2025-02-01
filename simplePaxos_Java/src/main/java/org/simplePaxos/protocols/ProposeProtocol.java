package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.ChannelCreatedRequest;
import org.simplePaxos.internalCommunicationMessages.ProposeRequest;
import org.simplePaxos.messages.*;
import org.simplePaxos.timers.ReproposeTimer;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.MessageInHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
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
    private PaxosMessage highestPromise;
    private PaxosMessage acceptValueCount;

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
        registerTimerHandler(ReproposeTimer.ID, this::reproposeTimer);
    }

    @RequestHandlerAnnotation(REQUEST_ID = ProposeRequest.ID)
    private void onProposeRequest(ProposeRequest request, short from){
        PaxosMessage toPropose = request.getPaxosMessage();
        if(request.getPaxosMessage() == null){
            return;
        }
        currentValue = toPropose;
        logger.info(self+ "__ GOING TO PROPOSE "+HelperAux.gson.toJson(toPropose));

        currentTerm = request.getPaxosMessage().term;
        if(toPropose.msgId == null){
            logger.info(self + " NOT GOING TO PROPOSE "+toPropose.msgId);
            return;
        }

        proposal_num = toPropose.proposalNum + (1+random.nextInt(100));
        toPropose.proposalNum = proposal_num;
        highestPromise = acceptValueCount = null;
        PrepareMessage prepareMessage = new PrepareMessage(proposal_num,toPropose.term);
        promises = 0;
        acks = 0;
        for (Host peer : peers) {
            sendMessage(prepareMessage,AcceptProtocol.PROTO_ID,peer);
        }
        logger.info(self + " SENT "+request.getPaxosMessage().msgId);
    }


    public void reproposeTimer(ReproposeTimer timer, long timerId){
        if(this.currentValue == null){
            onProposeRequest(new ProposeRequest(currentValue),Client.ID);
        }else{
            timerId = -1;
            cancelTimer(timerId);
        }
    }

    private void setPromiseValue(PaxosMessage acceptedValue){
        if(acceptedValue != null){
            if(highestPromise == null){
                highestPromise = acceptedValue;
            } else if (highestPromise.proposalNum < acceptedValue.proposalNum){
                highestPromise = acceptedValue;
            } else if (highestPromise.proposalNum == acceptedValue.proposalNum && highestPromise.msgId.hashCode() < acceptedValue.msgId.hashCode()){
                highestPromise = acceptedValue;
            }
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = PromiseMessage.ID)
    public void onPromiseMessage(MessageInEvent event, PromiseMessage promiseMessage){
        if (promiseMessage.term != currentTerm){
            return;
        }
        setPromiseValue(promiseMessage.acceptedValue);
        promises++;
        if (promises >= HelperAux.getMajority(peers.size())){
            if (highestPromise == null){
                highestPromise = currentValue;
            }
            if (highestPromise == null){
                return;
            }

            proposal_num = promiseMessage.promisedNum;
            highestPromise.term = currentTerm;
            highestPromise.proposalNum = proposal_num;
            acceptValueCount = highestPromise;
            AcceptMessage acceptMessage = new AcceptMessage(proposal_num,currentTerm,highestPromise);

            for (Host peer : peers) {
                sendMessage(acceptMessage,AcceptProtocol.PROTO_ID,peer);
            }
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = AcceptMessage.ID)
    public void onAccepted(MessageInEvent event, AcceptMessage acceptMessage){
        if (currentTerm != acceptMessage.term || proposal_num !=acceptMessage.proposalNum){
            return;
        }
        acceptValueCount.proposalNum = acceptMessage.proposalNum;
        acks++;
        if (acks == HelperAux.getMajority(peers.size())){
            acks = 0;
            DecidedMessage decidedMessage = new DecidedMessage(proposal_num,currentTerm,acceptValueCount);
            for (Host peer : peers) {
                sendMessage(decidedMessage,LearnProto.ID,peer);
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


