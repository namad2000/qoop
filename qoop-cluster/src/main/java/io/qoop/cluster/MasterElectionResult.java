package io.qoop.cluster;

public record MasterElectionResult(
        boolean isMaster,
        long generation,
        String currentMaster
) {
    public static MasterElectionResult won(long generation) {
        return new MasterElectionResult(true, generation, null);
    }

    public static MasterElectionResult lost(String currentMaster) {
        return new MasterElectionResult(false, 0L, currentMaster);
    }
}