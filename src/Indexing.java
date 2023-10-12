public class Indexing {
    // Número de hilos
    private static int N;

    // Path al directorio de entrada que contiene los ficheros que se van a procesar.
    private static String path;

    // Extensión de los ficheros a procesar
    private static String extension;

    public static void main(String[] args) {
        path = "./Input/SmallExample";
        if (args.length>0)
            path = args[0];

        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.processFiles(path);
    }
}