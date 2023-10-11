public class Main {
    // Número de hilos
    private static int N;

    // Path al directorio de entrada que contiene los ficheros que se van a procesar.
    private static String path;

    // Extensión de los ficheros a procesar
    private static String extension;

    public static void main(String[] args) {
        N = 10;
        path = "./Input";
        extension = ".txt";
        if (args.length>0)
            path = args[0];

        // Establece las variables modificables
        ProcesarDirectorios.Init(N, path, extension);

        // Busca los ficheros entrando en los directorios de forma recursiva
        ProcesarDirectorios.ProcesarDirectorioRecursivo(path);

        // Crea hilos virtuales para procesor los ficheros encontrados
        ProcesarDirectorios.ProcesarFicheros();

        // Muestra los resultados
        ProcesarDirectorios.MostrarResultados();
    }
}