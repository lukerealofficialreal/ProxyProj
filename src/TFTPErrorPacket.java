import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TFTPErrorPacket extends TFTPPacket {

    //Non-Packet members
    private static final int ERROR_CODE_SIZE = Short.BYTES;

    //Packet members
    //opcode, 2 bytes
    private short errorCode;
    private String errorMsg;

    public TFTPErrorPacket(Opcode opcode, short errorCode, String errorMsg) {
        super(opcode);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;

        this.size = calculateSize();
    }

    public TFTPErrorPacket(byte[] data, AtomicInteger currLoc) {
        super(getOpcodeFromRaw(data));
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.errorCode = getNextShortFromData(data, currLoc);
        this.errorMsg = getStringFromData(data, currLoc);

        this.size = calculateSize();

        rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }
    public TFTPErrorPacket(byte[] data) {
        super(getOpcodeFromRaw(data));
        AtomicInteger currLoc = new AtomicInteger(0);
        currLoc.set(currLoc.get() + OPCODE_SIZE);

        this.errorCode = getNextShortFromData(data, currLoc);
        this.errorMsg = getStringFromData(data, currLoc);

        this.size = calculateSize();

        rawData = allocateForRaw();
        rawData.put(Arrays.copyOf(data, size));
    }

    public short getErrorCode() {
        return errorCode;
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public byte[] getErrorMsgASCII() {
        return UTF16ToASCII(errorMsg);
    }


    @Override
    boolean validatePacket() {
        return true;
    }

    public byte[] getRawForErrorCode() {
        return getRawForShort(errorCode);
    }

    @Override
    byte[] getRaw() {
        if(rawData != null) {
            return rawData.array();
        }
        rawData = allocateForRaw();
        rawData.put(opcode.getRaw());
        rawData.put(getRawForErrorCode());
        rawData.put(getErrorMsgASCII());
        return rawData.array();
    }

    @Override
    int calculateSize() {
        return OPCODE_SIZE + ERROR_CODE_SIZE + UTF16AsASCIISize(errorMsg);
    }
}
