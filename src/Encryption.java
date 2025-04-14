abstract public class Encryption {
    //if key == null, encryption is disabled
    private Long key;
    protected boolean enabled = false;

    public void enable() {enabled = true;}

    public Encryption() {
        this.key = null;
    }

    public Encryption(long key) {
        this.key = key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public void disable() {
        this.key = null;
    }

    //No function if key == null
    abstract byte[] encrypt(byte[] data);
    abstract byte[] decrypt(byte[] data);


}
