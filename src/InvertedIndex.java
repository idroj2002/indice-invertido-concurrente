import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import static java.lang.System.exit;


public class InvertedIndex {

    // Constantes:
    public final String ANSI_RESET = "\u001B[0m";
    public final String ANSI_RED = "\u001B[31m";
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_GREEN_YELLOW_UNDER = "\u001B[32;40;4m";
    private final float MATCHING_PERCENTAGE = 0.8f;
    private final float NEARLY_MATCHING_PERCENTAGE = 0.6f;
    private final String EXTENSION = "txt"; // Extensión de los ficheros a procesar
    private final String DEFAULT_INDEX_DIR = "./Index/"; // Directorio por defecto donde se guarda el indice invertido.
    private final String INDEX_FILE_PREFIX = "IndexFile";
    private final String FILES_IDS_NAME = "FilesIds";
    private final String FILE_LINES_NAME = "FilesLinesContent";

    // Variables de clase:
    private String inputDirPath; // Contiene la ruta del directorio que contiene los ficheros a Indexar.
    private String indexDirPath; // Contiene la ruta del directorio que contiene el índice invertido.
    private Map<Integer,String> files = new HashMap<Integer,String>();
    private ConcurrentLinkedDeque<File> filesList = new ConcurrentLinkedDeque<File>();
    private Map<String, HashSet<Location>> index = new ConcurrentHashMap<>();
    private Map<Location, String> indexFilesLines = new HashMap<>();
    private Thread createVirtualThreads;
    private ProcessFiles createVirtualThreadsRunnable;
    private int fileNumber;
    private static Map<String,  HashSet<Location>> resultsMap = new TreeMap<String, HashSet <Location>>();

    // Constructores
    public InvertedIndex() {

    }

    public InvertedIndex(String InputPath) {
        this.inputDirPath = InputPath;
        this.indexDirPath = DEFAULT_INDEX_DIR;
    }

    public InvertedIndex(String inputDir, String indexDir) {
        this.inputDirPath = inputDir;
        this.indexDirPath = indexDir;
    }

    // Getters y Setters

    public File getNextFile() {
        return filesList.poll();
    }

    public String getIndexFilesLine(Location loc){
        return(indexFilesLines.get(loc));
    }

    public Map<Location, String> getIndexFilesLines() { return indexFilesLines; }

    public Map<String, HashSet<Location>> getIndex() { return index; }

    public Map<Integer, String> getFiles() { return files; }

    public void buildIndex() {
        Instant start = Instant.now();

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
        //if (Indexing.DEBUG) System.out.println("Index: " + index);
        indexFilesLines = createVirtualThreadsRunnable.getIndexFilesLines();

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[Build Index with %d files] Total execution time: %.3f secs.\n", fileNumber - 1, timeElapsed/1000.0);
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
            System.err.printf("Directory does not exist: %s.\n",file.getAbsolutePath());
    }

    private boolean checkFile(String name) {
        return name.endsWith(EXTENSION);
    }

    public void saveInvertedIndex() {
        Instant start = Instant.now();
        try {
            resetDirectory(indexDirPath);
            Runnable saveIndex = new SaveIndex(index, indexDirPath + "/" + INDEX_FILE_PREFIX);
            Thread saveIndexThread = Thread.startVirtualThread(saveIndex);

            Runnable saveFilesIds = new SaveFilesIds(files, indexDirPath + "/" + FILES_IDS_NAME);
            Thread saveFilesIdsThread = Thread.startVirtualThread(saveFilesIds);

            Runnable saveFilesLines = new SaveFilesLines(indexFilesLines, indexDirPath + "/" + FILE_LINES_NAME);
            Thread saveFilesLinesThread = Thread.startVirtualThread(saveFilesLines);

            saveIndexThread.join();
            saveFilesIdsThread.join();
            saveFilesLinesThread.join();

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
            System.out.printf("[Save Index with %d keys] Total execution time: %.3f secs.\n", index.size(), timeElapsed/1000.0);
        } catch (RuntimeException | InterruptedException e){
            System.err.printf(e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetDirectory(String path) throws RuntimeException {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) throw new RuntimeException("Error creating the directory: " + directory.getAbsolutePath());
        } else if (directory.isDirectory()) {
            try {
                FileUtils.cleanDirectory(directory);
            } catch (IOException e) {
                System.err.printf("Error erasing content of index directory: %s.\n",directory.getAbsolutePath());
                e.printStackTrace();
            }
        } else {
            if (Indexing.DEBUG) System.out.println("Index path not a directory: " + directory.getAbsolutePath());
        }
    }

    public void loadIndex() { loadIndex(indexDirPath); }

    public void loadIndex(String inputDirectory) {
        Instant start = Instant.now();

        loadInvertedIndex(inputDirectory);
        loadFilesIds(inputDirectory);
        loadFilesLines(inputDirectory);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[Load Index with %d keys] Total execution time: %.3f secs.\n", index.size(), timeElapsed/1000.0);
    }

    public void loadInvertedIndex(String inputDirectory) {
        File folder = new File(inputDirectory);
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
        Instant start = Instant.now();

        Map<Location, Integer> queryMatchings = new TreeMap<Location, Integer>();

        System.out.println ("Searching for query: "+queryString);

        // Pre-procesamiento query
        queryString = Normalizer.normalize(queryString, Normalizer.Form.NFD);
        queryString = queryString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String filter_line = queryString.replaceAll("[^a-zA-Z0-9áÁéÉíÍóÓúÚäÄëËïÏöÖüÜñÑ ]","");

        // Dividimos la línea en palabras.
        String[] words = filter_line.split("\\W+");
        final int querySize = words.length;

        for(String word: words)
        {
            if (word == null)
                continue;
            word = word.toLowerCase();
            // Procesar las distintas localizaciones de esta palabra
            if (resultsMap.get(word)==null)
                continue;

            for(Location loc: resultsMap.get(word))
            {
                // Si no existe esta localización en la tabla de coincidencias, entonces la añadimos con valor inicial a 1.
                Integer value = queryMatchings.putIfAbsent(loc, 1);
                if (value != null) {
                    // Si existe, incrementamos el número de coincidencias para esta localización.
                    queryMatchings.put(loc, value+1);
                }
            }
        }

        // Versión concurrente (menos óptima)
        /*ArrayList<ProcessQueryWord> tasks = new ArrayList<ProcessQueryWord>();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        System.out.println("Modul: " + querySize % MAX_QUERY_WORDS_PER_THREAD);
        int numberOfThreads = querySize / MAX_QUERY_WORDS_PER_THREAD;
        if (querySize % MAX_QUERY_WORDS_PER_THREAD > 0)
            numberOfThreads += 1;
        int offset = querySize % numberOfThreads;

        System.out.println("Words: " + querySize);
        System.out.println("Number of threads: " + numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            int numberOfWords = querySize / numberOfThreads;

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

        queryMatchings = combineProcessedQuerys(tasks);*/

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

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[Query end] Total execution time: %.3f secs.\n", timeElapsed/1000.0);
    }
}
