/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Indexing.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Indexing {
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

        Map<String, HashSet<Location>> old_index = invertedIndex.getIndex();
        Map<Location, String> old_indexFilesLines = invertedIndex.getIndexFilesLines();
        Map<Integer, String> old_files = invertedIndex.getFiles();
        invertedIndex.loadIndex();

        // Comprobar que el Indice Invertido cargado sea igual al salvado.
        try {
            assertEquals(old_index, invertedIndex.getIndex());
            assertEquals(old_indexFilesLines, invertedIndex.getIndexFilesLines());
            assertEquals(old_files, invertedIndex.getFiles());
        }catch (AssertionError e){
            System.out.println(invertedIndex.ANSI_RED+ e.getMessage() + " "+ invertedIndex.ANSI_RESET);
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[All Stages] Total execution time: %.3f secs.\n", timeElapsed/1000.0);
    }
}