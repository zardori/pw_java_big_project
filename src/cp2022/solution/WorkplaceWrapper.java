package cp2022.solution;

import cp2022.base.Workplace;

public class WorkplaceWrapper extends Workplace{

    private final Workplace workplace;

    public WorkplaceWrapper(Workplace wp) {
        super(wp.getId());
        this.workplace = wp;
    }


    @Override
    public void use() {

        // starting protocol

        workplace.use();

        // ending protocol


    }
}
