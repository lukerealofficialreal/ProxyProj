import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//This packet can represent both an OACK packet or the options portion of a read/write request packet
//The only difference is the fact that an OACK starts with opcode 6, whereas the options on a read/write request
//should have no opcode.
public class TFTPOptionPacket extends TFTPPacket {

    //Option Negotiation:
    //
    //Requestee appends their options to their request
    //Server reconstructs an options hashmap from their request
    //Server has a list of all the options it supports
    //Server attempts to get every option it knows about from the reconstructed hashmap and places them into a new hashmap
    //Once finished, the hashmaps are compared with .equals
    //If they are equal, the server constructs an OACK from its new hashmap and sends it back
    //The client repeats this process, and if they are equal again, the transaction can begin.
    //
    //If at either step the hashmaps are not equal, the option negotiation has failed and the transaction cannot proceed.
    //Send an error packet.


    //Non-Packet members
    private final int numOptions;

    //Packet members
    //opcode
    //private ArrayList<Option> options = new ArrayList<>();
    private HashMap<String, String> options = new HashMap<>();

    public TFTPOptionPacket(Opcode opcode, HashMap<String,String> options) {
        super(opcode);

        this.options = options;
        this.numOptions = options.size();

        size = calculateSize();

    }
    //TODO: Add second constructor w/out currLoc
    public TFTPOptionPacket(byte[] data, AtomicInteger currLoc) {
        super(getOpcodeFromRaw(data));
        //Only care about the size of the OACK
        if(isOack()) {
            assert(currLoc.get() == 0);
            currLoc.set(currLoc.get() + OPCODE_SIZE);
        }
        while(currLoc.get() < data.length-1) {
            printlnDB(currLoc.toString());
            String opt = getStringFromData(data,currLoc);
            String val = getStringFromData(data,currLoc);
            if(opt.isEmpty()) {break;}
            options.put(opt, val);
        }
        if(!options.isEmpty()) {
            this.numOptions = options.size();
        } else {
            this.numOptions = 0;
        }
        size = calculateSize();
        if(isOack()) {
            rawData = allocateForRaw();
            rawData.put(Arrays.copyOf(data, size));
        }
    }
    public TFTPOptionPacket(byte[] data) {
        super(getOpcodeFromRaw(data));
        AtomicInteger currLoc = new AtomicInteger(0);
        //Only care about the size of the OACK
        if(isOack()) {
            assert(currLoc.get() == 0);
            currLoc.set(currLoc.get() + OPCODE_SIZE);
        }
        while(currLoc.get() < data.length-1) {
            printlnDB(currLoc.toString());
            String opt = getStringFromData(data,currLoc);
            String val = getStringFromData(data,currLoc);
            if(opt.isEmpty()) {break;}
            options.put(opt, val);
        }
        if(!options.isEmpty()) {
            this.numOptions = options.size();
        } else {
            this.numOptions = 0;
        }
        size = calculateSize();
        if(isOack()) {
            rawData = allocateForRaw();
        }
    }

    public int getNumOptions() {
        return numOptions;
    }
    public String getValue(String key) {
        return options.get(key);
    }
    public boolean optionsEquals(TFTPOptionPacket op2) {
        return this.options.equals(op2.options);
    }
    public HashMap<String, String> getMap() {
        return options;
    }

    public boolean isOack() {
        return opcode == Opcode.OACK;
    }

    private int calculateOptionsSize() {
        int total = 0;

        for(HashMap.Entry<String,String> pair : options.entrySet()) {
            total += UTF16AsASCIISize(pair.getKey());
            total += UTF16AsASCIISize(pair.getValue());
        }

        return total;
    }

    @Override
    public byte[] getRaw() {
        if(rawData != null) {
            return rawData.array();
        } //else, convert to raw data, store in this.rawData, and return
        rawData = allocateForRaw();
        if(isOack()) {
            rawData.put(opcode.getRaw());
        }
        for(HashMap.Entry<String,String> pair : options.entrySet()) {
            rawData.put(UTF16ToASCII(pair.getKey()));
            rawData.put(UTF16ToASCII(pair.getValue()));
        }
        return rawData.array();
    }

    @Override
    boolean validatePacket() {
        return true;
    }

    @Override
    int calculateSize() {
        int total = calculateOptionsSize();
        if(isOack()) {
            total += OPCODE_SIZE;
        }
        return total;
    }
}
