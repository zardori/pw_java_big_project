package cp2022.solution;

import java.util.LinkedList;

public class OrdersQueue {


    private final LinkedList<MainQNode> queue;

    public OrdersQueue() {

        queue = new LinkedList<>();

    }


    public void addOrder(long thread_id, long patience) {

        queue.addLast(new MainQNode(thread_id, patience));

    }


    // This operation is linear.
    public void removeOrder(long thread_id) {
        queue.removeIf(itr -> itr.thread_id == thread_id);
    }


    // Linear.
    // Decrease patience of all threads that are before the thread with "thread_id".
    public void decreasePatience(long thread_id) {

        for (MainQNode q_node : queue) {
            if (q_node.thread_id == thread_id) {
                break;
            }
            q_node.patience -= 1;
        }
    }


    public long getFirstWaitingThread() {
        assert (!queue.isEmpty());
        return queue.peekFirst().thread_id;
    }


    public boolean isFirstPatience0() {
        if (queue.isEmpty()) {
            return false;
        } else {
            return queue.peekFirst().patience == 0;
        }
    }


}
