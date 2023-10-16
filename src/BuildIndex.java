/* ---------------------------------------------------------------
Práctica 1.
Código fuente: BuildIndex.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class BuildIndex implements Runnable {
    File file;
    private Map<String, HashSet <Location>> hash =  new TreeMap<String, HashSet <Location>>();

    public BuildIndex(File file) {
        this.file = file;
    }

    public Map<String, HashSet <Location>> getHash() { return this.hash; }

    @Override
    public void run() {
        if (file.isFile()) {
            //System.out.println("Processing file " + folder.getPath() + "/" + file.getName()+" -> ");
            try {
                FileReader input = new FileReader(file);
                BufferedReader bufRead = new BufferedReader(input);
                String keyLine;
                try {
                    // Leemos el fichero línea a linea (clave a clave)
                    while ((keyLine = bufRead.readLine()) != null)
                    {
                        HashSet<Location> locationsList = new HashSet<Location>();
                        // Descomponemos la línea leída en su clave (word) y las ubicaciones
                        String[] fields = keyLine.split("\t");
                        String word = fields[0];
                        String[] locations = fields[1].split(", ");
                        // Recorremos los offsets para esta clave y los añadimos al HashMap
                        for (int i = 0; i < locations.length; i++)
                        {
                            String[] location = locations[i].substring(1, locations[i].length()-1).split(",");
                            int fileId = Integer.parseInt(location[0]);
                            int line = Integer.parseInt(location[1]);
                            locationsList.add(new Location(fileId,line));
                        }
                        hash.put(word, locationsList);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading Index file");
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error opening Index file");
                e.printStackTrace();
            }
        }
    }
}
