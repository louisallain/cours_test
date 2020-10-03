package spanning_tree;

import csp_V1.*;
import java.util.Set;
import java.util.HashSet;

class SpanningMessageHandler extends Object implements MessageHandler {

    ConcurrentProcess process;
    SpanningTree spanningTree;

    public SpanningMessageHandler(ConcurrentProcess process, SpanningTree spanningTree) {
        this.process = process;
        this.spanningTree = spanningTree;
    }

    @Override
    public void onMessage(Message msg) {

        this.process.trace("Spanning msg receive");

        int sourceId = msg.getSourceId();
        String content = msg.getContent();

        if(content.equals(SpanningTree.ACK_DEJA_VU) || content.equals(SpanningTree.ACK_TERMINE)) {

            this.process.trace("Spanning ACK");
            if(content.equals(SpanningTree.ACK_DEJA_VU)) {
                this.process.trace("Spanning ACK_DEJA_VU");
                // succ = succ - {j}
                Set<Integer> tmp = new HashSet<>(this.spanningTree.getSuccessors());
                tmp.remove(sourceId);
                this.spanningTree.setSuccessors(tmp);
            }

            this.spanningTree.setNbAck(this.spanningTree.getNbAck() - 1);
            if(this.spanningTree.getNbAck() == 0) {

                this.process.trace("Root = " + this.spanningTree.getRoot());
                if(msg.getDestinationId() == this.spanningTree.getRoot()) {
                    this.process.trace(this.spanningTree.toString());
                    this.spanningTree.makeDoneCountDown.countDown();
                } 
                else {
                    this.process.sendMessage(new Message(this.spanningTree.getFather(), SpanningTree.SPANNING_TAG, SpanningTree.ACK_TERMINE));
                }
            }
        }
        // Lors de la réception de (traverser)
        else if(content.equals(SpanningTree.TRAVERSER)) {
            this.process.trace("Spanning TRAVERSER");
            if(this.spanningTree.isMarked()) {
                this.process.trace("DEJA VU " + this.process.getMyId());
                this.process.sendMessage(new Message(sourceId, SpanningTree.SPANNING_TAG, SpanningTree.ACK_DEJA_VU));
            }
            else {
                
                
                this.spanningTree.setMarked(true);
                this.spanningTree.setFather(sourceId);
                // succ = voisins - {j}
                Set<Integer> tmp = new HashSet<>(this.process.getNeighbourSet());
                tmp.remove(sourceId);
                this.spanningTree.setSuccessors(tmp);

                if(this.spanningTree.getSuccessors().isEmpty()) {
                    this.process.sendMessage(new Message(sourceId, SpanningTree.SPANNING_TAG, SpanningTree.ACK_TERMINE));
                }
                else {

                    this.spanningTree.setNbAck(this.spanningTree.getSuccessorCount());
                    for(Integer id : this.spanningTree.getSuccessors()) {
                        this.process.sendMessage(new Message(id, SpanningTree.SPANNING_TAG, SpanningTree.TRAVERSER));
                    }
                }
            }
        }
    }
}