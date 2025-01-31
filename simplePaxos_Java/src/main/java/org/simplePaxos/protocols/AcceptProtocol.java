package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.helperFiles.TermArguments;
import org.simplePaxos.messages.*;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.MessageInHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.internal.MessageInEvent;
import pt.unl.fct.di.novasys.network.ChannelLogicsWithNetty.NettyTCPChannel.utils.NewChannelsFactoryUtils;
import pt.unl.fct.di.novasys.network.babelChannels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;


public class AcceptProtocol extends GenericProtocolExtension  {

    private static final Logger logger = LogManager.getLogger(AcceptProtocol.class);
    public static final short PROTO_ID = 400;
    private Set<Host> peers;
    int totalSent;
    String proposer;
    Map<Integer,TermArguments> terms;
    Host self;

    int channel;
    public AcceptProtocol(String protoName, Properties properties) throws Exception {
        super(protoName, PROTO_ID);
        peers = new HashSet<>();
        totalSent = 0;

        //TODO after starting, connect to all clients
        String [] contacts = properties.getProperty("contacts").split(";");
        for (String contact : contacts) {
            String [] splittedAddress = contact.split(":");
            Host h = new Host(InetAddress.getByName(splittedAddress[0]),Integer.parseInt(splittedAddress[1]));
            peers.add(h);
        }

        String address = properties.getProperty("address");
        String port = properties.getProperty("port");
        self = new Host(InetAddress.getByName(address),Integer.parseInt(port));

        Properties channelProps = NewChannelsFactoryUtils.tcpChannelProperties(address,port);
        channel = createChannel(BabelTCP_P2P_Channel.CHANNEL_NAME,channelProps);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        logger.info("Accept Protocol Started for: ",self);
    }

    private void uponIHaveFileMessage(PaxosMessage msg, Host from, short sourceProto, int channelId, String connectionId) {
    }

    private TermArguments computeTerm(int term){
        return terms.computeIfAbsent(term, key -> {
            TermArguments termArguments = new TermArguments();
            termArguments.setTerm(term);
            return termArguments;
        });
    }
    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = PrepareMessage.ID)
    private void uponPrepareMessage(MessageInEvent event, PrepareMessage prepareMessage) {
        TermArguments term = computeTerm(prepareMessage.term);

        if (term.promised_num < prepareMessage.proposalNum){
            term.promised_num = prepareMessage.proposalNum;
            term.remoteHost = event.getFrom();
            PromiseMessage promiseMessage = new PromiseMessage(term.accepted_num,prepareMessage.proposalNum,term.term,term.acceptedValue);
            sendMessage(promiseMessage,event.connectionId);
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = AcceptMessage.ID)
    private void uponAcceptMessage(MessageInEvent event, AcceptMessage acceptMessage) {
        TermArguments term = computeTerm(acceptMessage.term);
        if ( term.promised_num < acceptMessage.proposalNum || (term.promised_num == acceptMessage.proposalNum && event.getFrom().equals(term.remoteHost) )){
            term.promised_num = acceptMessage.proposalNum;
            term.accepted_num = acceptMessage.proposalNum;
            term.acceptedValue = acceptMessage.paxosMessage;
            term.remoteHost = event.getFrom();
            sendMessage(acceptMessage,event.connectionId);
            totalSent++;
        }
    }

    @MessageInHandlerAnnotation(PROTO_MESSAGE_ID = DecidedMessage.ID)
    private void uponDecidedValue(MessageInEvent event,DecidedMessage acceptMessage) {
        //@TODO
        logger.info("@TODO");
        /**
        TermArguments term = computeTerm(acceptMessage.term);
        if ( term.promised_num < acceptMessage.proposalNum || (term.promised_num == acceptMessage.proposalNum && from.equals(term.remoteHost) )){
            term.promised_num = acceptMessage.proposalNum;
            term.accepted_num = acceptMessage.proposalNum;
            term.acceptedValue = acceptMessage.paxosMessage;
            term.remoteHost = from;
            sendMessage(acceptMessage,connectionId);
            totalSent++;
        }**/
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
