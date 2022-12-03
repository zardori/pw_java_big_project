package cp2022.solution;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;
import cp2022.base.Workshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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



    @Override
    public Workplace enter(WorkplaceId wid) {



        main_mutex_P();

        // TODO



        main_mutex_V();



        return null;
    }



    @Override
    public Workplace switchTo(WorkplaceId wid) {

        main_mutex_P();

        WorkplaceWrapper desired_workplace = id_to_workplace_map.get(wid);

        // if the desired workplace is free we can just take it (go without blocking)
        WorkplaceWrapper grabbed_wp = desired_workplace.grabWorkplace();

        long thread_id = Thread.currentThread().getId();

        // Every order need to be added to the main orders queue.
        // It will be accomplished when the thread will invoke use() method.
        main_queue.addOrder(thread_id, patience);

        // if the workplace wasn't empty we must join the workplace queue
        if (grabbed_wp == null) {
            desired_workplace.addToSwitchToQ(thread_id);
            thread_id_to_semaphore_map.put(thread_id, new Semaphore(0, true));
        }


        // TODO

        main_mutex_V();


        if (grabbed_wp != null) {
            return grabbed_wp;
        } else {
            private_semaphore_P();
        }

        return desired_workplace;
    }

    @Override
    public void leave() {




    }
}
