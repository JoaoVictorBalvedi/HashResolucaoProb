import java.util.function.IntUnaryOperator;

public final class HashFunctions {
    private HashFunctions() {}

    // h1: resto da divisão por m (típico)
    public static IntUnaryOperator modPrime(int m) {
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            return k % m;
        };
    }

    // h2: multiplicação (Knuth/A = (sqrt(5)-1)/2 aprox 0.618...), depois floor(m * frac(k*A))
    public static IntUnaryOperator multiplicacao(int m) {
        final double A = 0.6180339887498949;
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            double frac = (k * A) % 1.0;
            return (int) Math.floor(m * frac);
        };
    }

    // h3: misturador simples (xorshift leve) + mod m
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

    // Para Hash Duplo: segunda hash não pode dar 0; usa m primo
    public static IntUnaryOperator doubleHashSecondary(int m) {
        // 1 + (k mod (m-1)) garante no mínimo 1
        return (int key) -> {
            int k = key >= 0 ? key : -key;
            return 1 + (k % (m - 1));
        };
    }
}
