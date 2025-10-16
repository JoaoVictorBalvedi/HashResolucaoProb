import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/** Endereçamento aberto com sondagem linear. */
public class HashTableLinearProbing {
    private final int m;
    private final int[] table;
    private final boolean[] used;
    private final IntUnaryOperator h;

    private long collisions = 0;

    public HashTableLinearProbing(int m, IntUnaryOperator hash) {
        this.m = m;
        this.h = hash;
        this.table = new int[m];
        Arrays.fill(table, -1);
        this.used = new boolean[m];
    }

    public void clearMetrics() { collisions = 0; }
    public long getCollisions() { return collisions; }

    public void insert(int key) {
        int pos = h.applyAsInt(key);
        int start = pos;
        while (table[pos] != -1) {
            collisions++;
            pos = (pos + 1) % m;
            if (pos == start) throw new IllegalStateException("Tabela cheia");
        }
        table[pos] = key;
        used[pos] = true;
    }

    public boolean contains(int key) {
        int pos = h.applyAsInt(key);
        int start = pos;
        while (used[pos]) {
            if (table[pos] == key) return true;
            pos = (pos + 1) % m;
            if (pos == start) break;
        }
        return false;
    }

    public GapStats gapStats() {
        int prev = -1; int first = -1; int last = -1;
        int gapsCount = 0; long sum = 0; int min = Integer.MAX_VALUE; int max = 0;

        for (int i = 0; i < m; i++) {
            if (table[i] != -1) {
                if (first == -1) first = i;
                last = i;
                if (prev != -1) {
                    int gap = i - prev - 1;
                    min = Math.min(min, gap);
                    max = Math.max(max, gap);
                    sum += gap; gapsCount++;
                }
                prev = i;
            }
        }
        if (first != -1 && last != -1 && first != last) {
            int wrapGap = (m - 1 - last) + first;
            min = Math.min(min, wrapGap);
            max = Math.max(max, wrapGap);
            sum += wrapGap; gapsCount++;
        }

        double avg = gapsCount == 0 ? 0 : (double) sum / gapsCount;
        if (gapsCount == 0) { min = 0; max = 0; }
        return new GapStats(min, avg, max);
    }

    public static class GapStats {
        public final int min; public final double avg; public final int max;
        public GapStats(int min, double avg, int max) { this.min = min; this.avg = avg; this.max = max; }
    }
}
