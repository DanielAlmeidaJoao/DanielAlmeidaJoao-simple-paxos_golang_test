package org.simplePaxos.protocols;

import appExamples2.appExamples.channels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.network.data.Host;
import tcpSupport.tcpChannelAPI.channel.NettyTCPChannel;
import tcpSupport.tcpChannelAPI.utils.NewChannelsFactoryUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class AcceptProtocol extends GenericProtocolExtension  {
    public static final short PROTO_ID = 400;

    int channel;
    public AcceptProtocol(String protoName, Properties properties) throws Exception {
        super(protoName, PROTO_ID);

        String [] contact = properties.getProperty("contact").split(":");
        String NETWORK_PROTO = properties.getProperty("NETWORK_PROTO");
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

    }
}
