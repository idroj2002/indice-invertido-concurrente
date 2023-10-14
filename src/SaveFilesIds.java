import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SaveFilesIds implements Runnable {
    private final Map<Integer, String> files;
    private final String path;


    public SaveFilesIds(Map<Integer, String> files, String path) {
        this.files = files;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);
            Set<Map.Entry<Integer,String>> keySet = files.entrySet();

            for (Map.Entry<Integer, String> entry : keySet) {
                bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            bw.close(); // Cerramos el fichero.

        } catch (IOException e) {
            System.err.println("Error creating FilesIds file: " + path + "\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
