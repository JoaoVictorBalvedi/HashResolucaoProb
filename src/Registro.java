public class Registro {
    // Armazena o código como int para economizar memória.
    // Formatação para 9 dígitos fica a cargo da saída.
    private final int codigo;

    public Registro(int codigo) {
        if (codigo < 0 || codigo > 999_999_999) {
            throw new IllegalArgumentException("Código deve ter até 9 dígitos (0..999999999)");
        }
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String codigoFormatado() {
        return String.format("%09d", codigo);
    }
}
