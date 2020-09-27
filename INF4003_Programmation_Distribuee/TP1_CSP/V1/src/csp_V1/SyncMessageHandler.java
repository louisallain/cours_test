package csp_V1;

class SyncMessageHandler extends Object implements MessageHandler {

    ConcurrentProcess process;

    public SyncMessageHandler(ConcurrentProcess process) {
        this.process = process;
    }

    @Override
    synchronized public void onMessage(Message msg) {
        
        if(msg.getContent().equals(SyncState.SYNC_MSG_READY)) {

            if(this.process.sync_state_table.get(msg.getSourceId()) != SyncState.ACK_SND_READY_RCV) {
               
                // Met à jour l'état du processus
                this.process.sync_state_table.put(msg.getSourceId(), SyncState.NOTHING_SND_READY_RCV);
                this.process.trace("Ready receive from process num " + msg.getSourceId());

                Message response = new Message(msg.getSourceId(), SyncState.SYNC_TAG, SyncState.SYNC_MSG_ACK);
                response.setSourceId(this.process.getMyId());
                this.process.sendMessage(response);
                
                // Met à jour l'état du processus
                this.process.sync_state_table.put(msg.getSourceId(), SyncState.ACK_SND_READY_RCV);
                this.process.trace("ACK sent to process num " + msg.getDestinationId());
            }   
        } 
        else if(msg.getContent().equals(SyncState.SYNC_MSG_ACK)) {

            this.process.sync_state_table.put(msg.getSourceId(), SyncState.ACK_SND_ACK_RCV);
            this.process.neighbour_awaited.countDown();
            this.process.trace("ACK receive from process num " + msg.getSourceId());
            this.process.trace("Number of process awaited " + this.process.neighbour_awaited.getCount());
        }
    }
}