import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Stopwatch implements AutoCloseable {

    BufferedWriter writer;
    private static final char DELIMITER = ',';

    //Arraylist which will store
    HashMap<Short, Long> times = new HashMap<>();
    boolean enable = false;
    String runName;

    public Stopwatch(String runName) {
        this.runName = runName;
    }
    public Stopwatch() {
        this.runName = "blank";
    }

    public void start(Short key) {
        if(enable) {
            times.put(key, System.nanoTime());
        }
    }

    public void stop(Short key, int numBytes) {
        if(enable) {
            Long newTime = System.nanoTime();
            Long prevTime = times.get(key);
            if (prevTime == null) {
                return;
            } else {
                times.remove(key);
                try {
                    writer.write(Double.toString(bitsPerSecond(numBytes, newTime - prevTime)));
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Long getTimeSinceStored(Short key) {
        Long time = times.get(key);
        if(time == null)
            return null;
        else
            return System.nanoTime() - time;
    }

    public void enable() throws IOException {
        this.enable = true;
        if(runName.equals("blank"))
            writer = null;
        else{
            //Date now = new Date();
            writer = new BufferedWriter(new FileWriter(   runName + ".txt"));
        }
    }

    private static Double bitsPerSecond(int bytes, long nano){
        return ((double)bytes*8.0d)/(((double)nano)/1000000000.0d);
    }
    private static Double nanoToMiliseconds(long nano) {
        return (double)nano/1000000.0d;
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
