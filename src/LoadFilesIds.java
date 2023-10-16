/* ---------------------------------------------------------------
Práctica 1.
Código fuente: LoadFilesIds.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoadFilesIds implements Runnable {

    private String path;
    Map<Integer,String> files = new HashMap<Integer,String>();

    public LoadFilesIds(String path) {
        this.path = path;
    }

    public Map<Integer,String> getFiles() { return files; }

    @Override
    public void run() {
        try {
            FileReader input = new FileReader(path);
            BufferedReader bufRead = new BufferedReader(input);
            String keyLine;
            try {
                // Leemos fichero línea a linea (clave a clave)
                while ((keyLine = bufRead.readLine()) != null) {
                    // Descomponemos la línea leída en su clave (File Id) y la ruta del fichero.
                    String[] fields = keyLine.split("\t");
                    int fileId = Integer.parseInt(fields[0]);
                    fields[0]="";
                    String filePath = String.join("", fields);
                    files.put(fileId, filePath);
                }
                bufRead.close();

            } catch (IOException e) {
                System.err.println("Error reading Files Ids");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error opening Files Ids file");
            e.printStackTrace();
        }
    }
}
