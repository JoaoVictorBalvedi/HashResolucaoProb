public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando experimentos...");
            ExperimentRunner.runAll(args);
            System.out.println("Concluído. Resultados em results/metrics.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
