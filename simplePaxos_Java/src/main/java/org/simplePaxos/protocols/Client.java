package org.simplePaxos.protocols;

import org.simplePaxos.HelperAux;
import org.simplePaxos.internalCommunicationMessages.ChannelCreatedRequest;
import org.simplePaxos.internalCommunicationMessages.LearnRequest;
import org.simplePaxos.internalCommunicationMessages.ProposeRequest;
import org.simplePaxos.messages.PaxosMessage;
import org.simplePaxos.timers.HashResultPrinterTimer;
import org.simplePaxos.timers.ProposeTimer;
import pt.unl.fct.di.novasys.babel.annotations.ChannelEventHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.annotations.RequestHandlerAnnotation;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.network.ChannelLogicsWithNetty.NettyTCPChannel.utils.NewChannelsFactoryUtils;
import pt.unl.fct.di.novasys.network.babelChannels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static javax.xml.crypto.dsig.DigestMethod.SHA256;

public class Client extends GenericProtocolExtension {
    public static final short ID = 763;
    public static final Logger log = Logger.getLogger(Client.class.getName());

    List<PaxosMessage> ops;
    int count;
    int currentTerm;
    PaxosMessage lastProposed;
    Host self;
    long start;
    private Set<Host> peers;
    int channel;

    public Client(String protoName, short protoId) {
        super(protoName, protoId);
        peers = new HashSet<>();
        currentTerm = 1;
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


        registerTimerHandler(HashResultPrinterTimer.ID,this::printResultsPeriodically);
        registerTimerHandler(ProposeTimer.ID,this::timeHandler);

        String [] contacts = properties.getProperty("contacts").split(",");
        for (String contact : contacts) {
            String [] splittedAddress = contact.split(":");
            Host h = new Host(InetAddress.getByName(splittedAddress[0]),Integer.parseInt(splittedAddress[1]));
            //peers.add(h);
            openMessageConnection(h);

        }

        setupTimer(new ProposeTimer(),15*1000);

    }

    public PaxosMessage nextMessage(){
        if (start == 0){
            start = System.currentTimeMillis();
        }
        count++;
        if (count > 2){
            log.info(self+" -- ELAPSED IS -- : "+(System.currentTimeMillis()-start));
            return null;
        }
        String msgValue = self+"_"+count;
        return new PaxosMessage(msgValue,msgValue,0,currentTerm,0);

    }



    public void appendMap(){
        int size = ops.size();
        log.info(String.format("< %s %d >",self.toString(),size));
        StringBuilder stringBuffer = new StringBuilder();
        for (PaxosMessage op : ops) {
            stringBuffer.append(op.msgValue);
        }
        log.info(self+"__"+HelperAux.digest(stringBuffer.toString()));
    }

    void printResultsPeriodically(HashResultPrinterTimer timer, long timerId){
        appendMap();
    }
    public void timeHandler(ProposeTimer proposeTimer, long timer){
        sendRequest(new ProposeRequest(nextMessage()),ProposeProtocol.ID);
    }

    @RequestHandlerAnnotation(REQUEST_ID = ID)
    public void onRequest(LearnRequest request, short from){
        PaxosMessage value = request.decidedMessage.paxosMessage;
        if(currentTerm == request.decidedMessage.term){
            currentTerm++;
        }
        ops.add(request.decidedMessage.paxosMessage);
        if (lastProposed != null && lastProposed.msgId.equals(request.decidedMessage.paxosMessage.msgId)){
            lastProposed = nextMessage();
        }

        if (lastProposed == null){
            sendRequest(new ProposeRequest(new PaxosMessage(null,null,value.proposalNum,value.term,0)),ProposeProtocol.ID);
        } else {
            lastProposed.term = currentTerm;
            lastProposed.proposalNum = request.decidedMessage.proposalNum;
            sendRequest(new ProposeRequest(lastProposed),ProposeProtocol.ID);
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
