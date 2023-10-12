import static java.lang.System.exit;
import static java.lang.System.in;

public class Query {
    public static void main(String[] args) {
        String queryString = null;
        String indexDirectory = null;
        InvertedIndex invertedIndex;

        if (args.length !=2) {
            System.err.println("Error in Parameters. Usage: Query <String> <IndexDirectory>");
            exit(0);
        }
        queryString = args[0];
        indexDirectory = args[1];

        invertedIndex = new InvertedIndex();
        invertedIndex.loadInvertedIndex(indexDirectory);
    }
}
