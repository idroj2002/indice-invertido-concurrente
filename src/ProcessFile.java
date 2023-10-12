import java.io.*;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class ProcessFile implements Runnable {

    private final File file;
    private final int fileId;
    private final Map<String, HashSet<Location>> index = new TreeMap<String, HashSet <Location>>();

    public ProcessFile(File f, int id) {
        this.file = f;
        this.fileId = id;
    }

    public Map<String, HashSet<Location>> getIndex() {
        return index;
    }

    @Override
    public void run() {
        System.out.printf("Processing %3dth file %s\n", fileId, file.getName());

        // Crear buffer reader para leer el fichero a procesar.
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int lineNumber = 0;  // inicializa contador de líneas a 0.
            while( (line = br.readLine()) != null)   // Leemos siguiente línea de texto del fichero.
            {
                lineNumber++;
                //TotalLines++;
                if (Indexing.DEBUG) System.out.printf("Procesando linea %d fichero %d: ", lineNumber, fileId);
                Location newLocation = new Location(fileId, lineNumber);
                //IndexFilesLines.put(loc, line);
                // Eliminamos carácteres especiales de la línea del fichero.
                line = Normalizer.normalize(line, Normalizer.Form.NFD);
                line = line.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                String filter_line = line.replaceAll("[^a-zA-Z0-9áÁéÉíÍóÓúÚäÄëËïÏöÖüÜñÑ ]","");
                // Dividimos la línea en palabras.
                String[] words = filter_line.split("\\W+");
                //String[] words = line.split("(?U)\\p{Space}+");
                // Procesar cada palabra
                for(String word:words)
                {
                    if (Indexing.DEBUG) System.out.printf("%s ",word);
                    word = word.toLowerCase();
                    // Obtener entrada correspondiente en el Indice Invertido
                    HashSet<Location> locations = index.computeIfAbsent(word, k -> new HashSet<Location>());
                    // Si no existe esa palabra en el indice invertido, creamos una lista vacía de Localizaciones y la añadimos al Indice
                    //TotalWords++;
                    // Añadimos nueva localización en la lista de localizaciomes asocidada con ella.
                    //int oldLocSize = locations.size();
                    locations.add(newLocation);
                    //if (locations.size()>oldLocSize) TotalLocations++;
                }
                if (Indexing.DEBUG) System.out.println();
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Fichero %s no encontrado.\n",file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("Error lectura fichero %s.\n",file.getAbsolutePath());
            e.printStackTrace();
        }
    }


}
