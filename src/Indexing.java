import java.time.Duration;
import java.time.Instant;

public class Indexing {
    // Número de hilos
    private static int N;

    // Path al directorio de entrada que contiene los ficheros que se van a procesar.
    private static String path;

    // Extensión de los ficheros a procesar
    private static String extension;
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        InvertedIndex invertedIndex;

        if (args.length < 1 || args.length > 2) {
            System.err.println("Erro in Parameters. Usage: Indexing <SourceDirectory> [<Index_Directory>]");
            System.exit(-1);
        }
        if (args.length < 2)
            invertedIndex = new InvertedIndex(args[0]);
        else
            invertedIndex = new InvertedIndex(args[0], args[1]);

        Instant start = Instant.now();

        invertedIndex.buildIndex();
        invertedIndex.saveInvertedIndex();

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[All Stages] Total execution time: %.3f secs.\n", timeElapsed/1000.0);
    }
}