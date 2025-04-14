import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//A data store which supports efficient storage for both read-only
public class DataStore<T> {
    private T[] readOnlyArr;
    private ArrayList<T> rewritableArray;
    boolean rewritable;

    private DataStore(T[] readOnlyArr, ArrayList<T> rewritableArray, boolean rewritable) {
        this.readOnlyArr = readOnlyArr;
        this.rewritableArray = rewritableArray;
        this.rewritable = rewritable;
    }


    public static <T> DataStore<T> newReadOnlyStore(T[] arr) {
        return new DataStore<T>(arr, null, false);
    }
    public static <T> DataStore<T> newRewritableStore(T[] arr) {
        return new DataStore<T>(null, new ArrayList<T>(Arrays.asList(arr)), true);
    }
    public static <T> DataStore<T> newRewritableStore() {
        return new DataStore<T>(null, new ArrayList<T>(), true);
    }


}