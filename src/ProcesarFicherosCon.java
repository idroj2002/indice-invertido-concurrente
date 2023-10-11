import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ProcesarFicherosCon implements Runnable {

    private Map<Character, Integer> resultsMap = new TreeMap<>();
    private int totalCaracteres;

    public ProcesarFicherosCon() {
        totalCaracteres = 0;
    }

    public Map<Character, Integer> getResultsMap() { return resultsMap; }

    @Override
    public void run() {
        File file = ProcesarDirectorios.getNextFile();
        while (file != null) {
            FileReader fr = null;
            try {
                // Crear un objeto FileReader con el path del fichero que toca procesar.
                fr = new FileReader(file);
            } catch (FileNotFoundException e) {
                System.err.printf("Fichero %s no encontrado.\n",file.getAbsolutePath());
                e.printStackTrace();
            }

            // Leer y procesar el fichero
            int c;
            Character car = 'a';
            // Iterar por todos los caracteres del fichero
            while (true)
            {
                try {
                    if (!((c = fr.read()) != -1)) break;

                    car = (char)c;
                    totalCaracteres++;
                    // Si no existe el carácter como clave en el Mapa, añadirla con valor de 1
                    Integer value = resultsMap.putIfAbsent(car, 1);
                    if (value != null) {
                        // Si existe, incrementar el número de ocurrencias para este carácter.
                        resultsMap.put(car, value+1);
                    }

                } catch (IOException e) {
                    System.err.printf("Error lectura fichero %s.\n",file.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            file = ProcesarDirectorios.getNextFile();
        }
    }
}
