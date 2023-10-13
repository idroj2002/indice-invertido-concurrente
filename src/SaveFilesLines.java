import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SaveFilesLines implements Runnable {
    final String FILE_LINES_NAME = "FilesLinesContent";
    private final Map<Location, String> indexFilesLines;
    private final String indexDirPath;


    public SaveFilesLines(Map<Location, String> indexFilesLines, String indexDirPath) {
        this.indexFilesLines = indexFilesLines;
        this.indexDirPath = indexDirPath;
    }

    @Override
    public void run() {
        try {
            File KeyFile = new File(indexDirPath + "/" + FILE_LINES_NAME);
            FileWriter fw = new FileWriter(KeyFile);
            BufferedWriter bw = new BufferedWriter(fw);
            Set<Map.Entry<Location, String>> keySet = indexFilesLines.entrySet();

            for (Map.Entry<Location, String> entry : keySet) {
                bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            bw.close(); // Cerramos el fichero.
        } catch (IOException e) {
            System.err.println("Error creating FilesLines contents file: " + indexDirPath + FILE_LINES_NAME + "\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
