package com.nashgoldd.mysticrealm.supernatural.multiblock.build;

import java.util.ArrayList;
import java.util.List;

/**
 * Fila global de {@link MultiblockBuildJob}s em andamento, avançada a cada tick do servidor
 * por {@link MultiblockBuildTickHandler}. Genérico — sem referência a vampiros.
 */
public final class MultiblockBuildQueue {

    private static final List<MultiblockBuildJob> JOBS = new ArrayList<>();

    private MultiblockBuildQueue() {}

    public static void enqueue(MultiblockBuildJob job) {
        JOBS.add(job);
    }

    public static void tick() {
        JOBS.removeIf(MultiblockBuildJob::tick);
    }
}
