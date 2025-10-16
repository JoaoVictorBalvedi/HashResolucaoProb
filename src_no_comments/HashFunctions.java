import java.util.function.IntUnaryOperator;

public final class HashFunctions {
    private HashFunctions() {}

    public static IntUnaryOperator modPrime(int m) {
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            return k % m;
        };
    }

    public static IntUnaryOperator multiplicacao(int m) {
        final double A = 0.6180339887498949;
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            double frac = (k * A) % 1.0;
            return (int) Math.floor(m * frac);
        };
    }

    public static IntUnaryOperator mixMod(int m) {
        return (int key) -> {
            int x = key;
            x ^= (x << 13);
            x ^= (x >>> 17);
            x ^= (x << 5);
            x = x >= 0 ? x : -x;
            return x % m;
        };
    }

    public static IntUnaryOperator doubleHashSecondary(int m) {
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            return 1 + (k % (m - 1));
        };
    }
}
