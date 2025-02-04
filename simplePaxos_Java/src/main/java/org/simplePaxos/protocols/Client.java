package org.simplePaxos.protocols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.ChannelCreatedRequest;
import org.simplePaxos.internalCommunicationMessages.LearnRequest;
import org.simplePaxos.internalCommunicationMessages.ProposeRequest;
import org.simplePaxos.messages.PaxosMessage;
import org.simplePaxos.timers.FinishTimer;
import org.simplePaxos.timers.HashResultPrinterTimer;
import org.simplePaxos.timers.ProposeTimer;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.TimerEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.network.ChannelLogicsWithNetty.NettyTCPChannel.utils.NewChannelsFactoryUtils;
import pt.unl.fct.di.novasys.network.babelChannels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class Client extends GenericProtocolExtension {
    public static final short ID = 763;
    private static final Logger log = LogManager.getLogger(Client.class);

    List<PaxosMessage> ops;
    int count;
    int currentTerm;
    PaxosMessage lastProposed;
    Host self;
    long start;
    private Set<Host> peers;
    int channel;
    long periodicProposeTimer;
    long timerId = -1;
    int proposalNumber;
    PaxosMessage lastSentMessage;

    public Client(String protoName, short protoId) {
        super(protoName, protoId);
        peers = new HashSet<>();
        currentTerm = 1;
        ops = new LinkedList<>();
        count = 0;
        proposalNumber = 1;
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        //TODO after starting, connect to all clients


        String address = properties.getProperty("address");
        String port = properties.getProperty("port");
        self = new Host(InetAddress.getByName(address),Integer.parseInt(port));

        Properties channelProps = NewChannelsFactoryUtils.tcpChannelProperties(address,port);
        channel = createChannel(BabelTCP_P2P_Channel.CHANNEL_NAME,channelProps);

        ChannelCreatedRequest request = new ChannelCreatedRequest(channel,self);

        sendRequest(request,AcceptProtocol.PROTO_ID);
        sendRequest(request,LearnProto.ID);
        sendRequest(request,ProposeProtocol.ID);

        String [] contacts = properties.getProperty("contacts").split(",");
        for (String contact : contacts) {
            String [] splittedAddress = contact.split(":");
            Host h = new Host(InetAddress.getByName(splittedAddress[0]),Integer.parseInt(splittedAddress[1]));
            //peers.add(h);
            openMessageConnection(h);

        }

        setupTimer(ProposeTimer.ID,5*1000);
        //timerId = setupPeriodicTimer(ProposeTimer.ID,5*1000,100);
        setupPeriodicTimer(HashResultPrinterTimer.ID,5*1000,5*1000);
    }

    Set<String> sent = new HashSet<>();
    public PaxosMessage nextMessage(){
        if (start == 0){
            start = System.currentTimeMillis();
        }
        if (count > 1000 || lastProposed != null){
            return lastProposed;
        }

        count++;
        String msgValue = self+"_"+count;
        lastProposed = new PaxosMessage(msgValue,msgValue);
        sent.add(lastProposed.msgId);
        lastSentMessage = lastProposed;
        return lastProposed;

    }

    public void appendMap(){
        int size = ops.size();
        StringBuilder stringBuffer = new StringBuilder();
        for (PaxosMessage op : ops) {
            stringBuffer.append(op.msgValue);
        }
        log.info(count+": "+self+"__"+size+"__"+HelperAux.digest(stringBuffer.toString()));
    }

    @TimerEventHandlerAnnotation(TIMER_ID = HashResultPrinterTimer.ID)
    void printResultsPeriodically(long timerId){
        appendMap();
    }

    @TimerEventHandlerAnnotation(TIMER_ID = ProposeTimer.ID)
    public void timeHandler(long timer){
        nextMessage();
        if(lastProposed == null){
            cancelTimer(timerId);
        } else {
            sendRequest(new ProposeRequest(lastProposed,proposalNumber,currentTerm),ProposeProtocol.ID);
        }
    }

    @TimerEventHandlerAnnotation(TIMER_ID = FinishTimer.ID)
    public void onFinishTimer(long timer){
        log.info("Exiting... .. .");
        System.exit(0);
    }

    @RequestHandlerAnnotation(REQUEST_ID = LearnRequest.REQUEST_ID)
    public void onRequest(LearnRequest request, short from){
        PaxosMessage value = request.decidedMessage.paxosMessage;
        if(currentTerm == request.decidedMessage.term){
            currentTerm++;
            if(currentTerm == 3003){
                setupTimer(FinishTimer.ID,30*1000);
            }
            proposalNumber = request.decidedMessage.proposalNum;
            ops.add(request.decidedMessage.paxosMessage);

            if (lastProposed!=null && lastProposed.msgId.equals(value.msgId)){
                if(count > 1000 && lastSentMessage.msgId.equals(lastProposed.msgId) ){
                    log.info(count+"__"+self+" -- ELAPSED IS -- : "+(System.currentTimeMillis()-start)+" TERM: "+currentTerm+" __ "+sent.size());
                }
                sent.remove(lastProposed.msgId);
                lastProposed = null;
            }
        } else {
            log.info(self+"_ currentTerm "+currentTerm+". RECEIVED: "+currentTerm);
            System.exit(0);
        }

        timeHandler(timerId);
    }

    @ChannelEventHandlerAnnotation(EVENT_ID = OnMessageConnectionUpEvent.EVENT_ID)
    public void onConnectionUp(OnMessageConnectionUpEvent event, int channelId){
        peers.add(event.getNode());
        log.info( self + ". CONNECTION IS UP: "+event.getNode());
    }
    @ChannelEventHandlerAnnotation(EVENT_ID = OnConnectionDownEvent.EVENT_ID)
    public void onConnectionDown(OnConnectionDownEvent event, int channelId){
        peers.remove(event.getNode());
        log.info( self + ". CONNECTION IS DOWN: "+event.getNode());
    }
}
