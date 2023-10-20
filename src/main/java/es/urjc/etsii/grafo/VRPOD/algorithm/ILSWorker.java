package es.urjc.etsii.grafo.VRPOD.algorithm;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ILSWorker {
    private final SolutionBuilder<VRPODSolution, VRPODInstance> builder;
    private final ExecutorService executor;
    private final ILSConfig config;
    private final BlockingQueue<VRPODSolution> prev;
    private final BlockingQueue<VRPODSolution> next;
    private final CyclicBarrier barrier;
    private final AtomicInteger activeWorkers; // Used to sync the workers
    private final int nWorkers;
    private final int nRotaterounds;

    public ILSWorker(SolutionBuilder<VRPODSolution, VRPODInstance> builder, ExecutorService executor, ILSConfig config, BlockingQueue<VRPODSolution> prev, BlockingQueue<VRPODSolution> next, CyclicBarrier barrier, AtomicInteger activeWorkers, int nWorkers, int nRotaterounds) {
        this.builder = builder;
        this.executor = executor;
        this.config = config;
        this.prev = prev;
        this.next = next;
        this.activeWorkers = activeWorkers;
        this.barrier = barrier;
        this.nWorkers = nWorkers;
        this.nRotaterounds = nRotaterounds;
    }

    public Future<VRPODSolution> buildInitialSolution(VRPODInstance instance){
        return executor.submit(() -> {
            var solution = this.builder.initializeSolution(instance);
            return config.constructor.construct(solution);
        });
    }

    public Future<VRPODSolution> startWorker(VRPODSolution initialSolution){
        return executor.submit(() -> work(initialSolution));
    }

    private VRPODSolution work(VRPODSolution initialSolution) throws InterruptedException, BrokenBarrierException {
        VRPODSolution best = initialSolution;
        var nShakes = this.config.nShakes == -1? initialSolution.ins.getRecommendedNumberOfShakes(): this.config.nShakes;
        var numeroRebotesDeLaSolucion = this.nWorkers * nRotaterounds;
        var currentNShakes = nShakes / numeroRebotesDeLaSolucion; // Iteraciones de shake que tenemos que hacer por cada ronda

        // Numero de veces que tiene que hacer pull/push
        for (int round = 0; round < numeroRebotesDeLaSolucion && !TimeControl.isTimeUp(); round++) {
            // Do shake
            for (int i = 1; i <= currentNShakes && !TimeControl.isTimeUp(); i++) {
                best = iteration(best, i);
            }

            // Mark current worker as finished
            activeWorkers.decrementAndGet();

            // Keep working until everyone finishes
            while(activeWorkers.get() != 0 && !TimeControl.isTimeUp()){
                best = iteration(best, -10);
            }

            next.add(best);                       // Push para el siguiente thread
            this.barrier.await();                 // Esperamos a que todos  hayan hecho push

            best = prev.take();                   // Pull del thread previo
        }

        return best;
    }

    private VRPODSolution iteration(VRPODSolution best, int it) {
        var current = best.cloneSolution();
        this.config.shake.shake(current, this.config.shakeStrength);
        this.config.improver.improve(current);
        if (current.getOptimalValue() < best.getOptimalValue()) {
            best = current;
        }
        return best;
    }
}
