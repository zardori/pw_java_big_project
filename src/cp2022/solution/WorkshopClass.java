package cp2022.solution;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;
import cp2022.base.Workshop;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

public class WorkshopClass implements Workshop {

    // For each thread we specify how many threads that came
    // to the workshop after it can enter before it.
    private final long patience;
    private final Semaphore main_mutex;

    private ArrayList<WorkplaceWrapper> workplaces;

    private final ConcurrentHashMap<WorkplaceId, WorkplaceWrapper> id_to_workplace_map;
    /*default*/ final OrdersQueue main_queue;
    /*default*/ final ConcurrentHashMap<Long, Semaphore> thread_id_to_semaphore_map;

    // For thread id determines which workplace was used by the thread most recent.
    /*default*/ final ConcurrentHashMap<Long, WorkplaceWrapper> thread_id_to_workplace_map;


    public WorkshopClass(Collection<Workplace> wps) {

        thread_id_to_workplace_map = new ConcurrentHashMap<>();

        workplaces = new ArrayList<>(wps.size());
        for (Workplace wp : wps) {
            workplaces.add(new WorkplaceWrapper(wp, this));
        }

        id_to_workplace_map = new ConcurrentHashMap<>();
        for (WorkplaceWrapper wp : workplaces) {

            id_to_workplace_map.put(wp.getId(), wp);

        }

        main_queue = new OrdersQueue();

        main_mutex = new Semaphore(1, true);

        thread_id_to_semaphore_map = new ConcurrentHashMap<>();

        patience = 2L * id_to_workplace_map.size() - 1;

    }

    // Makes thread tries to pass its private semaphore
    // (the one matched with its id)
    private void private_semaphore_P() {
        try {
            thread_id_to_semaphore_map.get(Thread.currentThread().getId()).acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }
    }



    public void main_mutex_P() {

        try {
            main_mutex.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }

    }

    public void main_mutex_V() {

        main_mutex.release();

    }


    // Returns thread ids that creates cycle.
    // If there is no cycle returns null.
    // Linear with the number of workplaces.
    public LinkedList<Long> getCycle() {

        for (Map.Entry<Long, WorkplaceWrapper> entry : thread_id_to_workplace_map.entrySet()) {
            Long key = entry.getKey();
            WorkplaceWrapper value = entry.getValue();
        }

        //TODO

        return null;

    }



    @Override
    public Workplace enter(WorkplaceId wid) {

        main_mutex_P();

        WorkplaceWrapper desired_workplace = id_to_workplace_map.get(wid);
        long thread_id = Thread.currentThread().getId();

        // Every order need to be added to the main orders queue.
        // It will be accomplished when the thread will invoke use() method.
        main_queue.addOrder(thread_id, patience);

        // this variable indicates if the thread can enter workshop
        // according to the 2 * N limit
        boolean can_enter_workshop = !main_queue.isFirstPatience0();

        // this variable indicates if the thread managed to grab the workplace offhand
        boolean is_wp_grabbed = false;

        if (can_enter_workshop) {

            // if the desired workplace is free we can just take it (go without blocking)
            is_wp_grabbed = desired_workplace.grabWorkplace();

            if (is_wp_grabbed) {
                // all threads that came before current thread should have patience decreased
                main_queue.decreasePatience(thread_id);
            } else {
                desired_workplace.addToEnterQ(thread_id);
            }

        }

        thread_id_to_semaphore_map.put(thread_id, new Semaphore(0, true));


        main_mutex_V();

        if (!is_wp_grabbed) {
            private_semaphore_P();
        }

        return desired_workplace;
    }



    @Override
    public Workplace switchTo(WorkplaceId wid) {

        main_mutex_P();

        WorkplaceWrapper desired_workplace = id_to_workplace_map.get(wid);

        // if the desired workplace is free we can just take it (go without blocking)
        boolean is_wp_grabbed = desired_workplace.grabWorkplace();

        long thread_id = Thread.currentThread().getId();

        // Every order need to be added to the main orders queue.
        // It will be accomplished when the thread will invoke use() method.
        main_queue.addOrder(thread_id, patience);

        List<Long> threads_in_cycle = null;

        // if the workplace wasn't empty we must wait for it
        if (!is_wp_grabbed) {

            desired_workplace.addToEagerToSwitch(thread_id);

            // cycle resolving: check for cycle
            threads_in_cycle = getCycle();

            // If there is a cycle, wake all threads which are parts of it.
            if (threads_in_cycle != null) {
                for (long t : threads_in_cycle) {
                    thread_id_to_workplace_map.get(t).setResolvingCycle();
                    // curr thread is not waiting on semaphore, so we won't wake it
                    if (thread_id != t) {
                        thread_id_to_semaphore_map.get(t).release();
                    }
                }
            } else {
                // If there is no cycle we must prepare private semaphore to wait on it.
                thread_id_to_semaphore_map.put(thread_id, new Semaphore(0, true));
            }
        }



        main_mutex_V();

        // If there is no free pass and there is no cycle thread must wait.
        if (!is_wp_grabbed && threads_in_cycle == null) {
            private_semaphore_P();
        }


        return desired_workplace;
    }

    @Override
    public void leave() {

        long thread_id = Thread.currentThread().getId();
        WorkplaceWrapper wp = thread_id_to_workplace_map.get(thread_id);
        assert(wp != null);

        thread_id_to_semaphore_map.remove(thread_id);

        wp.use_guard_V();

    }
}
