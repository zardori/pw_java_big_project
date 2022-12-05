package cp2022.solution;

import cp2022.base.Workplace;

import java.util.*;
import java.util.concurrent.Semaphore;

public class WorkplaceWrapper extends Workplace{

    private final Workplace workplace;
    private final WorkshopClass workshop;

    private final Semaphore use_guard;

    private volatile boolean resolving_cycle;

    private volatile boolean ready_to_enter;


    // queue for ids of the threads that wants to enter this workplace
    private final Queue<Long> enter_queue;

    // The set of threads (their ids) that wants to enter this workplace
    private final LinkedList<Long> eager_to_switch;


    public WorkplaceWrapper(Workplace wp,
                            WorkshopClass workshop) {
        super(wp.getId());
        this.workplace = wp;
        this.workshop = workshop;

        // probably may be also unfair
        use_guard = new Semaphore(1, true);

        enter_queue = new LinkedList<>();
        eager_to_switch = new LinkedList<>();


        resolving_cycle = false;
        ready_to_enter = true;

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

    public void setResolvingCycle() {

        resolving_cycle = true;
    }


    // Add thread to the set of threads that wants to switch to this workplace.
    public void addToEagerToSwitch(long thread_id) {
        eager_to_switch.add(thread_id);
    }

    public LinkedList<Long> getEagerToSwitch() {
        return eager_to_switch;

    }

    public void deleteFromEagerToSwitch(Set<Long> values_to_del) {
        eager_to_switch.removeIf(values_to_del::contains);
    }

    public void addToEnterQ(long thread_id) {
        enter_queue.add(thread_id);
    }

    public boolean isReadyToEnter() {

        return ready_to_enter;

    }


    // Ask next waiting thread to come to the workplace.
    // Should be invoked only if workplace is ready to be entered.
    public void askNextThread() {

        long next_thread_id;

        Semaphore thread_semaphore;

        if (!eager_to_switch.isEmpty()) {

            // If the "eager_to_switch" set is not empty ready_to_enter should be always false
            assert(!ready_to_enter);

            next_thread_id = eager_to_switch.poll();
            thread_semaphore = workshop.thread_id_to_semaphore_map.get(next_thread_id);

            assert(thread_semaphore != null);

            thread_semaphore.release();

        } else if (!enter_queue.isEmpty()) {

            next_thread_id = enter_queue.peek();
            thread_semaphore = workshop.thread_id_to_semaphore_map.get(next_thread_id);
            if (workshop.main_queue.isFirstPatience0()) {
                // If patience of the first thread in the main queue is 0,
                // we can only release thread from the workplace queue if it is the same one
                if (next_thread_id == workshop.main_queue.getFirstWaitingThread()) {
                    ready_to_enter = false;
                    enter_queue.poll();
                    thread_semaphore.release();
                } else {
                    ready_to_enter = true;
                }
            } else {
                workshop.main_queue.decreasePatience(next_thread_id);
                ready_to_enter = false;
                enter_queue.poll();
                thread_semaphore.release();
            }

        } else {
            ready_to_enter = true;
        }
    }

    public boolean grabWorkplace() {
        if (ready_to_enter) {
            ready_to_enter = false;
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void use() {

        // starting protocol

        long curr_thread_id = Thread.currentThread().getId();

        workshop.main_mutex_P();

        WorkplaceWrapper prev_wp = workshop.thread_id_to_workplace_map.get(curr_thread_id);

        // If the use method was invoked, it means that the order is complete.
        // Check if it was blocking order. If so, check if free workplaces can be unlocked.
        if (workshop.main_queue.isFirstPatience0() &&
                workshop.main_queue.getFirstWaitingThread() == curr_thread_id) {
            workshop.main_queue.removeOrder(curr_thread_id);
            workshop.checkBlockedWorkplaces();
        } else {
            workshop.main_queue.removeOrder(curr_thread_id);
        }

        // If the use method was invoked, we can safely invoke the inner use method
        // it in previous workplace of the current thread.
        if (prev_wp != null) {
            prev_wp.use_guard_V();

            if (!resolving_cycle) {
                // If we are not resolving cycle previous workplace will be free
                prev_wp.askNextThread();
            } else {
                resolving_cycle = false;
            }
        }

        workshop.thread_id_to_workplace_map.put(curr_thread_id, this);

        workshop.main_mutex_V();

        use_guard_P();

        workplace.use();


    }
}
