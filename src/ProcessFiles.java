import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ProcessFiles implements Runnable {

    private InvertedIndex index;
    private ArrayList<Thread> threads;
    private ArrayList<ProcessFile> runnables;
    private volatile Thread blinker;

    public void Finish() {
        Thread moribund = blinker;
        blinker = null;
        moribund.interrupt();
    }

    public ProcessFiles(InvertedIndex index) {
        this.index = index;
        runnables = new ArrayList<ProcessFile>();
        threads = new ArrayList<Thread>();
    }

    @Override
    public void run() {
        blinker = Thread.currentThread();
        Thread currentThread = Thread.currentThread();
        while(blinker == currentThread && !currentThread.isInterrupted())
        {
            File nextFile = index.getNextFile();
            if (nextFile == null) {
                // Dejamos a los otros hilos continuar con su ejecución
                Thread.yield();
                continue;
            }

            ProcessFile task = new ProcessFile(nextFile);
            Thread t = Thread.startVirtualThread(task);
            runnables.add(task);
            threads.add(t);
        }
        /*
        * ProcessFiles task = new ProcessFiles();
          Thread t = Thread.startVirtualThread(task);
          runnables.add(task);
          threads.add(t);
        * */
    }
}
