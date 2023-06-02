package es.urjc.etsii.grafo.VRPOD.auto;

import es.urjc.etsii.grafo.algorithms.scattersearch.ScatterSearch;
import es.urjc.etsii.grafo.autoconfig.service.filter.BlacklistFilterStrategy;

import java.util.Set;

public class AutoconfigBlacklist extends BlacklistFilterStrategy {
    @Override
    public Set<Class<?>> getBlacklist() {
        return Set.of(ScatterSearch.class);
    }
}
