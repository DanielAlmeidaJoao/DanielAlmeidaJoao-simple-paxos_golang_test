import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplePaxos.protocols.AcceptProtocol;
import org.simplePaxos.protocols.Client;
import org.simplePaxos.protocols.LearnProto;
import org.simplePaxos.protocols.ProposeProtocol;
import pt.unl.fct.di.novasys.babel.core.Babel;
import pt.unl.fct.di.novasys.network.ChannelLogicsWithNetty.NettyQuicChannel.utils.enums.NetworkRole;
import pt.unl.fct.di.novasys.network.babelChannels.babelNewChannels.tcpChannels.BabelTCP_P2P_Channel;
import pt.unl.fct.di.novasys.network.babelChannels.initializers.BabelTCPChannelInitializer;
import pt.unl.fct.di.novasys.network.data.Host;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

public class Main {

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String DEFAULT_CONF = "config.properties";

    public static void main(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        //babel.registerChannelInitializer(BabelQUIC_P2P_Channel.CHANNEL_NAME,new BabelQUICChannelInitializer(NetworkRole.P2P_CHANNEL));
        babel.registerChannelInitializer(BabelTCP_P2P_Channel.CHANNEL_NAME,new BabelTCPChannelInitializer(NetworkRole.P2P_CHANNEL));
        //babel.registerChannelInitializer(BabelUDPChannel.NAME,new BabelUDPInitializer());

        Properties props = Babel.loadConfig(args, DEFAULT_CONF);
        addInterfaceIp(props);
        String address = props.getProperty("address");
        String port = props.getProperty("port");

        //boolean isServer = props.getProperty("isServer")!=null;

        Host self = new Host(InetAddress.getByName(address), Short.parseShort(port));

        Client client = new Client("Client",Client.ID);
        AcceptProtocol acceptProtocol = new AcceptProtocol("accept",props);
        LearnProto learnProto = new LearnProto("learn",LearnProto.ID);
        ProposeProtocol proposeProtocol = new ProposeProtocol("proposer",ProposeProtocol.ID);

        babel.registerProtocol(client);
        babel.registerProtocol(acceptProtocol);
        babel.registerProtocol(learnProto);
        babel.registerProtocol(proposeProtocol);

        client.init(props);
        acceptProtocol.init(props);
        learnProto.init(props);
        proposeProtocol.init(props);

        String NETWORK_PROTO  = props.getProperty("NETWORK_PROTO");

        //PeerSampling sampling = new PeerSampling(props);
        //String NETWORK_PROTO  = props.getProperty("NETWORK_PROTO");

        logger.info("THESIS BABEL, I am {}. PROTO: {}", self,NETWORK_PROTO);

        /* StreamingServer streamingServer = new StreamingServer("StreamingServer",props);

        babel.registerProtocol(streamingServer);

        streamingServer.init(props);

         */

        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            logger.info("Goodbye");
            System.out.println("Hello world 33232!");
        }));

        babel.start();
    }

    public static void addInterfaceIp(Properties props) throws SocketException, java.security.InvalidParameterException {
        String interfaceName;
        if ((interfaceName = props.getProperty("interface")) != null) {
            String ip = getIpOfInterface(interfaceName);
            if (ip != null)
                props.setProperty("address", ip);
            else {
                throw new java.security.InvalidParameterException("Property interface is set to " + interfaceName + ", but has no ip");
            }
        }
    }
    public static String getIpOfInterface(String interfaceName) throws SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
        System.out.println(networkInterface);
        Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
        InetAddress currentAddress;
        while (inetAddress.hasMoreElements()) {
            currentAddress = inetAddress.nextElement();
            if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                return currentAddress.getHostAddress();
            }
        }
        return null;
    }
}