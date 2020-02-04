package casa.util.dodwanclient;

import casa.util.Processor;
import casa.util.pdu.Pdu;
import java.util.HashSet;
import java.util.Set;

/**
* Service used to observe the peers in the direct neighborhood.
*/
public class PeerService extends Listenable<PeerListener>
{
    /**
    * Counter to generate unique tokens for requests
    */
    private static int TKN_CPT = 0;

    /**
    * Singleton instance of this class
    */
    private static PeerService instance;

    /**
    * Timeout for replies
    */
    private final int replyTimeout;

    /**
    * Channel used to send/receive requests/responses and notifications
    */
    private final PduChannel channel;

    /**
    * Give a singleton instance of this class
    *
    * @return singleton {@link PeerService}
    */
    public static PeerService getInstance()
    {
        if (instance == null) {
            Configuration conf = Configuration.getInstance();
            instance = new PeerService(conf.getReplyTimeout());
        }
        return instance;
    }

    /**
    * Private constructor (singleton pattern). Register this instance to receive
    * add_peer, remove_peer and clear_peers notifications from the server
    *
    * @param replyTimeout the timeout for replies
    */
    private PeerService(int replyTimeout) {
        this.replyTimeout = replyTimeout;
        this.channel = Session.getInstance().getChannel();
        this.channel.putProcessor("add_peer", new Processor<Pdu>() {
            @Override
            public void process(Pdu pdu) { onPeerAdded(pdu); }
        });
        this.channel.putProcessor("remove_peer", new Processor<Pdu>() {
            @Override
            public void process(Pdu pdu) { onPeerRemoved(pdu); }
        });
        this.channel.putProcessor("clear_peers", new Processor<Pdu>() {
            @Override
            public void process(Pdu pdu) { onPeersCleared(); }
        });
    }

    /**
    * Give the name of the current peer (host name)
    *
    * @return the current peer name (may be null)
    */
    public String getMyPeerName() throws Exception
    {
        Pdu pdu = this.channel.writeForReply(makePdu("get_my_peer"), replyTimeout, "recv_pids");
        return pdu.getString("pid");
    }

    /**
    * Give the names of the peers that are currently in the direct neighborhood
    * of the current peer
    *
    * @return names of the peers in the direct neighborhood
    */
    public Set<String> getPeers() throws Exception
    {
        Pdu pdu = this.channel.writeForReply(makePdu("get_peers"), replyTimeout, "recv_pids");
        Set<String> pidSet = new HashSet<>();
        String[] pids = pdu.getStringArray("pids");
        if (pids != null) {
            for (String pid : pids) {
                pidSet.add(pid);
            }
        }
        return pidSet;
    }

    /**
    * Process the add_peer server notifications
    *
    * @param pdu the PDU received from the server
    */
    private void onPeerAdded(Pdu pdu)
    {
        String pid = pdu.getString("pid");
        for (PeerListener listener : getListeners()) {
            listener.onPeerAdded(pid);
        }
    }

    /**
    * Process the remove_peer server notifications
    *
    * @param pdu the PDU received from the server
    */
    private void onPeerRemoved(Pdu pdu)
    {
        String pid = pdu.getString("pid");
        for (PeerListener listener : getListeners()) {
            listener.onPeerRemoved(pid);
        }
    }

    /**
    * Process the clear_peers server notifications
    */
    private void onPeersCleared()
    {
        for (PeerListener listener : getListeners()) {
            listener.onPeersCleared();
        }
    }

    /**
    * Create a PDU having a token and the given name
    *
    * @param the name value to be set in the PDU
    * @return the new PDU
    */
    private Pdu makePdu(String name)
    {
        Pdu pdu = this.channel.makePdu();
        pdu.putString("tkn", "PEER-" + TKN_CPT++);
        pdu.putString("name", name);
        return pdu;
    }
}
