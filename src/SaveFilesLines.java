import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SaveFilesLines implements Runnable {
    private final Map<Location, String> indexFilesLines;
    private final String path;


    public SaveFilesLines(Map<Location, String> indexFilesLines, String path) {
        this.indexFilesLines = indexFilesLines;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            File KeyFile = new File(path);
            FileWriter fw = new FileWriter(KeyFile);
            BufferedWriter bw = new BufferedWriter(fw);
            Set<Map.Entry<Location, String>> keySet = indexFilesLines.entrySet();

            for (Map.Entry<Location, String> entry : keySet) {
                bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            bw.close(); // Cerramos el fichero.
        } catch (IOException e) {
            System.err.println("Error creating FilesLines contents file: " + path + "\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
