import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;


public class InvertedIndex {

    // Extensión de los ficheros a procesar
    private final String EXTENSION = "txt";

    //private static int totalFiles;
    private ArrayList<Thread> threads;
    private ArrayList<ProcessFiles> runnables;
    private ConcurrentLinkedDeque<File> filesList;
    private Thread createVirtualThreads;
    private ProcessFiles createVirtualThreadsRunnable;
    //private static Map<Character,Integer> resultsMap = new TreeMap<>();

    public InvertedIndex() {
        runnables = new ArrayList<ProcessFiles>();
        threads = new ArrayList<Thread>();
        filesList = new ConcurrentLinkedDeque<File>();
    }

    public File getNextFile() {
        return filesList.poll();
    }

    public void processFiles(String dirPath) {
        createVirtualThreadsRunnable = new ProcessFiles(this);
        createVirtualThreads = Thread.startVirtualThread(createVirtualThreadsRunnable);
        System.out.println("Ficheros:");
        processFilesRecursive(dirPath);
        createVirtualThreadsRunnable.Finish();
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
                        System.out.flush();
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
