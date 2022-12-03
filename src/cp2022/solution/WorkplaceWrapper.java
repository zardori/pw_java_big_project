package cp2022.solution;

import cp2022.base.Workplace;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class WorkplaceWrapper extends Workplace{

    private final Workplace workplace;
    private final WorkshopClass workshop;

    private final Semaphore use_guard;

    private boolean ready_to_enter;
    private boolean ready_to_use;

    // queue for ids of the threads that wants to enter this workplace
    private Queue<Long> enter_queue;
    // queue for ids of the threads that wants to switch to this workplace
    private Queue<Long> switch_to_queue;


    public WorkplaceWrapper(Workplace wp,
                            WorkshopClass workshop) {
        super(wp.getId());
        this.workplace = wp;
        this.workshop = workshop;

        // probably may be also unfair
        use_guard = new Semaphore(1, true);

        ready_to_enter = true;
        ready_to_use = true;


    }

    private void use_guard_P() {
        try {
            use_guard.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }
    }

    public void use_guard_V() {
        use_guard.release();
    }



    public void addToEnterQ(long thread_id) {

        enter_queue.add(thread_id);

    }

    public synchronized void addToSwitchToQ(long thread_id) {
        switch_to_queue.add(thread_id);
    }

    public synchronized boolean switchToQIsEmpty() {
        return switch_to_queue.isEmpty();
    }

    public synchronized long getFromSwitchToQ() {
        assert(!switch_to_queue.isEmpty());
        return switch_to_queue.poll();
    }


    public WorkplaceWrapper grabWorkplace() {
        if (ready_to_enter) {
            ready_to_enter = false;
            return this;
        } else {
            return null;
        }
    }






    @Override
    public void use() {

        // starting protocol

        long curr_thread_id = Thread.currentThread().getId();

        workshop.main_mutex_P();

        // If the use method was invoked, it means that the order is complete.
        workshop.main_queue.removeOrder(curr_thread_id);

        // If the use method was invoked, we can safely invoke the inner use method
        // it in previous workplace of the current thread.
        WorkplaceWrapper prev_wp = workshop.thread_id_to_workplace_map.get(curr_thread_id);

        if (prev_wp != null) {
            prev_wp.use_guard_V();
        }

        workshop.thread_id_to_workplace_map.put(curr_thread_id, this);


        // TODO

        workshop.main_mutex_V();

        use_guard_P();

        workplace.use();


        // ending protocol

        // At this moment other thread can enter this workplace
        workshop.main_mutex_P();

        long thread_id;

        if (!switch_to_queue.isEmpty()) {
            thread_id = switch_to_queue.poll();
            workshop.thread_id_to_semaphore_map.get(thread_id).release();
        }


        // TODO

        workshop.main_mutex_V();


    }
}
