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

import java.util.List;
import java.util.stream.Collectors;

public class IraceResultsExperiment extends AbstractExperiment<VRPODSolution, VRPODInstance> {

    private final AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder;

    public IraceResultsExperiment(AutomaticAlgorithmBuilder<VRPODSolution, VRPODInstance> builder) {
        this.builder = builder;
    }

    @Override
    public List<Algorithm<VRPODSolution, VRPODInstance>> getAlgorithms() {
        var algorithms = List.of(
                builder.buildFromStringParams("ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.07 ROOT_IteratedGreedy.destructionReconstruction=DestroyRebuild ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive_VRPODGRASPConstructive.alpha=0.02 ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.destructive=RandomDeassign ROOT_IteratedGreedy.improver=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_LocalSearchBestImprovement.neighborhood=CombiCompletaNeigh ROOT_IteratedGreedy.maxIterations=496295 ROOT_IteratedGreedy.stopIfNotImprovedIn=546034"),
                builder.buildFromStringParams("ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.06 ROOT_IteratedGreedy.destructionReconstruction=DestroyRebuild ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive_VRPODGRASPConstructive.alpha=0.03 ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.destructive=RandomDeassign ROOT_IteratedGreedy.improver=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_LocalSearchBestImprovement.neighborhood=CombiCompletaNeigh ROOT_IteratedGreedy.maxIterations=743168 ROOT_IteratedGreedy.stopIfNotImprovedIn=977131"),
                builder.buildFromStringParams("ROOT=IteratedGreedy ROOT_IteratedGreedy.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.constructive_VRPODGRASPConstructive.alpha=0.07 ROOT_IteratedGreedy.destructionReconstruction=DestroyRebuild ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive=VRPODGRASPConstructive ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.constructive_VRPODGRASPConstructive.alpha=0.03 ROOT_IteratedGreedy.destructionReconstruction_DestroyRebuild.destructive=RandomDeassign ROOT_IteratedGreedy.improver=LocalSearchBestImprovement ROOT_IteratedGreedy.improver_LocalSearchBestImprovement.neighborhood=CombiCompletaNeigh ROOT_IteratedGreedy.maxIterations=183247 ROOT_IteratedGreedy.stopIfNotImprovedIn=689679"),
            simplifiedSOTA()
        );

        // Run all algorithms with 1_000_000 iteration limit so the run until timelimit
        return algorithms.stream().map(a -> new MultiStartAlgorithm<>(a.getShortName(), a, 1_000_000, 1_000_000, 1_000_000)).collect(Collectors.toList());
    }

    public Algorithm<VRPODSolution, VRPODInstance> simplifiedSOTA() {

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
