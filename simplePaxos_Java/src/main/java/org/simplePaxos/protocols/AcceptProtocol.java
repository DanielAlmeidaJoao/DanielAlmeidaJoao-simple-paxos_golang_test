package org.simplePaxos.protocols;

import appExamples2.appExamples.channels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.messages.PaxosMessage;
import org.simplePaxos.messages.PrepareMessage;
import pt.unl.fct.di.novasys.babel.channels.events.OnConnectionDownEvent;
import pt.unl.fct.di.novasys.babel.channels.events.OnMessageConnectionUpEvent;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.network.data.Host;
import tcpSupport.tcpChannelAPI.channel.NettyTCPChannel;
import tcpSupport.tcpChannelAPI.utils.NewChannelsFactoryUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


public class AcceptProtocol extends GenericProtocolExtension  {
    private static final Logger logger = LogManager.getLogger(AcceptProtocol.class);
    public static final short PROTO_ID = 400;


    int channel;
    public AcceptProtocol(String protoName, Properties properties) throws Exception {
        super(protoName, PROTO_ID);

        String [] contact = properties.getProperty("contact").split(":");
        String address = properties.getProperty("address");
        String port = properties.getProperty("port");

        Properties channelProps = NewChannelsFactoryUtils.tcpChannelProperties(address,port);
        if(properties.getProperty("N_Z_COPY")!=null){
            channelProps.setProperty(NettyTCPChannel.NOT_ZERO_COPY,"ON");
        }
        channel = createChannel(BabelTCP_P2P_Channel.CHANNEL_NAME,channelProps);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {

        registerMessageSerializer(channel, PaxosMessage.ID, PaxosMessage.serializer);
        registerMessageSerializer(channel, PaxosMessage.ID, PaxosMessage.serializer);

        registerChannelEventHandler(channel, OnMessageConnectionUpEvent.EVENT_ID, this::uponMessageConnectionUp);
        registerChannelEventHandler(channel, OnConnectionDownEvent.EVENT_ID, this::uponConnectionDown);

        registerMessageHandler(channel, PaxosMessage.ID, this::uponIHaveFileMessage,null,null);

    }

    private void uponIHaveFileMessage(PaxosMessage msg, Host from, short sourceProto, int channelId, String connectionId) {

    }

    private void uponPrepareMessage(PrepareMessage prepareMessage, Host from, short sourceProto, int channelId, String connectionId) {

    }

    private void uponConnectionDown(OnConnectionDownEvent event, int channelId) {
        logger.info("CONNECTION DOWN: {} {} {}",event.connectionId,event.getNode(),event.type);
    }

    private void uponMessageConnectionUp(OnMessageConnectionUpEvent event, int channelId) {
        logger.info("{} MESSAGE CONNECTION TO {} IS UP.",channelId,event.getNode());
    }
}
