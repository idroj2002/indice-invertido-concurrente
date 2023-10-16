/* ---------------------------------------------------------------
Práctica 1.
Código fuente: ProcessFile.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class ProcessFile implements Runnable {

    private final File file;
    private final int fileId;
    private final Map<String, HashSet<Location>> index = new TreeMap<String, HashSet <Location>>();
    private final Map<Location, String> indexFileLines = new LinkedHashMap<>();

    public ProcessFile(File f, int id) {
        this.file = f;
        this.fileId = id;
    }

    public Map<String, HashSet<Location>> getIndex() {
        return index;
    }

    public Map<Location, String> getIndexFileLines() {
        return indexFileLines;
    }

    @Override
    public void run() {
        System.out.printf("Processing %3dth file %s\n", fileId, file.getName());

        // Crear buffer reader para leer el fichero a procesar.
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int lineNumber = 0;  // inicializa contador de líneas a 0.
            while( (line = br.readLine()) != null) {   // Leemos siguiente línea de texto del fichero.
                lineNumber++;
                Location newLocation = new Location(fileId, lineNumber);
                indexFileLines.put(newLocation, line);
                // Eliminamos carácteres especiales de la línea del fichero.
                line = Normalizer.normalize(line, Normalizer.Form.NFD);
                line = line.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                String filter_line = line.replaceAll("[^a-zA-Z0-9áÁéÉíÍóÓúÚäÄëËïÏöÖüÜñÑ ]","");
                // Dividimos la línea en palabras.
                String[] words = filter_line.split("\\W+");
                // Procesar cada palabra
                for(String word: words)
                {
                    word = word.toLowerCase();
                    // Si no existe esa palabra en el indice invertido, creamos una lista vacía de Localizaciones y la añadimos al Indice
                    HashSet<Location> locations = index.computeIfAbsent(word, k -> new HashSet<Location>());
                    // Añadimos nueva localización en la lista de localizaciomes asocidada con ella.
                    locations.add(newLocation);
                }
            }
            if (Indexing.DEBUG) System.out.println("File lines of " + file.getName() + ": " + indexFileLines + "\n");
        } catch (FileNotFoundException e) {
            System.err.printf("Fichero %s no encontrado.\n",file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("Error lectura fichero %s.\n",file.getAbsolutePath());
            e.printStackTrace();
        }
    }


}
