package es.urjc.etsii.grafo.VRPOD.algorithm;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static es.urjc.etsii.grafo.util.ConcurrencyUtil.awaitAll;

@SuppressWarnings("DuplicatedCode")
public class SeqExchangerILS extends Algorithm<VRPODSolution, VRPODInstance> {

    private static final Logger log = LoggerFactory.getLogger(SeqExchangerILS.class);
    private final int nSoluciones;
    private final int nRotateRounds;
    private final ILSConfig[] configs;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     */
    public SeqExchangerILS(String name, int nSoluciones, int nRotateRounds, ILSConfig... configs) {
        super(name);
        this.nSoluciones = nSoluciones;
        this.nRotateRounds = nRotateRounds;
        this.configs = configs;
    }

    /**
     * Executes the algorythm for the given instance
     *
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    public VRPODSolution algorithm(VRPODInstance ins) {

//        int nThreads = Runtime.getRuntime().availableProcessors() / 2;
//
        var nWorkers = this.configs.length;
//        if (nThreads < nWorkers) {
//            System.out.format("[Warning] Available nThreads (%s) is less than the number of configured workers (%s), performance may be reduced\n", nThreads, nWorkers);
//        }

        // Create threads and workers
//        var executor = Executors.newFixedThreadPool(4);
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var first = new ArrayBlockingQueue<VRPODSolution>(1);
        var activeWorkers = new AtomicInteger(nWorkers);
        var barrier = new CyclicBarrier(nWorkers, () -> activeWorkers.set(nWorkers));
        var prev = first;
        var workers = new ArrayList<ILSWorker>();
        for (int i = 0; i < nWorkers; i++) {
            // Next is a new queue if not the last element, and the first element if we are the last
            var next = i == nWorkers - 1 ? first : new ArrayBlockingQueue<VRPODSolution>(1);
            workers.add(new ILSWorker(getBuilder(), executor, configs[i], prev, next, barrier, activeWorkers, nWorkers, nRotateRounds));
            prev = next;
        }

        if (nSoluciones % nWorkers != 0) {
            log.warn("nSolutions is not a multiple of workers, using nearest number: {}", nSoluciones / nWorkers * nWorkers);
        }

        VRPODSolution best = null;
        for (int i = 0; i < nSoluciones / nWorkers; i++) {
            var futures = new ArrayList<Future<VRPODSolution>>();
            for (var worker : workers) {
                futures.add(worker.buildInitialSolution(ins));
            }

            // Wait until all solutions are build before starting the next phase
            var solutions = awaitAll(futures);

            // Shuffle list and start working on them
            //Collections.shuffle(solutions, RandomManager.getRandom());

            // Improvement rounds
            futures = new ArrayList<>();
            for (int j = 0; j < workers.size(); j++) {
                futures.add(workers.get(j).startWorker(solutions.get(j)));
            }

            VRPODSolution current = getBestSolution(futures);
            if (best == null || DoubleComparator.isLess(current.getOptimalValue(), best.getOptimalValue())) {
                best = current;
            }
        }

        executor.shutdown();
        return best;
    }

    private static VRPODSolution getBestSolution(ArrayList<Future<VRPODSolution>> futures) {
        try {
            double min = Double.MAX_VALUE;
            VRPODSolution best = null;
            for(var future: futures){
                var solution = future.get();
                if(solution.getOptimalValue() < min){
                    best = solution;
                    min = solution.getOptimalValue();
                }
            }
            return best;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
