import java.io.File;
import java.util.*;

public class ProcessFiles implements Runnable {

    private final InvertedIndex buildIndex;
    private final ArrayList<Thread> threads;
    private final ArrayList<ProcessFile> runnables;
    private final Map<String, HashSet<Location>> index = new TreeMap<>();
    private final Map<Location, String> indexFilesLines = new HashMap<>();
    private boolean existMoreFiles = true;
    private int id;

    public void Finish() {
        existMoreFiles = false;
    }

    public ProcessFiles(InvertedIndex index) {
        this.buildIndex = index;
        runnables = new ArrayList<ProcessFile>();
        threads = new ArrayList<Thread>();
        id = 1;
    }

    public Map<String, HashSet<Location>> getIndex() {
        return index;
    }

    public Map<Location, String> getIndexFilesLines() {
        return indexFilesLines;
    }

    @Override
    public void run() {
        while(true)
        {
            File nextFile = buildIndex.getNextFile();
            if (nextFile == null) {
                if (!existMoreFiles) break;

                // Dejamos a los otros hilos continuar con su ejecución
                Thread.yield();
                continue;
            }

            ProcessFile task = new ProcessFile(nextFile, id++);
            Thread t = Thread.startVirtualThread(task);
            runnables.add(task);
            threads.add(t);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (ProcessFile processFile : runnables) {
            for (Map.Entry<String, HashSet<Location>> entry : processFile.getIndex().entrySet())
                index
                        .computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                        .addAll(entry.getValue());
            for (Map.Entry<Location, String> entry : processFile.getIndexFilesLines().entrySet())
                indexFilesLines.computeIfAbsent(entry.getKey(), k -> entry.getValue());
        }
    }
}
