package cp2022.solution;

import java.util.LinkedList;
import java.util.Queue;

public class OrdersQueue {


    private final LinkedList<MainQNode> queue;

    public OrdersQueue() {

        queue = new LinkedList<>();

    }


    public void addOrder(long thread_id, long patience) {

        queue.addLast(new MainQNode(thread_id, patience));

    }




/*

    // blocking operation
    // adds thread to the end of the queue
    // with initial patience
    public void waitInQ(int patience) {

        throw new RuntimeException("waitInQ unimplemented");
    }

    // wakes thread with the given id
    public void wake(int thread_id) {

        throw new RuntimeException("wake unimplemented");
    }


*/

}
