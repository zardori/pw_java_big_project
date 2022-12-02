package cp2022.solution;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;
import cp2022.base.Workshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class WorkshopClass implements Workshop {


    private ArrayList<WorkplaceWrapper> workplaces;

    private ConcurrentMap<WorkplaceId, WorkplaceWrapper> id_to_workplace_map;

    public WorkshopClass(Collection<Workplace> wps) {

        workplaces = new ArrayList<>(wps.size());
        for (Workplace wp : wps) {

            workplaces.add(new WorkplaceWrapper(wp));
        }

        for (WorkplaceWrapper wp : workplaces) {

            id_to_workplace_map.put(wp.getId(), wp);

        }

    }



    @Override
    public Workplace enter(WorkplaceId wid) {
        return null;
    }

    @Override
    public Workplace switchTo(WorkplaceId wid) {
        return null;
    }

    @Override
    public void leave() {

    }
}
