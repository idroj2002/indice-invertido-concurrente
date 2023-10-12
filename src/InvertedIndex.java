import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


public class InvertedIndex {

    // Constantes:
    private final String EXTENSION = "txt"; // Extensión de los ficheros a procesar
    private final String DEFAULTINDEXDIR = "./Index/"; // Directorio por defecto donde se guarda el indice invertido.

    // Variables de clase:
    private String InputDirPath; // Contiene la ruta del directorio que contiene los ficheros a Indexar.
    private String IndexDirPath; // Contiene la ruta del directorio que contiene el índice invertido.
    private ArrayList<Thread> threads;
    private Map<Integer,String> files = new HashMap<Integer,String>();
    private ArrayList<ProcessFiles> runnables;
    private ConcurrentLinkedDeque<File> filesList;
    private Map<String, HashSet<Location>> index = new TreeMap<>();
    private Thread createVirtualThreads;
    private ProcessFiles createVirtualThreadsRunnable;
    private int fileNumber;
    //private static Map<Character,Integer> resultsMap = new TreeMap<>();

    public InvertedIndex(String InputPath) {
        this.InputDirPath = InputPath;
        this.IndexDirPath = DEFAULTINDEXDIR;
        initCollections();
    }

    public InvertedIndex(String inputDir, String indexDir) {
        this.InputDirPath = inputDir;
        this.IndexDirPath = indexDir;
        initCollections();
    }

    private void initCollections() {
        runnables = new ArrayList<ProcessFiles>();
        threads = new ArrayList<Thread>();
        files = new HashMap<>();
        filesList = new ConcurrentLinkedDeque<File>();
    }

    public File getNextFile() {
        return filesList.poll();
    }

    public void buildIndex() {
        createVirtualThreadsRunnable = new ProcessFiles(this);
        createVirtualThreads = Thread.startVirtualThread(createVirtualThreadsRunnable);
        fileNumber = 1;
        processFilesRecursive(InputDirPath);
        createVirtualThreadsRunnable.Finish();
        try {
            createVirtualThreads.join();
        } catch (Exception e) {
            System.err.println("Join Exception: " + e.getMessage());
        }
        index = createVirtualThreadsRunnable.getIndex();
        if (Indexing.DEBUG) System.out.println(index);
    }

    // Procesamiento recursivo del directorio para buscar los ficheros de texto, almacenandolo en la lista fileList
    private void processFilesRecursive(String dirpath) {
        File file=new File(dirpath);
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
