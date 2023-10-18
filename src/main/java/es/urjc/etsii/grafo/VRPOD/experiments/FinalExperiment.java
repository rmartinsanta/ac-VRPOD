package es.urjc.etsii.grafo.VRPOD.experiments;

import es.urjc.etsii.grafo.VRPOD.constructives.VRPODGRASPConstructive;
import es.urjc.etsii.grafo.VRPOD.destructives.RandomMovement;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODExtendedNeighborhood;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.VNS;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.solver.Mork;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FinalExperiment extends AbstractExperiment<VRPODSolution, VRPODInstance> {

    private final AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder;

    public FinalExperiment(AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder) {
        this.builder = builder;
    }

    @Override
    public List<Algorithm<VRPODSolution, VRPODInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<VRPODSolution, VRPODInstance>>();

        String[] iraceOutput = """
                
                """.split("\n");

        algorithms.add(sotaAlgorithm());
        for (int i = 0; i < iraceOutput.length; i++) {
            if (!iraceOutput[i].isBlank()) {
                var algorithm = builder.buildFromStringParams(iraceOutput[i].trim());
                // Wrap algorithms as multistart with "infinite" iterations, so we are consistent with the autoconfig engine.
                // Algorithms will automatically stop when they reach the timelimit for a given instance
                var multistart = new MultiStartAlgorithm<>("ac"+i, algorithm, 1_000_000, 1_000_000, 1_000_000);
                algorithms.add(multistart);
            }
        }

        return algorithms;
    }



    public Algorithm<VRPODSolution, VRPODInstance> sotaAlgorithm() {

        var combi = new VRPODExtendedNeighborhood();
        return new VNS<>("Reimplementation",
                (s, k) -> {
                    int iter = s.getInstance().getNumOccasionalDrivers() * 10;
                    return k > iter ? VNS.KMapper.STOPNOW : 1;
                },
                new VRPODGRASPConstructive(),
                new RandomMovement(50),
                new LocalSearchFirstImprovement<>(Mork.getFMode(), combi)
                );
    }
}
