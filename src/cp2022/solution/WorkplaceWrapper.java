package cp2022.solution;

import cp2022.base.Workplace;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class WorkplaceWrapper extends Workplace{

    private final Workplace workplace;
    private final WorkshopClass workshop;



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

        ready_to_enter = true;
        ready_to_use = true;


    }

    public void addToEnterQ(long thread_id) {

        enter_queue.add(thread_id);

    }

    public void addToSwitchToQ(long thread_id) {
        switch_to_queue.add(thread_id);
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



        // TODO

        workplace.use();

        // ending protocol
        // TODO

    }
}
