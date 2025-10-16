import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/** Endereçamento aberto com hash duplo: h(k, i) = (h1(k) + i * h2(k)) mod m */
public class HashTableDoubleHashing {
    private final int m;
    private final int[] table; // -1 = vazio
    private final boolean[] used;
    private final IntUnaryOperator h1, h2;

    // Métricas
    private long collisions = 0;

    public HashTableDoubleHashing(int m, IntUnaryOperator h1, IntUnaryOperator h2) {
        this.m = m;
        this.h1 = h1;
        this.h2 = h2;
        this.table = new int[m];
        Arrays.fill(table, -1);
        this.used = new boolean[m];
    }

    public void clearMetrics() { collisions = 0; }
    public long getCollisions() { return collisions; }

    public void insert(int key) {
        int base = h1.applyAsInt(key);
        int step = h2.applyAsInt(key);
        int pos = base;
        int i = 0;
        while (table[pos] != -1) {
            collisions++;
            i++;
            pos = (base + i * step) % m;
        }
        table[pos] = key;
        used[pos] = true;
    }

    public boolean contains(int key) {
        int base = h1.applyAsInt(key);
        int step = h2.applyAsInt(key);
        int pos = base;
        int i = 0;
        while (used[pos]) {
            if (table[pos] == key) return true;
            i++;
            pos = (base + i * step) % m;
            if (i > m) break; // segurança
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
