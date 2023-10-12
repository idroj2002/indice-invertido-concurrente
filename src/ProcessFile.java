import java.io.File;

public class ProcessFile implements Runnable {

    private File file;
    private int id;

    public ProcessFile(File f, int id) {
        this.file = f;
        this.id = id;
    }

    @Override
    public void run() {
        System.out.printf("Processing %3dth file %s\n", id, file.getName());
    }
}
