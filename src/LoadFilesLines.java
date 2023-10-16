/* ---------------------------------------------------------------
Práctica 1.
Código fuente: LoadFilesLiens.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class LoadFilesLines implements Runnable {

    String path;
    private Map<Location, String> indexFilesLines = new TreeMap<Location, String>();

    public LoadFilesLines(String path) {
        this.path = path;
    }

    public Map<Location, String> getIndexFilesLines() { return indexFilesLines; }

    @Override
    public void run() {
        try {
            FileReader input = new FileReader(path);
            BufferedReader bufRead = new BufferedReader(input);
            String keyLine;
            try {
                // Leemos fichero línea a linea (clave a clave)
                while ((keyLine = bufRead.readLine()) != null) {
                    // Descomponemos la línea leída en su clave (Location) y la linea de texto correspondiente
                    String[] fields = keyLine.split("\t");
                    String[] location = fields[0].substring(1, fields[0].length()-1).split(",");
                    int fileId = Integer.parseInt(location[0]);
                    int line = Integer.parseInt(location[1]);
                    fields[0]="";
                    String textLine = String.join("", fields);
                    indexFilesLines.put(new Location(fileId,line),textLine);
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
