import java.nio.ByteBuffer;

public class XOREncryption extends Encryption{
    private Long key = null;

    public XOREncryption() {
        super();
    }

    public XOREncryption(long key) {
        super(key);
    }

    public void setKey(long key) {
        this.key = advanceKey(key);
    }

    public byte[] encrypt(byte[] data) {

        if(!enabled)
            return data;


        //Convert both to byte[]
        byte[] keyBytes = longToBytes(key);
        byte[] cryptedMsg = new byte[data.length];

        for(int i = 0; i < data.length; i++) {
            cryptedMsg[i] = (byte) (data[i] ^ keyBytes[i%keyBytes.length]);
        }

        return cryptedMsg;
    }

    //Passes through if key is null
    public byte[] decrypt(byte[] data) {
        if(!enabled)
            return data;
        return encrypt(data);
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private static long advanceKey(long r)
    {
        r ^= r << 13; r ^= r >>> 7; r ^= r << 17; return r;
    }
}
