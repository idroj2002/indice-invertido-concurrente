import java.io.File;

public class ProcessFile implements Runnable {

    File file;

    public ProcessFile(File f) {
        this.file = f;
    }

    @Override
    public void run() {
        System.out.println(file.getAbsolutePath());
    }
}
