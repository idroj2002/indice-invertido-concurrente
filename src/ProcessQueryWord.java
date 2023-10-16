/* ---------------------------------------------------------------
Práctica 1.
Código fuente: ProcessQueryWord.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class ProcessQueryWord implements Runnable {

    String[] words;
    Map<String, HashSet<Location>> hash;
    Map<Location, Integer> queryMatchings = new TreeMap<Location, Integer>();

    public ProcessQueryWord(String[] words, Map<String, HashSet<Location>> hash) {
        this.words = words;
        this.hash = hash;
    }

    public Map<Location, Integer> getHash() { return queryMatchings; }

    @Override
    public void run() {
        for(String word: words)
        {
            if (word == null)
                continue;
            word = word.toLowerCase();
            // Procesar las distintas localizaciones de esta palabra
            if (hash.get(word)==null)
                continue;

            for(Location loc: hash.get(word))
            {
                // Si no existe esta localización en la tabla de coincidencias, entonces la añadimos con valor inicial a 1.
                Integer value = queryMatchings.putIfAbsent(loc, 1);
                if (value != null) {
                    // Si existe, incrementamos el número de coincidencias para esta localización.
                    queryMatchings.put(loc, value+1);
                }
            }
        }
    }
}
