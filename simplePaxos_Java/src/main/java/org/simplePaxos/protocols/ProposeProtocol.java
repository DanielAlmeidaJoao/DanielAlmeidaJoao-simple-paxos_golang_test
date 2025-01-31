package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.HelperAux;
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
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class ProposeProtocol extends GenericProtocolExtension {

    private static final Logger logger = LogManager.getLogger(ProposeProtocol.class);

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
    private long timerId;


    public ProposeProtocol(String protoName, short protoId) {
        super(protoName, protoId);
        currentTerm = 1;
        promises = 0;

    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        String port = properties.getProperty("NETWORK_PORT");
        String address = properties.getProperty("NETWORK_ADDRESS");
        self = new Host(InetAddress.getByName(address),Integer.parseInt(port));
        registerTimerHandler(ReproposeTimer.ID, this::reproposeTimer);

    }

    @RequestHandlerAnnotation(REQUEST_ID = ProposeRequest.ID)
    private void onProposeRequest(ProposeRequest request, short from){
        PaxosMessage toPropose = request.getPaxosMessage();
        currentTerm = request.getPaxosMessage().term;
        if(toPropose.msgId == null){
            cancelTimer(timerId);
            timerId = -1;
            return;
        }

        if (timerId <= 0){
            timerId = setupPeriodicTimer(new ReproposeTimer(this.currentValue),1000*5,1000*6);
            proposal_num = toPropose.proposalNum + (1+random.nextInt(100));
            toPropose.proposalNum = proposal_num;
            highestPromise = acceptValueCount = null;
            PrepareMessage prepareMessage = new PrepareMessage(proposal_num,toPropose.term);
            promises = 0;
            acks = 0;
            for (Host peer : peers) {
                sendMessage(prepareMessage,peer);
            }
        }
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
        if (promises == HelperAux.getMajority(peers.size())){
            if (highestPromise == null){
                highestPromise = currentValue;
            }
            if (highestPromise == null){
                return;
            }

            highestPromise.term = currentTerm;
            acceptValueCount = highestPromise;
            AcceptMessage acceptMessage = new AcceptMessage(proposal_num,currentTerm,highestPromise);
            for (Host peer : peers) {
                sendMessage(acceptMessage,peer);
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
                sendMessage(decidedMessage,peer);
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

}


