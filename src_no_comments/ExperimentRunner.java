import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.function.IntUnaryOperator;

public class ExperimentRunner {

    private static int[] TABLE_SIZES = { 200_003, 2_000_003, 20_000_027 };
    private static int[] DATA_SIZES  = { 100_000, 1_000_000, 10_000_000 };
    private static int REPEATS = 1;
    private static final long SEED = 42L;

    private static boolean VERBOSE = true;

    private static int[] generateDataset(int n, long seed) {
        Random rnd = new Random(seed + n);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt(1_000_000_000);
        return arr;
    }

    private static long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static void gcPause() {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    }

    private static void ensureResultsHeader(FileWriter fw) throws IOException {
        fw.write("run_id,table_type,table_m,hash_name,data_n,phase,metric,value\n");
    }

    private static void log(FileWriter fw, int runId, String tableType, int m, String hashName, int n,
                            String phase, String metric, String value) throws IOException {
        fw.write(String.format("%d,%s,%d,%s,%d,%s,%s,%s\n",
                runId, tableType, m, hashName, n, phase, metric, value));
    }

    public static void runAll(String[] args) throws IOException {

        boolean runChain = true, runLin = true, runDouble = true;

        for (String a : args) {
            if (a.equalsIgnoreCase("--quick")) {
                DATA_SIZES = new int[]{100_000};
                TABLE_SIZES = new int[]{200_003};
            } else if (a.startsWith("--tables=")) {
                String v = a.substring("--tables=".length()).toLowerCase();
                runChain  = v.contains("chain");
                runLin    = v.contains("lin");
                runDouble = v.contains("double");
            } else if (a.startsWith("--msizes=")) {
                String v = a.substring("--msizes=".length());
                String[] parts = v.split(",");
                int[] ms = new int[parts.length];
                for (int i = 0; i < parts.length; i++) ms[i] = Integer.parseInt(parts[i].trim());
                TABLE_SIZES = ms;
            } else if (a.startsWith("--repeats=")) {
                REPEATS = Math.max(1, Integer.parseInt(a.substring("--repeats=".length()).trim()));
            } else if (a.equalsIgnoreCase("--quiet")) {
                VERBOSE = false;
            }
        }

        try (FileWriter fw = new FileWriter("results/metrics.csv")) {
            ensureResultsHeader(fw);

            int runId = 0;
            for (int rep = 0; rep < REPEATS; rep++) {
                if (VERBOSE) System.out.printf("=== Repetição %d/%d ===%n", rep+1, REPEATS);

                for (int m : TABLE_SIZES) {
                    IntUnaryOperator hMod = HashFunctions.modPrime(m);
                    IntUnaryOperator hMul = HashFunctions.multiplicacao(m);
                    IntUnaryOperator hMix = HashFunctions.mixMod(m);
                    IntUnaryOperator h2   = HashFunctions.doubleHashSecondary(m);

                    if (runChain) {
                        runChaining(fw, runId++, m, "mod", hMod);
                        runChaining(fw, runId++, m, "mul", hMul);
                        runChaining(fw, runId++, m, "mix", hMix);
                    }

                    if (runLin) {
                        runLinear(fw, runId++, m, "mod", hMod);
                        runLinear(fw, runId++, m, "mul", hMul);
                        runLinear(fw, runId++, m, "mix", hMix);
                    }

                    if (runDouble) {
                        runDoubleHash(fw, runId++, m, "mod+dh", hMod, h2);
                        runDoubleHash(fw, runId++, m, "mul+dh", hMul, h2);
                        runDoubleHash(fw, runId++, m, "mix+dh", hMix, h2);
                    }
                }
            }
        }
    }

    private static void runChaining(FileWriter fw, int runId, int m, String hashName, IntUnaryOperator h) throws IOException {
        for (int n : DATA_SIZES) {
            if (VERBOSE) System.out.printf("[chaining][%s] m=%d, n=%d%n", hashName, m, n);

            int[] data = generateDataset(n, SEED);

            gcPause();
            long memBefore = usedMemoryBytes();

            HashTableChaining table = new HashTableChaining(m, h, n);

            table.clearMetrics();
            long t0 = System.nanoTime();
            int progressStep = Math.max(1, n / 10);
            int cnt = 0;
            for (int x : data) {
                table.insert(x);
                if (VERBOSE && (++cnt % progressStep == 0)) {
                    System.out.printf("  insert %d/%d%n", cnt, n);
                }
            }
            long t1 = System.nanoTime();

            gcPause();
            long memAfter = usedMemoryBytes();

            log(fw, runId, "chaining", m, hashName, n, "insert", "time_ns", Long.toString(t1 - t0));
            log(fw, runId, "chaining", m, hashName, n, "insert", "collisions", Long.toString(table.getCollisions()));
            log(fw, runId, "chaining", m, hashName, n, "insert", "memory_bytes", Long.toString(Math.max(0, memAfter - memBefore)));

            long b0 = System.nanoTime();
            for (int x : data) {
                if (!table.contains(x)) throw new AssertionError("Falha na busca (chaining)!");
            }
            long b1 = System.nanoTime();
            log(fw, runId, "chaining", m, hashName, n, "search", "time_ns", Long.toString(b1 - b0));

            int[] top3 = table.top3ChainLengths();
            log(fw, runId, "chaining", m, hashName, n, "structure", "chain_top1", Integer.toString(top3[0]));
            log(fw, runId, "chaining", m, hashName, n, "structure", "chain_top2", Integer.toString(top3[1]));
            log(fw, runId, "chaining", m, hashName, n, "structure", "chain_top3", Integer.toString(top3[2]));

            HashTableChaining.GapStats gs = table.bucketGapStats();
            log(fw, runId, "chaining", m, hashName, n, "gaps", "min", Integer.toString(gs.min));
            log(fw, runId, "chaining", m, hashName, n, "gaps", "avg", Double.toString(gs.avg));
            log(fw, runId, "chaining", m, hashName, n, "gaps", "max", Integer.toString(gs.max));
        }
    }

    private static void runLinear(FileWriter fw, int runId, int m, String hashName, IntUnaryOperator h) throws IOException {
        for (int n : DATA_SIZES) {
            if (n >= m) continue;
            if (VERBOSE) System.out.printf("[linear][%s] m=%d, n=%d (load=%.3f)%n", hashName, m, n, n/(double)m);

            int[] data = generateDataset(n, SEED);

            gcPause();
            long memBefore = usedMemoryBytes();

            HashTableLinearProbing table = new HashTableLinearProbing(m, h);
            table.clearMetrics();
            long t0 = System.nanoTime();
            int progressStep = Math.max(1, n / 10);
            int cnt = 0;
            for (int x : data) {
                table.insert(x);
                if (VERBOSE && (++cnt % progressStep == 0)) {
                    System.out.printf("  insert %d/%d%n", cnt, n);
                }
            }
            long t1 = System.nanoTime();

            gcPause();
            long memAfter = usedMemoryBytes();

            log(fw, runId, "linear", m, hashName, n, "insert", "time_ns", Long.toString(t1 - t0));
            log(fw, runId, "linear", m, hashName, n, "insert", "collisions", Long.toString(table.getCollisions()));
            log(fw, runId, "linear", m, hashName, n, "insert", "memory_bytes", Long.toString(Math.max(0, memAfter - memBefore)));

            long b0 = System.nanoTime();
            for (int x : data) {
                if (!table.contains(x)) throw new AssertionError("Falha na busca (linear)!");
            }
            long b1 = System.nanoTime();
            log(fw, runId, "linear", m, hashName, n, "search", "time_ns", Long.toString(b1 - b0));

            HashTableLinearProbing.GapStats gs = table.gapStats();
            log(fw, runId, "linear", m, hashName, n, "gaps", "min", Integer.toString(gs.min));
            log(fw, runId, "linear", m, hashName, n, "gaps", "avg", Double.toString(gs.avg));
            log(fw, runId, "linear", m, hashName, n, "gaps", "max", Integer.toString(gs.max));
        }
    }

    private static void runDoubleHash(FileWriter fw, int runId, int m, String hashName, IntUnaryOperator h1, IntUnaryOperator h2) throws IOException {
        for (int n : DATA_SIZES) {
            if (n >= m) continue;
            if (VERBOSE) System.out.printf("[doublehash][%s] m=%d, n=%d (load=%.3f)%n", hashName, m, n, n/(double)m);

            int[] data = generateDataset(n, SEED);

            gcPause();
            long memBefore = usedMemoryBytes();

            HashTableDoubleHashing table = new HashTableDoubleHashing(m, h1, h2);
            table.clearMetrics();
            long t0 = System.nanoTime();
            int progressStep = Math.max(1, n / 10);
            int cnt = 0;
            for (int x : data) {
                table.insert(x);
                if (VERBOSE && (++cnt % progressStep == 0)) {
                    System.out.printf("  insert %d/%d%n", cnt, n);
                }
            }
            long t1 = System.nanoTime();

            gcPause();
            long memAfter = usedMemoryBytes();

            log(fw, runId, "doublehash", m, hashName, n, "insert", "time_ns", Long.toString(t1 - t0));
            log(fw, runId, "doublehash", m, hashName, n, "insert", "collisions", Long.toString(table.getCollisions()));
            log(fw, runId, "doublehash", m, hashName, n, "insert", "memory_bytes", Long.toString(Math.max(0, memAfter - memBefore)));

            long b0 = System.nanoTime();
            for (int x : data) {
                if (!table.contains(x)) throw new AssertionError("Falha na busca (doublehash)!");
            }
            long b1 = System.nanoTime();
            log(fw, runId, "doublehash", m, hashName, n, "search", "time_ns", Long.toString(b1 - b0));

            HashTableDoubleHashing.GapStats gs = table.gapStats();
            log(fw, runId, "doublehash", m, hashName, n, "gaps", "min", Integer.toString(gs.min));
            log(fw, runId, "doublehash", m, hashName, n, "gaps", "avg", Double.toString(gs.avg));
            log(fw, runId, "doublehash", m, hashName, n, "gaps", "max", Integer.toString(gs.max));
        }
    }
}
