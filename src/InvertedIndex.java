import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;


public class InvertedIndex {
    // Constatntes:

    private final String EXTENSION = "txt";     // Extensión de los ficheros a procesar

    private final String DefaultIndexDir = "./Index/";     // Directorio por defecto donde se guarda el indice invertido.

    // Variables de clase:
    private String InputDirPath = null;     // Contiene la ruta del directorio que contiene los ficheros a Indexar.
    private String IndexDirPath = null;     // Contiene la ruta del directorio que contiene el índice invertido.

    //private static int totalFiles;
    private ArrayList<Thread> threads;
    private Map<Integer,String> files = new HashMap<Integer,String>();
    private ArrayList<ProcessFiles> runnables;
    private ConcurrentLinkedDeque<File> filesList;
    private Thread createVirtualThreads;
    private ProcessFiles createVirtualThreadsRunnable;
    private int fileNumber;
    //private static Map<Character,Integer> resultsMap = new TreeMap<>();

    public InvertedIndex() {
        runnables = new ArrayList<ProcessFiles>();
        threads = new ArrayList<Thread>();
        files = new HashMap<>();
        filesList = new ConcurrentLinkedDeque<File>();
    }

    public InvertedIndex(String InputPath) {
        this.InputDirPath = InputPath;
        this.IndexDirPath = DefaultIndexDir;
    }

    public InvertedIndex(String inputDir, String indexDir) {
        this.InputDirPath = inputDir;
        this.IndexDirPath = indexDir;
    }

    public File getNextFile() {
        return filesList.poll();
    }

    public void processFiles() {
        createVirtualThreadsRunnable = new ProcessFiles(this);
        createVirtualThreads = Thread.startVirtualThread(createVirtualThreadsRunnable);
        fileNumber = 1;
        processFilesRecursive(InputDirPath);
        createVirtualThreadsRunnable.Finish();
    }

    // Procesamiento recursivo del directorio para buscar los ficheros de texto, almacenandolo en la lista fileList
    private void processFilesRecursive(String dirPath) {
        File file = new File(dirPath);
        File content[] = file.listFiles();
        if (content != null) {
            for (int i = 0; i < content.length; i++) {
                if (content[i].isDirectory()) {
                    // Si es un directorio, procesarlo recursivamente.
                    processFilesRecursive(content[i].getAbsolutePath());
                }
                else {
                    // Si es un fichero de texto, crear un hilo para procesarlo.
                    if (checkFile(content[i].getName())){
                        filesList.add(content[i]);
                        files.put(fileNumber++, content[i].getAbsolutePath());
                    }
                }
            }
        }
        else
            System.err.printf("Directorio %s no existe.\n",file.getAbsolutePath());
    }

    private boolean checkFile(String name) {
        return name.endsWith(EXTENSION);
    }
}
