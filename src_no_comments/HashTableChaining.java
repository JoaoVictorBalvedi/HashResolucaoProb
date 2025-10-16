import java.util.Arrays;
import java.util.function.IntUnaryOperator;


public class HashTableChaining {
    private final int m;
    private final int[] head;
    private final int[] next;
    private final int[] keys;
    private final IntUnaryOperator h;
    private int nodeCount = 0;

    private long collisions = 0;

    public HashTableChaining(int m, IntUnaryOperator hash, int capacity) {
        this.m = m;
        this.h = hash;
        this.head = new int[m];
        Arrays.fill(this.head, -1);
        this.keys = new int[capacity];
        this.next = new int[capacity];
    }

    public void clearMetrics() {
        collisions = 0;
    }

    public long getCollisions() {
        return collisions;
    }

    public void insert(int key) {
        int b = h.applyAsInt(key);
        int idx = nodeCount++;
        keys[idx] = key;
        int cur = head[b];
        while (cur != -1) {
            collisions++;
            cur = next[cur];
        }
        next[idx] = head[b];
        head[b] = idx;
    }

    public boolean contains(int key) {
        int b = h.applyAsInt(key);
        int cur = head[b];
        while (cur != -1) {
            if (keys[cur] == key) return true;
            cur = next[cur];
        }
        return false;
    }

    public int[] top3ChainLengths() {
        int a = 0, b = 0, c = 0;
        for (int i = 0; i < m; i++) {
            int len = 0, cur = head[i];
            while (cur != -1) {
                len++;
                cur = next[cur];
            }
            if (len > a) { c = b; b = a; a = len; }
            else if (len > b) { c = b; b = len; }
            else if (len > c) { c = len; }
        }
        return new int[]{a, b, c};
    }

    public GapStats bucketGapStats() {
        int prev = -1;
        int gapsCount = 0;
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;

        for (int i = 0; i < m; i++) {
            if (head[i] != -1) {
                if (prev != -1) {
                    int gap = i - prev - 1;
                    min = Math.min(min, gap);
                    max = Math.max(max, gap);
                    sum += gap;
                    gapsCount++;
                }
                prev = i;
            }
        }
        double avg = gapsCount == 0 ? 0 : (double) sum / gapsCount;
        if (gapsCount == 0) { min = 0; max = 0; }
        return new GapStats(min, avg, max);
    }

    public static class GapStats {
        public final int min;
        public final double avg;
        public final int max;
        public GapStats(int min, double avg, int max) {
            this.min = min; this.avg = avg; this.max = max;
        }
    }
}
