import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TFTPDataPacket extends TFTPPacket {

    //Non-Packet members
    private static final int BLOCK_NUM_SIZE = Short.BYTES;
    public static int MAX_DATA_SIZE = 512; //bytes

    //Packet members
    //opcode, 2 bytes
    private final short blockNum;
    private byte[] data = null;

    public TFTPDataPacket(Opcode opcode, byte[] data, short blockNum) {
        super(opcode);

        this.blockNum = blockNum;
        this.data = data;

        this.size = calculateSize();
    }

    public TFTPDataPacket(byte[] data, AtomicInteger currLoc) {
        super(getOpcodeFromRaw(data));
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.blockNum = getNextShortFromData(data, currLoc);

        this.data = new byte[data.length - currLoc.get()];
        System.arraycopy(data, currLoc.get(), this.data, 0, data.length - currLoc.get());

        this.size = calculateSize();

        this.rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }
    public TFTPDataPacket(byte[] data) {
        super(getOpcodeFromRaw(data));
        AtomicInteger currLoc = new AtomicInteger(0);
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.blockNum = getNextShortFromData(data, currLoc);

        this.data = new byte[data.length - currLoc.get()];
        System.arraycopy(data, currLoc.get(), this.data, 0, data.length - currLoc.get());

        this.size = calculateSize();

        this.rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }

    public short getBlockNum() {
        return blockNum;
    }


    //Returns only the data portion of the raw data
    public byte[] getData() {
        return data;
    }

    public byte[] getRawForBlockNum() {
        return getRawForShort(blockNum);
    }

    public boolean isTerminating() {
        return calculateDataSize() < MAX_DATA_SIZE;
    }



    @Override
    boolean validatePacket() {
        return true;
    }

    @Override
    byte[] getRaw() {
        if(rawData != null) {
            return rawData.array();
        }
        rawData = allocateForRaw();
        rawData.put(opcode.getRaw());
        rawData.put(getRawForBlockNum());
        rawData.put(data);
        return rawData.array();
    }

    int calculateDataSize() {
        return data.length * Byte.BYTES;
    }

    @Override
    int calculateSize() {
        return OPCODE_SIZE + BLOCK_NUM_SIZE + calculateDataSize();
    }
}
