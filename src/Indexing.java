public class Indexing {
    // Número de hilos
    private static int N;

    // Path al directorio de entrada que contiene los ficheros que se van a procesar.
    private static String path;

    // Extensión de los ficheros a procesar
    private static String extension;

    public static void main(String[] args) {
        InvertedIndex invertedIndex;

        if (args.length < 1 || args.length > 2)
            System.err.println("Erro in Parameters. Usage: Indexing <SourceDirectory> [<Index_Directory>]");
        if (args.length < 2)
            invertedIndex = new InvertedIndex(args[0]);
        else
            invertedIndex = new InvertedIndex(args[0], args[1]);

        invertedIndex.buildIndex();
    }
}