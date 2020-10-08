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

        // Lors de la réception de (traverser)
        if(content.equals(SpanningTree.TRAVERSER)) {

            this.process.trace("Spanning TRAVERSER");

            if(this.spanningTree.getMarked() == true) {

                this.process.trace("DEJA VU " + this.process.getMyId());
                this.process.sendMessage(new Message(sourceId, SpanningTree.SPANNING_TAG, SpanningTree.ACK_DEJA_VU));
            }
            else {
                
                this.spanningTree.marked.set(true);
                this.spanningTree.father.set(sourceId);
                // succ = voisins - {j}
                this.spanningTree.successors.addAll(this.process.getNeighbourSet());
                this.spanningTree.successors.remove(sourceId);

                if(this.spanningTree.getSuccessors().isEmpty()) {
                    this.process.sendMessage(new Message(sourceId, SpanningTree.SPANNING_TAG, SpanningTree.ACK_TERMINE));
                }
                else {

                    this.spanningTree.nbAck.set(this.spanningTree.getSuccessorCount());
                    for(Integer id : this.spanningTree.getSuccessors()) {
                        this.process.sendMessage(new Message(id, SpanningTree.SPANNING_TAG, SpanningTree.TRAVERSER));
                    }
                }
            }
        }

        else if(content.equals(SpanningTree.ACK_DEJA_VU) || content.equals(SpanningTree.ACK_TERMINE)) {

            this.process.trace("Spanning ACK");
            
            if(content.equals(SpanningTree.ACK_DEJA_VU)) {
                this.process.trace("Spanning ACK_DEJA_VU");
                // succ = succ - {j}
                this.spanningTree.successors.remove(sourceId);
            }

            this.spanningTree.nbAck.decrementAndGet();
            if(this.spanningTree.nbAck.get() == 0) {

                this.spanningTree.doneMake.countDown();

                if(this.spanningTree.getFather() == -1) {
                    this.process.printOut("Parcours terminé");
                    //this.process.printOut(this.spanningTree.toString());
                } 
                else {
                    //if(this.spanningTree.getFather() != -1) this.process.printOut(this.spanningTree.toString());
                    this.process.sendMessage(new Message(this.spanningTree.getFather(), SpanningTree.SPANNING_TAG, SpanningTree.ACK_TERMINE));
                }
            }
        }
    }
}