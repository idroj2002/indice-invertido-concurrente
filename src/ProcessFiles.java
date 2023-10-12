import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.Thread.sleep;

public class ProcessFiles implements Runnable {

    private InvertedIndex index;
    private ArrayList<Thread> threads;
    private ArrayList<ProcessFile> runnables;
    private boolean existMoreFiles = true;
    private int id;

    public void Finish() {
        existMoreFiles = false;
    }

    public ProcessFiles(InvertedIndex index) {
        this.index = index;
        runnables = new ArrayList<ProcessFile>();
        threads = new ArrayList<Thread>();
        id = 1;
    }

    @Override
    public void run() {
        while(true)
        {
            File nextFile = index.getNextFile();
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
    }
}
