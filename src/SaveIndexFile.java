/* ---------------------------------------------------------------
Práctica 1.
Código fuente: SaveIndexFile.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SaveIndexFile implements Runnable {
    // Variables
    private final Map<String, HashSet<Location>> index;
    private final Set<String> keySet;
    private final BufferedWriter bw;


    public SaveIndexFile(Map<String, HashSet<Location>> index, Set<String> keySet, BufferedWriter bw) {
        this.index = index;
        this.keySet = keySet;
        this.bw = bw;
    }

    @Override
    public void run() {
        String key;

        // Bucle para recorrer los ficheros de indice a crear.
        for (String s : keySet) {
            key = s;
            saveIndexKey(key, bw);  // Salvamos la clave al fichero.
        }
        try {
            bw.close(); // Cerramos el fichero.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveIndexKey(String key, BufferedWriter bw)
    {
        try {
            HashSet<Location> locations = index.get(key);
            // Creamos un string con todos los offsets separados por una coma.
            //String joined1 = StringUtils.join(locations, ";");
            String joined = String.join(";",locations.toString());
            bw.write(key+"\t");
            bw.write(joined.substring(1,joined.length()-1)+"\n");
        } catch (IOException e) {
            System.err.println("Error writing Index file");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}