import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TFTPReadWritePacket extends TFTPPacket {

    //Non-Packet members
    private final int MODE_SIZE = 6; //The size of mode "octet" in ascii, null terminated

    //Packet members
    //opcode; 2 bytes
    private final String fileName;
    // null terminated /0
    private final String mode = "octet"; //octet is the only mode supported
    // null terminated /0
    private final TFTPOptionPacket options;

    public TFTPReadWritePacket(Opcode opcode, String fileName, TFTPOptionPacket options) {
        super(opcode);
        this.fileName = fileName;
        this.options = options;
        super.size = calculateSize();
    }

    public TFTPReadWritePacket(byte[] data, AtomicInteger currLoc) {
        super(getOpcodeFromRaw(data));
        currLoc.set(currLoc.get() + OPCODE_SIZE);
        printlnDB(currLoc.toString());
        this.fileName = getStringFromData(data, currLoc);
        printlnDB(currLoc.toString()); //discard mode
        currLoc.set(currLoc.get() + MODE_SIZE);

        this.options = new TFTPOptionPacket(data, currLoc);
        super.size = calculateSize();

        this.rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }
    public TFTPReadWritePacket(byte[] data) {
        super(getOpcodeFromRaw(data));
        AtomicInteger currLoc = new AtomicInteger(0);
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        printlnDB(currLoc.toString());

        this.fileName = getStringFromData(data, currLoc);
        printlnDB(currLoc.toString()); //discard mode
        //printlnDB("MODE_SIZE = " + String.valueOf(MODE_SIZE) + "\nmode length = " + String.valueOf(UTF16AsASCIISize(mode)));
        //getStringFromData(data, currLoc);
        currLoc.set(currLoc.get() + MODE_SIZE);


        this.options = new TFTPOptionPacket(data, currLoc);
        super.size = calculateSize();



        rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }

    @Override
    public byte[] getRaw() {
        //if raw data is already converted, just return what is already there
        if(rawData != null) {
            return rawData.array();
        } //else, convert to raw data, store in this.rawData, and return
        rawData = allocateForRaw();
        rawData.put(opcode.getRaw());
        rawData.put(getFileNameASCII());
        rawData.put(getModeASCII());
        rawData.put(options.getRaw());
        return rawData.array();
    }

    public String getFileName() {
        return fileName;
    }
    public String getMode() {
        return mode;
    }
    public byte[] getFileNameASCII() {
        return UTF16ToASCII(fileName);
    }
    public byte[] getModeASCII() {
        return UTF16ToASCII(mode);
    }
    public TFTPOptionPacket getOptions() {
        return options;
    }

    @Override
    boolean validatePacket() {
        return true;
    }

    @Override
    public int calculateSize() {
        printlnDB("TFTPReadWritePacket Sizes:");
        printlnDB(String.valueOf(OPCODE_SIZE));
        printlnDB(String.valueOf(UTF16AsASCIISize(fileName)));
        printlnDB(Arrays.toString(UTF16ToASCII(fileName)));
        printlnDB(String.valueOf(UTF16AsASCIISize(mode)));
        printlnDB(Arrays.toString(UTF16ToASCII(mode)));
        printlnDB(String.valueOf(options.getSize()));
        return OPCODE_SIZE + UTF16AsASCIISize(fileName) + UTF16AsASCIISize(mode) + options.getSize();
    }
}
