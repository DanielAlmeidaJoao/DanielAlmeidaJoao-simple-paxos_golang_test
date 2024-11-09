package org.simplePaxos.protocols;

import appExamples2.appExamples.channels.babelNewChannels.quicChannels.BabelQUIC_P2P_Channel;
import appExamples2.appExamples.channels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fileStreaming.messages.FileBytesMessage;
import org.fileStreaming.messages.IHaveFile;
import org.fileStreaming.timers.BroadcastTimer;
import pt.unl.fct.di.novasys.babel.channels.events.*;
import pt.unl.fct.di.novasys.babel.core.GenericProtocolExtension;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.network.data.Host;
import quicSupport.utils.QUICLogics;
import tcpSupport.tcpChannelAPI.channel.NettyTCPChannel;
import tcpSupport.tcpChannelAPI.utils.BabelInputStream;
import tcpSupport.tcpChannelAPI.utils.NewChannelsFactoryUtils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StreamingServer extends GenericProtocolExtension {
    private static final Logger logger = LogManager.getLogger(StreamingServer.class);
    public static final short PROTO_ID = 400;
    public Properties properties;
    //public final int channelId;
    //public final Host broadcastAddress;
    public final int connectionProtoChannel;
    public final Host connectionProtoHost;
    public final long disseminationStart, disseminationPeriod;
    Map<Host,Pair<Long,Long>> timeElapsed;
    public final String isMessageSend;
    Path filePath;
    String protocol;

    public StreamingServer(String protoName, Properties properties) throws Exception{
        super(protoName, PROTO_ID);
        this.properties = properties;
        String address = properties.getProperty("address");
        String port = properties.getProperty("port");
        connectionProtoHost = new Host(InetAddress.getByName(address),Integer.parseInt(port));
        filePath = Paths.get(properties.getProperty("FILE_PATH"));

        disseminationPeriod = Long.parseLong(properties.getProperty("disseminationPeriod"));
        disseminationStart = Long.parseLong(properties.getProperty("disseminationStart"));

        isMessageSend = (String) properties.get("MESSAGE");
        logger.info("IS MESSAGE SENT: {} ",isMessageSend);
        Properties channelProps;
        String channelName;
        timeElapsed = new HashMap<>();
        /**
        //String broadCastAddress = properties.getProperty("BROADCAST_ADDRESS");
        //String broadcastPort = properties.getProperty("broadcast_port");
        //broadcastAddress = new Host(InetAddress.getByName(broadCastAddress),Integer.parseInt(broadcastPort));


        channelProps = TCPChannelUtils.udpChannelProperties(broadCastAddress,broadcastPort);
        channelProps.setProperty(FactoryMethods.SERVER_THREADS,properties.getProperty("SERVER_THREADS"));
        channelProps.setProperty(NettyUDPServer.UDP_BROADCAST_PROP,"ON");
        channelId = createChannel(BabelUDPChannel.NAME, channelProps);
         **/


         protocol = properties.getProperty("NETWORK_PROTO");

         logger.info("V3 using ping: {}", properties.getProperty("PING","500"));

        Pair<String,Properties> p = createConnectionChannel(protocol,address,port,properties);
        connectionProtoChannel = createChannel(p.getLeft(),p.getRight());
        registerMessageSerializer(connectionProtoChannel,IHaveFile.ID,IHaveFile.serializer);
        registerMessageSerializer(connectionProtoChannel, FileBytesMessage.ID,FileBytesMessage.serializer);

        //registerChannelEventHandler(connectionProtoChannel, OnChannelError.EVENT_ID, this::uponChannelError);
        registerChannelEventHandler(connectionProtoChannel, OnStreamConnectionUpEvent.EVENT_ID, this::uponStreamConnectionUp);
        registerChannelEventHandler(connectionProtoChannel, OnMessageConnectionUpEvent.EVENT_ID, this::uponMessageConnectionUp);

        registerChannelEventHandler(connectionProtoChannel, OnChannelError.EVENT_ID, this::uponChannelError);
        registerChannelEventHandler(connectionProtoChannel, OnConnectionDownEvent.EVENT_ID, this::uponConnectionDown);
        registerMessageHandler(connectionProtoChannel, IHaveFile.ID, this::uponIHaveFileMessage,null,null);

        logger.info("{} IS MESSAGE {} . PROTO {}. CLIENT",connectionProtoHost,isMessageSend,protocol);
        //registerTimerHandler(BroadcastTimer.TimerCode, this::uponBroadcastTime);
        System.out.println("V3 SERVER STARTED "+connectionProtoHost);
    }
    private void uponIHaveFileMessage(IHaveFile msg, Host from, short sourceProto, int channelId, String streamId) {
        Pair<Long,Long> p = timeElapsed.get(from);
        Pair<Long,Long> p2 = Pair.of(p.getLeft(),msg.fileLength-p.getLeft());
        timeElapsed.put(from,p2);
        logger.info("{} {} HAS THE FILE!!! ELAPSED: {}",connectionProtoHost,from,msg.fileLength);
        System.out.println("RECEIVED FROM "+from+" --- "+msg.fileLength+" +++ SENT "+p.getLeft());
        clients--;
        if(clients==0){
            List<Long> elapsed = new LinkedList<>();
            long sumTotal = 0;
            for (Pair<Long, Long> value : timeElapsed.values()) {
                elapsed.add(value.getRight());
                sumTotal += value.getRight();
            }
            float s = timeElapsed.size();
            float d = sumTotal;
            float average = d/s;
            String h = Arrays.toString(elapsed.toArray());
            logger.info(h);
            System.out.println(h);
            String type = "";
            if(isMessageSend.equalsIgnoreCase("ME")){
                type = "MessageConnection";
            } else if (isMessageSend.equalsIgnoreCase("ST_M")) {
                type = "StreamWithMessages";
            } else if (isMessageSend.equalsIgnoreCase("ST")) {
                type = "StreamWithZeroCopy";
            }else{
                System.out.println("UNKNOWN TYPE of isMessageSend!!!");
                System.exit(0);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/results1NODEV2.txt", true))) {
                writer.append(protocol+" "+timeElapsed.size()+" " +type+" "+average);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("FINISHED");
            System.exit(0);
        }
    }
    private void uponConnectionDown(OnConnectionDownEvent event, int channelId) {
        logger.info("CONNECTION DOWN: {} {} {}",event.connectionId,event.getNode(),event.type);
    }
    int clients = 0;
    private void uponStreamConnectionUp(OnStreamConnectionUpEvent event, int channelId) {
        logger.info("{} Stream CONNECTION UP. SENDING THE FILE TO {}",connectionProtoHost,event.getNode());
        System.out.println("Stream CONNECTION UP. SENDING THE FILE TO"+event.getNode());
        try{
            if(isMessageSend.equals("ST")){
                long start = System.currentTimeMillis();
                timeElapsed.put(event.getNode(),Pair.of(start,0L));
                event.babelInputStream.writeFile(filePath.toFile());
                clients++;
            }else{
                startStreaming(event.getNode(),null,event.babelInputStream);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("EXITTED");
            System.exit(0);
        }
    }
    private void uponMessageConnectionUp(OnMessageConnectionUpEvent event, int channelId) {
        System.out.println("MESSAGE CONNECTION UP. SENDING THE FILE TO"+event.getNode());
        countBroadcast++;
        String name="name_"+countBroadcast;
        System.out.println("TIMER TRIGGERED "+name);
        IHaveFile iHaveFile = new IHaveFile(filePath.toFile().length(),name,connectionProtoHost);
        sendMessage(iHaveFile,event.conId);
        System.out.println("BROADCAST SENT");
        logger.info("{} MESSAGE SENT TO {}",connectionProtoHost,event.getNode());
        if(isMessageSend.equals("ME")){
            //startStreaming(event.getNode(),event.conId);
            BroadcastTimer b = new BroadcastTimer();
            b.host = event.getNode();
            b.conId = event.conId;
            setupTimer(b, disseminationStart);
        }
    }
    private void startStreaming(Host host, String conId, BabelInputStream inputStream) {
        logger.info("WITH THREAD {} MESSAGE CONNECTION UP. SENDING THE FILE TO {}",connectionProtoHost,host);
        long start = System.currentTimeMillis();
        timeElapsed.put(host,Pair.of(start,0L));
        clients++;
        if(inputStream!=null){
            inputStream.setFlushMode(true);
        }
        new Thread(() -> {
            try {
                int size = 1024*64;
                FileInputStream ff = new FileInputStream(filePath.toFile());
                byte [] read = new byte[size];
                int ef = 0;
                while ( (ef = ff.read(read,0,size))>0){
                    //sendMessage(new BabelStreamDeliveryEvent(read,ef),host);
                    if(inputStream==null){
                        sendMessage(new FileBytesMessage(read,ef),conId);
                    }else{
                        inputStream.writeBytes(read,0,ef);
                    }
                    read = new byte[size];
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("LEAVING HEREERE");
                System.exit(0);
            }
        }).start();
    }

    private void uponMsgFail2(OnStreamDataSentEvent msg, Host host, short destProto,
                              Throwable throwable, int channelId) {
        logger.error("Message {} to {} failed, reason: {}", msg, host, throwable);
    }

    public static Pair<String,Properties> createConnectionChannel(String proto, String address, String port, Properties properties) throws IOException {
        Properties channelProps;
        int channel;
        Pair<String,Properties> result;
        if(proto.equalsIgnoreCase("quic")){
            //System.out.println("QUIC ON");
            //channelProps.setProperty("metrics_interval","2000");
            channelProps = NewChannelsFactoryUtils.quicChannelProperty(address,port);

            String maxAck = properties.getProperty("MAX_ACK","2000");
            channelProps.setProperty(QUICLogics.MAX_ACK_DELAY,maxAck);
            String congAlgo = properties.getProperty("CONG_ALGO","CUBIC");
            channelProps.setProperty(QUICLogics.CongestionControlAlgorithm,congAlgo);

            System.out.println("QUIC MAXACK AND CONGALGO "+maxAck+" -- "+congAlgo);

            channelProps.setProperty(QUICLogics.MAX_IDLE_TIMEOUT_IN_SECONDS,"3000");
            String max = properties.getProperty("MAX_DATA",""+(1024*1024));
            channelProps.setProperty(QUICLogics.INITIAL_MAX_DATA,max);
            channelProps.setProperty(QUICLogics.INITIAL_MAX_STREAM_DATA_BIDIRECTIONAL_REMOTE,max);
            channelProps.setProperty(QUICLogics.INITIAL_MAX_STREAM_DATA_BIDIRECTIONAL_LOCAL,max);

            //int p = (2048);
            //channelProps.setProperty(QUICLogics.MAX_UDP_RCV_SND_PAYLOD_SIZE,""+p);
            //channelProps.setProperty(QUICLogics.idleTimeoutPercentageHB,"15");
            //channelProps.setProperty(QUICLogics.MAX_ACK_DELAY,"150");
            //channelProps.setProperty(QUICLogics.MAX_ACK_DELAY,"200");

            //addExtraProps(singleThreaded,zeroCopy,channelProps);
            //channel = createChannel(BabelQUIC_P2P_Channel.CHANNEL_NAME, channelProps);
            result = Pair.of(BabelQUIC_P2P_Channel.CHANNEL_NAME,channelProps);
        }else{

            //System.out.println("TCP ON");
            channelProps = NewChannelsFactoryUtils.tcpChannelProperties(address,port);
            if(properties.getProperty("N_Z_COPY")!=null){
                channelProps.setProperty(NettyTCPChannel.NOT_ZERO_COPY,"ON");
            }
            //channel = createChannel(BabelTCP_P2P_Channel.CHANNEL_NAME, channelProps);
            result = Pair.of(BabelTCP_P2P_Channel.CHANNEL_NAME,channelProps);
        }
        return result;
    }
    private void uponChannelError(OnChannelError event, int channelId) {
        logger.info("{} ERROR ----- {}",connectionProtoHost,event);
    }
    int countBroadcast = 0;
    private void uponBroadcastTime(BroadcastTimer timer, long timerId) {
        startStreaming(timer.host,timer.conId,null);
    }

    @Override
    public void init(Properties properties) throws HandlerRegistrationException, IOException {
        registerTimerHandler(BroadcastTimer.TimerCode, this::uponBroadcastTime);

        //long id = setupPeriodicTimer(new BroadcastTimer(), disseminationStart, disseminationPeriod);
        //System.out.println("SET TIMER: "+disseminationStart + " -- "+disseminationPeriod+" ID: "+id);

    }
}
