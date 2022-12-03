package cp2022.solution;

import cp2022.base.Workplace;

public class WorkplaceWrapper extends Workplace{

    private final Workplace workplace;

    boolean ready_to_enter;
    boolean ready_to_use;






    public WorkplaceWrapper(Workplace wp) {
        super(wp.getId());
        this.workplace = wp;

        ready_to_enter = true;
        ready_to_use = true;


    }


    @Override
    public void use() {

        // starting protocol

        workplace.use();

        // ending protocol


    }
}
