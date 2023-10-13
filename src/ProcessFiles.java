import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class ProcessFiles implements Runnable {

    private InvertedIndex buildIndex;
    private ArrayList<Thread> threads;
    private ArrayList<ProcessFile> runnables;
    private final Map<String, HashSet<Location>> index = new TreeMap<>();
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
        }
    }
}
