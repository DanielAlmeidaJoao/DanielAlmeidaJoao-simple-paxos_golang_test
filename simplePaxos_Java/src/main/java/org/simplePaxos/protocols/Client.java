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
import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;
import pt.unl.fct.di.novasys.network.ChannelLogicsWithNetty.NettyTCPChannel.utils.NewChannelsFactoryUtils;
import pt.unl.fct.di.novasys.network.babelChannels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static javax.xml.crypto.dsig.DigestMethod.SHA256;

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

    public Client(String protoName, short protoId) {
        super(protoName, protoId);
        peers = new HashSet<>();
        currentTerm = 1;
        ops = new LinkedList<>();
        count = 0;
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


        //registerTimerHandler(HashResultPrinterTimer.ID,this::printResultsPeriodically);
        //registerTimerHandler(ProposeTimer.ID,this::timeHandler);
        //registerTimerHandler(FinishTimer.ID,this::onFinishTimer);

        String [] contacts = properties.getProperty("contacts").split(",");
        for (String contact : contacts) {
            String [] splittedAddress = contact.split(":");
            Host h = new Host(InetAddress.getByName(splittedAddress[0]),Integer.parseInt(splittedAddress[1]));
            //peers.add(h);
            openMessageConnection(h);

        }

        setupTimer(ProposeTimer.ID,10*1000);
        //setupPeriodicTimer(ProposeTimer.ID,5*1000,250);
        setupPeriodicTimer(HashResultPrinterTimer.ID,5*1000,5*1000);
    }

    public PaxosMessage nextMessage(){
        if (start == 0){
            start = System.currentTimeMillis();
        }
        if (count > 1000){
            setupTimer(FinishTimer.ID,30*1000);
            return null;
        } else {
            count++;
        }
        if(lastProposed != null){
            return lastProposed;
        }
        String msgValue = self+"_"+count;
        return new PaxosMessage(msgValue,msgValue);

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
        if(lastProposed == null){
            lastProposed = nextMessage();
        }
        if(lastProposed == null){
            super.cancelTimer(timer);
        } else {
            super.cancelTimer(timer);
            sendRequest(new ProposeRequest(lastProposed,0,currentTerm),ProposeProtocol.ID);
            timerId = setupTimer(ProposeTimer.ID,200);
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
        } else {
            return;
        }
        cancelTimer(timerId);
        ops.add(request.decidedMessage.paxosMessage);
        if (lastProposed!=null){
            if(lastProposed.msgId.equals(value.msgId)){
                log.info(self+". TERM "+currentTerm+ ". DECISION TAKEN "+request.decidedMessage.paxosMessage.msgId);
                lastProposed = null;
                lastProposed = nextMessage();
            }
            if(lastProposed != null){
                sendRequest(new ProposeRequest(lastProposed,request.decidedMessage.proposalNum,currentTerm),ProposeProtocol.ID);
                timerId = setupTimer(ProposeTimer.ID,200);
            } else {
                log.info(self+" -- ELAPSED IS -- : "+(System.currentTimeMillis()-start));
            }
        }
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
