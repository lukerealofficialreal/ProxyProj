import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TFTPAckPacket extends TFTPPacket{
    //Non-Packet members
    private final int BLOCK_NUM_SIZE = Short.BYTES;

    //Packet members
    //opcode, 2 bytes
    private final short blockNum;

    public TFTPAckPacket(Opcode opcode, short blockNum) {
        super(opcode);

        this.blockNum = blockNum;

        this.size = calculateSize();
    }

    public TFTPAckPacket(byte[] data, AtomicInteger currLoc) {
        super(getOpcodeFromRaw(data));
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.blockNum = getNextShortFromData(data, currLoc);

        this.size = calculateSize();

        this.rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }
    public TFTPAckPacket(byte[] data) {
        super(getOpcodeFromRaw(data));
        AtomicInteger currLoc = new AtomicInteger(0);
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.blockNum = getNextShortFromData(data, currLoc);

        this.size = calculateSize();

        this.rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }

    public short getBlockNum() {
        return blockNum;
    }

    public byte[] getRawForBlockNum() {
        return getRawForShort(blockNum);
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
        return rawData.array();
    }

    @Override
    int calculateSize() {
        return OPCODE_SIZE + BLOCK_NUM_SIZE;
    }


}
