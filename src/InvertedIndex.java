import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import static java.lang.System.exit;


public class InvertedIndex {

    // Constantes:
    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_GREEN_YELLOW_UNDER = "\u001B[32;40;4m";
    private final String ANSI_RESET = "\u001B[0m";
    private final float MATCHING_PERCENTAGE = 0.8f;
    private final float NEARLY_MATCHING_PERCENTAGE = 0.6f;
    private final String EXTENSION = "txt"; // Extensión de los ficheros a procesar
    private final String DEFAULT_INDEX_DIR = "./Index/"; // Directorio por defecto donde se guarda el indice invertido.
    private final String INDEX_FILE_PREFIX = "IndexFile";
    private final String FILES_IDS_NAME = "FilesIds";
    private final String FILE_LINES_NAME = "FilesLinesContent";
    private final int QUERY_WORDS_PER_THREAD = 2;

    // Variables de clase:
    private String inputDirPath; // Contiene la ruta del directorio que contiene los ficheros a Indexar.
    private String indexDirPath; // Contiene la ruta del directorio que contiene el índice invertido.
    private ArrayList<Thread> threads;
    private Map<Integer,String> files = new HashMap<Integer,String>();
    private ArrayList<ProcessFiles> runnables;
    private ConcurrentLinkedDeque<File> filesList;
    private Map<String, HashSet<Location>> index = new ConcurrentHashMap<>();
    private Map<Location, String> indexFilesLines = new TreeMap<Location, String>();
    private Thread createVirtualThreads;
    private ProcessFiles createVirtualThreadsRunnable;
    private int fileNumber;
    private static Map<String,  HashSet<Location>> resultsMap = new TreeMap<String, HashSet <Location>>();

    // Constructores
    public InvertedIndex() {
        initCollections();
    }

    public InvertedIndex(String InputPath) {
        this.inputDirPath = InputPath;
        this.indexDirPath = DEFAULT_INDEX_DIR;
        initCollections();
    }

    public InvertedIndex(String inputDir, String indexDir) {
        this.inputDirPath = inputDir;
        this.indexDirPath = indexDir;
        initCollections();
    }

    private void initCollections() {
        runnables = new ArrayList<ProcessFiles>();
        threads = new ArrayList<Thread>();
        files = new HashMap<>();
        filesList = new ConcurrentLinkedDeque<File>();
    }

    // Getters y Setters

    public File getNextFile() {
        return filesList.poll();
    }

    private String getIndexFilesLine(Location loc){
        return(indexFilesLines.get(loc));
    }

    public void buildIndex() {
        ProcessFiles createVirtualThreadsRunnable = new ProcessFiles(this);
        Thread createVirtualThreads = Thread.startVirtualThread(createVirtualThreadsRunnable);
        fileNumber = 1;
        processFilesRecursive(inputDirPath);
        createVirtualThreadsRunnable.Finish();
        try {
            createVirtualThreads.join();
        } catch (Exception e) {
            System.err.println("Join Exception: " + e.getMessage());
        }
        index = createVirtualThreadsRunnable.getIndex();
        if (Indexing.DEBUG) System.out.println(index);
        saveInvertedIndex();
    }

    // Procesamiento recursivo del directorio para buscar los ficheros de texto, almacenandolo en la lista fileList
    private void processFilesRecursive(String dirpath) {
        File file=new File(dirpath);
        File content[] = file.listFiles();
        if (content != null) {
            for (File value : content) {
                if (value.isDirectory()) {
                    // Si es un directorio, procesarlo recursivamente.
                    processFilesRecursive(value.getAbsolutePath());
                } else {
                    // Si es un fichero de texto, crear un hilo para procesarlo.
                    if (checkFile(value.getName())) {
                        filesList.add(value);
                        files.put(fileNumber++, value.getAbsolutePath());
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

    private void saveInvertedIndex() {
        try {
            resetDirectory(indexDirPath);
            Runnable saveIndex = new SaveIndex(index, indexDirPath);
            Thread saveIndexThread = Thread.startVirtualThread(saveIndex);


            Runnable saveFilesIds = new SaveFilesIds(files, indexDirPath);
            Thread saveFilesIdsThread = Thread.startVirtualThread(saveFilesIds);

            saveIndexThread.join();
            saveFilesIdsThread.join();

            //saveFilesLines(indexDirectory);
        } catch (RuntimeException | InterruptedException e){
            System.err.printf(e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetDirectory(String directory) throws RuntimeException {
        File path = new File(directory);
        if (!path.exists())
            if (!path.mkdir()) throw new RuntimeException("Error creando el directorio " + directory);
        else if (path.isDirectory()) {
            try {
                FileUtils.cleanDirectory(path);
            } catch (IOException e) {
                System.err.printf("Error borrando contenido directorio indice %s.\n",path.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    public void loadIndex(String inputDirectory) {
        loadInvertedIndex(inputDirectory);
        loadFilesIds(inputDirectory);
        loadFilesLines(inputDirectory);
    }

    public void loadInvertedIndex(String inputDirectory) {
        File folder = new File(inputDirectory);
        System.out.println("Dir: " + folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles((d, name) -> name.startsWith(INDEX_FILE_PREFIX));

        // Control de errores
        if (listOfFiles == null) {
            System.err.println("Directory " + inputDirectory + " not found");
            exit(-1);
        }
        if (listOfFiles.length == 0) {
            System.err.println("The input dir " + folder.getAbsolutePath() + " is empty");
            exit(-1);
        }

        ArrayList<BuildIndex> tasks = new ArrayList<BuildIndex>();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (File file : listOfFiles) {
            BuildIndex task = new BuildIndex(file);
            Thread thread = Thread.startVirtualThread(task);
            tasks.add(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        resultsMap = combineHashes(tasks);
    }

    private Map<String, HashSet<Location>> combineHashes(ArrayList<BuildIndex> runnables) {
        Map<String, HashSet<Location>> hash = new TreeMap<String, HashSet <Location>>();
        for (BuildIndex buildIndex : runnables) {
            for (Map.Entry<String, HashSet<Location>> entry : buildIndex.getHash().entrySet()) {
                hash
                        .computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                        .addAll(entry.getValue());
            }
        }
        return hash;
    }

    private Map<Location, Integer> combineProcessedQuerys(ArrayList<ProcessQueryWord> runnables) {
        Map<Location, Integer> hash = new TreeMap<Location, Integer>();

        for (ProcessQueryWord processQueryWord : runnables) {
            Map<Location, Integer> map = processQueryWord.getHash();

            for (Map.Entry<Location, Integer> entry : map.entrySet()) {
                Location location = entry.getKey();
                Integer value = entry.getValue();

                hash.merge(location, value, Integer::sum);
            }
        }

        return hash;
    }

    private void loadFilesIds(String inputDirectory) {
        LoadFilesIds task = new LoadFilesIds(inputDirectory + "/" + FILES_IDS_NAME);
        Thread thread = Thread.startVirtualThread(task);
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        files = task.getFiles();
    }

    private void loadFilesLines(String inputDirectory) {
        LoadFilesLines task = new LoadFilesLines(inputDirectory + "/" + FILE_LINES_NAME);
        Thread thread = Thread.startVirtualThread(task);
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        indexFilesLines = task.getIndexFilesLines();
    }

    // Implentar una consulta sobre el indice invertido:
    //  1. Descompone consulta en palabras.
    //  2. Optiene las localizaciones de cada palabra en el indice invertido.
    //  3. Agrupa palabras segun su localizacion en una hash de coincidencias.
    //  4. Recorremos la tabla de coincidencia y mostramos las coincidencias en función del porcentaje de matching.
    public void query(String queryString) {
        Map<Location, Integer> queryMatchings = new TreeMap<Location, Integer>();

        System.out.println ("Searching for query: "+queryString);

        // Pre-procesamiento query
        queryString = Normalizer.normalize(queryString, Normalizer.Form.NFD);
        queryString = queryString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String filter_line = queryString.replaceAll("[^a-zA-Z0-9áÁéÉíÍóÓúÚäÄëËïÏöÖüÜñÑ ]","");

        // Dividimos la línea en palabras.
        String[] words = filter_line.split("\\W+");
        final int querySize = words.length;

        // Creamos los hilos
        ArrayList<ProcessQueryWord> tasks = new ArrayList<ProcessQueryWord>();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        int numberOfThreads = querySize / QUERY_WORDS_PER_THREAD;
        int offset = querySize % QUERY_WORDS_PER_THREAD;
        if (numberOfThreads == 0){
            numberOfThreads = 1;
            offset--;
        }

        System.out.println("Number of threads: " + numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            int numberOfWords = Math.min(querySize, QUERY_WORDS_PER_THREAD);

            if (offset > 0) {
                numberOfWords++;
                offset--;
            }

            System.out.println("Number of words: " + numberOfWords);

            ProcessQueryWord task = new ProcessQueryWord(Arrays.copyOf(words, numberOfWords), resultsMap);
            words = Arrays.copyOfRange(words, numberOfWords, words.length);

            Thread thread = Thread.startVirtualThread(task);
            tasks.add(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        queryMatchings = combineProcessedQuerys(tasks);

        boolean coincidenceFound = false;
        // Recorremos la tabla de coincidencia y mostramos las líneas en donde aparezca más de un % de las palabras de la query.
        for(Map.Entry<Location, Integer> matching : queryMatchings.entrySet()) {
            Location location = matching.getKey();
            if ((matching.getValue() / (float) querySize) == 1.0) {
                coincidenceFound = true;
                System.out.printf(ANSI_GREEN_YELLOW_UNDER + "%.2f%% Full Matching found in line %d of file %s: %s.\n" + ANSI_RESET, (matching.getValue() / (float) querySize) * 100.0, location.getLine(), location.getFileId(), getIndexFilesLine(location));
            } else if ((matching.getValue() / (float) querySize) >= MATCHING_PERCENTAGE) {
                coincidenceFound = true;
                System.out.printf(ANSI_GREEN + "%.2f%% Matching found in line %d of file %s: %s.\n" + ANSI_RESET, (matching.getValue() / (float) querySize) * 100.0, location.getLine(), location.getFileId(), getIndexFilesLine(location));
            } else if ((matching.getValue()/(float)querySize) >= NEARLY_MATCHING_PERCENTAGE) {
                coincidenceFound = true;
                System.out.printf(ANSI_RED + "%.2f%% Weak Matching found in line %d of file %s: %s.\n" + ANSI_RESET, (matching.getValue() / (float) querySize) * 100.0, location.getLine(), location.getFileId(), getIndexFilesLine(location));
            }
        }

        // Mostramos un mensaje si no se ha encontrado ninguna coincidencia
        if (!coincidenceFound)
            System.out.printf(ANSI_RED+"No Matching found.\n"+ANSI_RESET);
    }
}
