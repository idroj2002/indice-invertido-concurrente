/* ---------------------------------------------------------------
Práctica 1.
Código fuente: SaveIndex.java
Grau Informàtica i ADE
Arenas Romero, Jordi. NIF: 39394122K
Barón Pascual, Sergi. NIF: 48281063S
--------------------------------------------------------------- */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SaveIndex implements Runnable {
    // Constantes
    final int MAX_FILES = 200;   // Número máximo de ficheros para salvar el índice invertido.
    final int MIN_FILES = 2;     // Número mínimo de ficheros para salvar el índice invertido.
    final int KEYS_BY_FILE = 1000;

    // Variables
    private final Map<String, HashSet<Location>> index;
    private final String path;
    private final List<Thread> threadList = new ArrayList<>();

    public SaveIndex(Map<String, HashSet<Location>> index, String path) {
        this.index = index;
        this.path = path;
    }

    @Override
    public void run() {
        int numberOfFiles, remainingFiles;
        long remainingKeys, keysByFile;
        Charset utf8 = StandardCharsets.UTF_8;
        Set<String> keySet = index.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        remainingKeys =  keySet.size();
        numberOfFiles = keySet.size() / KEYS_BY_FILE; // 3
        // Calculamos el número de ficheros a crear en función del número de claves que hay en el hash.
        if (numberOfFiles > MAX_FILES)
            numberOfFiles = MAX_FILES;
        if (numberOfFiles < MIN_FILES)
            numberOfFiles = MIN_FILES;
        remainingFiles = numberOfFiles; // 3

        // Bucle para recorrer los ficheros de indice a crear.
        for (int f = 1; f <= numberOfFiles; f++)
        {
            try {
                File KeyFile = new File(path + String.format("%03d", f));
                FileWriter fw = new FileWriter(KeyFile);
                BufferedWriter bw = new BufferedWriter(fw);
                // Calculamos el número de claves a guardar en este fichero.
                keysByFile = remainingKeys / remainingFiles;
                remainingKeys -= keysByFile;
                remainingFiles--;

                Set<String> subKeySet = new LinkedHashSet<>();
                for (long k = 0; k < keysByFile; k++) {
                    if (keyIterator.hasNext()) subKeySet.add(keyIterator.next());
                }
                Runnable saveIndexFile = new SaveIndexFile(index, subKeySet, bw);
                Thread saveIndexFileThread = Thread.startVirtualThread(saveIndexFile);
                threadList.add(saveIndexFileThread);
            } catch (IOException e) {
                System.err.println("Error creating Index file " + path + String.format("%03d", f));
                e.printStackTrace();
                System.exit(-1);
            }
        }
        try {
            for (Thread t: threadList) {
                t.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Join exception: " + e.getMessage());
        }
    }
}
