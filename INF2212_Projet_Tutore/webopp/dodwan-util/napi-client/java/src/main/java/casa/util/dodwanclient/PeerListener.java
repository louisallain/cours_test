package casa.util.dodwanclient;

/**
* A listener that observes the peers in the direct neighborhood
*/
public interface PeerListener
{
    /**
    * Method called when a peer is added in the direct neighborhood
    *
    * @param peerName the name of the peer
    */
    void onPeerAdded(String peerName);

    /**
    * Method called when a peer is removed from the direct neighborhood
    *
    * @param peerName the name of the peer
    */
    void onPeerRemoved(String peerName);

    /**
    * Method called when all peers are removed from the direct neighborhood 
    */
    void onPeersCleared();
}
