package election;

import csp_V1.*;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

public class Election {

    private ConcurrentProcess process;
    private Set<Integer> neighbours;
    private ElectionEnum state;
    private Weight biggestEverSeen;
    private Weight weight;
    private Integer father;
    private Set<Integer> succs;
    private CountDownLatch blck;

    public Election(int id, String filename, int base_port, Weight weight) {

        this.process = new ConcurrentProcess(id, "node", base_port);
        this.process.setTrace(true);
        this.process.readNeighbouring(filename);
        this.process.startLoop();
        this.neighbours = this.process.getNeighbourSet();
        this.state = ElectionEnum.INITIAL;
        this.biggestEverSeen = weight;
        this.weight = weight;
        this.father = null;
        this.succs = new HashSet<>();
        this.blck = new CountDownLatch(1);

        this.process.addMessageListener(ElectionEnum.EXPLORE_TAG, new MessageHandler() {
      
            @Override
            synchronized public void onMessage(Message msg) {

                int sourceId = msg.getSourceId();
                Quadruplet receiveArgs = Quadruplet.fromMessageContent(msg.getContent());
                int root = receiveArgs.root;
                double rcvWeight = receiveArgs.weight;
                List<Integer> doneList = receiveArgs.done_list;
                List<Integer> todoList = receiveArgs.todo_list;

                if(biggestEverSeen.getValue() > rcvWeight) {
                    if(state == ElectionEnum.INITIAL) {
                        runExploration();
                    }
                }
                else if(biggestEverSeen.getValue() <= rcvWeight) {

                    state = ElectionEnum.DEFEATED;
                    biggestEverSeen.setValue(rcvWeight);
                    father = Integer.valueOf(sourceId);
                    List<Integer> y = new ArrayList<>(neighbours);
                    y.removeAll(doneList);
                    List<Integer> doneListToSend = Election.union(doneList, Integer.valueOf(process.getMyId()));
                    
                    if(y.isEmpty()) {

                        succs = new HashSet<>();
                        if(todoList.isEmpty()) {
                            process.sendMessage(new Message(sourceId, ElectionEnum.CONCLUDE_TAG, ""));
                            blck.countDown();
                        } 
                        else {
                            Quadruplet args = new Quadruplet(root, rcvWeight, doneListToSend, new ArrayList<>(todoList));
                            process.sendMessage(new Message(sourceId, ElectionEnum.TURN_BACK_TAG, args.toMessageContent()));
                        }
                    }
                    else {

                        Integer x = Collections.max(y);
                        succs = new HashSet<>(x);
                        List<Integer> todoListToSend = Election.union(todoList, y);
                        todoListToSend.remove(x);
                        Quadruplet args = new Quadruplet(root, rcvWeight, doneListToSend, todoListToSend);
                        process.sendMessage(new Message(x, ElectionEnum.EXPLORE_TAG, args.toMessageContent()));
                    }
                }
            }
        });

        this.process.addMessageListener(ElectionEnum.TURN_BACK_TAG, new MessageHandler() {
      
            @Override
            synchronized public void onMessage(Message msg) {

                int sourceId = msg.getSourceId();
                Quadruplet receiveArgs = Quadruplet.fromMessageContent(msg.getContent());
                int root = receiveArgs.root;
                double rcvWeight = receiveArgs.weight;
                List<Integer> doneList = receiveArgs.done_list;
                List<Integer> todoList = receiveArgs.todo_list;

                if(biggestEverSeen.getValue() == rcvWeight) {

                    List<Integer> y = Election.intersection(new ArrayList<>(neighbours), todoList);

                    if(y.isEmpty()) {
                        process.sendMessage(new Message(father, ElectionEnum.TURN_BACK_TAG, msg.getContent()));
                    }
                    else {

                        Integer x = Collections.max(y);
                        succs.add(x);
                        List<Integer> todoListToSend =  new ArrayList<>(todoList);
                        todoListToSend.remove(x);
                        Quadruplet args = new Quadruplet(root, rcvWeight, doneList, todoListToSend);
                        process.sendMessage(new Message(x, ElectionEnum.EXPLORE_TAG, args.toMessageContent()));
                    }
                }
            }
        });

        this.process.addMessageListener(ElectionEnum.CONCLUDE_TAG, new MessageHandler() {
      
            @Override
            public synchronized void onMessage(Message msg) {

                int sourceId = msg.getSourceId();

                if(biggestEverSeen.getValue() == weight.getValue()) {
                    state = ElectionEnum.ELECTED;
                    process.printOut("Elected process / Weight : " + process.getMyId() + " / " + weight.getValue());
                }

                Set<Integer> tmp = new HashSet<>(succs);
                
                if(father != null) tmp.add(father);
                tmp.remove(sourceId);
                
                for(Integer x : tmp) {
                    process.sendMessage(new Message(x, ElectionEnum.CONCLUDE_TAG, ""));
                }

                blck.countDown();
            }
        });
    }

    private void runExploration() {

        this.process.waitNeighbouring("Wainting for other process at Election:runElection");

        Integer x = Collections.max(this.neighbours);
        this.state = ElectionEnum.CANDIDATE;
        this.father = null;
        this.succs.add(x);

        List<Integer> done_list = new ArrayList<>();
        done_list.add(this.process.getMyId());

        List<Integer> todo_list = new ArrayList<>(this.neighbours);
        todo_list.remove(x);

        Quadruplet root_weight_doneList_todoList = new Quadruplet(this.process.getMyId(), this.weight.getValue(), done_list, todo_list);

        this.process.sendMessage(new Message(x, ElectionEnum.EXPLORE_TAG, root_weight_doneList_todoList.toMessageContent()));

        try {
            blck.await();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }

        process.exitLoop();
        process.printOut("finished");
    }

    public void runElection() {
        if(this.state == ElectionEnum.INITIAL) {
            this.runExploration();
        }
    }

    public static <T> List<T> union(List<T> l1, List<T> l2) {
        Set<T> set = new HashSet<>();
        set.addAll(l1);
        set.addAll(l2);
        return new ArrayList<T>(set);
    }

    public static <T> List<T> union(List<T> l1, T el) {
        Set<T> set = new HashSet<>();
        set.addAll(l1);
        set.add(el);
        return new ArrayList<T>(set);
    }

    public static <T> List<T> intersection(List<T> l1, List<T> l2) {
        
        List<T> l = new ArrayList<>();
        for(T t : l1) {
            if(l2.contains(t)) {
                l.add(t);
            }
        }
        return l;
    }

    public static void main(String[] args){
    
        String filename= args[0];
        int my_id= Integer.parseInt(args[1]);
        int base_port= (args.length>2) ? Integer.parseInt(args[2]):0;
        new Election(my_id, filename, base_port, new Identity(my_id)).runElection();
      }
}