import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SaveFilesIds implements Runnable {
    final String FILE_IDS_NAME = "FilesIds";
    private final Map<Integer, String> files;
    private final String indexDirPath;


    public SaveFilesIds(Map<Integer, String> files, String indexDirPath) {
        this.files = files;
        this.indexDirPath = indexDirPath;
    }

    @Override
    public void run() {
        try {
            //File IdsFile = new File(outputDirectory +"/"+ DFilesIdsName);
            FileWriter fw = new FileWriter(indexDirPath + "/" + FILE_IDS_NAME);
            BufferedWriter bw = new BufferedWriter(fw);
            Set<Map.Entry<Integer,String>> keySet = files.entrySet();

            for (Map.Entry<Integer, String> entry : keySet) {
                bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            bw.close(); // Cerramos el fichero.

        } catch (IOException e) {
            System.err.println("Error creating FilesIds file: " + indexDirPath + FILE_IDS_NAME + "\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
