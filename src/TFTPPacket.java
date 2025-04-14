import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class TFTPPacket {
    //Test
    private static final boolean PRINT_DEBUG = false;
    public static final int MAX_PACKET_SIZE = 516;
    //Non-packet members
    protected static final int OPCODE_SIZE = Short.BYTES;

    //Note: Blocknum being a short limits the maximum supported file size to 16 Gigabytes
    //private static short staticBlockNum = 0;
    protected int size; //the size (in bytes) of the packet (in data form)
    protected ByteBuffer rawData = null; //The raw data of the packet. Null when not yet created

    public enum Opcode {
        READ_RQ((short)1), WRITE_RQ((short)2), DATA((short)3), ACK((short)4), ERROR((short)5), OACK((short)6);
        private final short val;
        Opcode(short v) {val = v;}
        short getVal() {return val;}
        byte[] getRaw() {
            return getRawForShort(val);
        }
    }

    //Packet members
    protected final Opcode opcode;

    public TFTPPacket(Opcode opcode) {
        this.opcode = opcode;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public int getSize() {
        return size;
    }

    //increment and return next staticBlockNum
    //protected short nextBlockNum() {
       ;// return (staticBlockNum++);
    //}

    //Every packet is either valid (well formed and able to be sent) or not valid (unable to be sent)
    abstract boolean validatePacket();

    //Every packet has a representation in bytes with a maximum size of 512 bytes
    abstract byte[] getRaw();

    //every packet has a defined size in Bytes
    abstract int calculateSize();

    //Returns a UTF-16 string representation of a null-terminated ascii string found in the data after the starting location
    //Returns null if none is found
    //pos is updated to the character after the null terminator at the end of the string
    //TODO: See if this actually works
    protected static String getStringFromData(byte[] raw, AtomicInteger pos) {
        int end;
        int start = pos.get();
        assert(start != 0); //If start == 0, pos will not increment and a blank string will be returned.
        for(end = start; end < raw.length; end++)
        {
            if(raw[end] == '\0') {break;}
        }
        pos.set(end + 1); //Set position to 1 after the null terminator
        return new String(raw, start, end-start);
    }

    //determine the packet type of some given raw data which represents a packet
    //Returns null if the given data does not have a valid opcode
    protected static Opcode getOpcodeFromRaw(byte[] raw) {
        //get opcode from raw data
        //first 16 bits, 2 bytes of file represent the opcode
        //first half should always be empty due to the small range of values
        return shortToOpcode((short)raw[1]);
    }

    //Convert short to opcode
    //Null if value is an invalid opcode
    protected static Opcode shortToOpcode(short v) {
        for (Opcode o: Opcode.values()) {
            if(o.getVal() == v)
                return o;
        }
        return null;
    }

    //gets the next 2 bytes from a byte array,
    //increments currLoc twice
    //converts two bytes into a single short
    public static short getNextShortFromData(byte[] raw, AtomicInteger currLoc) {
        //get the next two bytes
        int byte1 = raw[currLoc.get()];
        currLoc.set(currLoc.get() + 1);
        int byte2 = raw[currLoc.get()];
        currLoc.set(currLoc.get() + 1);

        //Java has no unsigned :(
        if(byte1 < 0) {
            byte1 = 256+byte1;
        }
        if(byte2 < 0) {
            byte2 = 256+byte2;
        }

        //Combine them using bitwise operations
        return (short) ((byte1 << 8) | byte2);
    }

    public static byte[] getRawForShort(short val) {
        ByteBuffer temp = ByteBuffer.allocate(Short.BYTES);
        temp.putShort(val);
        return temp.array();
    }


    protected static byte[] UTF16ToASCII(String str) {
        str += "\0";
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    protected static int UTF16AsASCIISize(String str) {
        //printlnDB(Arrays.toString(UTF16ToASCII(str)));
        return UTF16ToASCII(str).length * Byte.BYTES;
    }

    //Allocates an amount of memory to store the raw data representation of a packet.
    protected ByteBuffer allocateForRaw() {
        //if(terminate) {
         //   assert (size < MAX_SIZE);
          //  return ByteBuffer.allocate(MAX_SIZE);
        return ByteBuffer.allocate(size);
    }

    protected static void printlnDB(String str)
    {
       if(PRINT_DEBUG)
           System.out.println(str);
    }
    protected static void printDB(String str)
    {
        if(PRINT_DEBUG)
            System.out.print(str);
    }
}

