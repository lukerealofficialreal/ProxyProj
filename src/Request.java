import java.net.InetAddress;
import java.util.HashMap;

public class Request {
    public enum RequestType {
        READ, WRITE;
    }

    public RequestType type;
    public String fname;
    public HashMap<String, String> options;
    public InetAddress address;
    public int port;
    public Request(RequestType type, String fname, InetAddress address, int port) {
        this.type = type;
        this.fname = fname;
        this.options = null;
        this.address = address;
        this.port = port;
    }

    public Request(RequestType type, String fname, HashMap<String, String> options, InetAddress address, int port) {
        this.type = type;
        this.fname = fname;
        this.options = options;
        this.address = address;
        this.port = port;
    }

    public Request(TFTPReadWritePacket packet, InetAddress address, int port) {
        if (packet.getOpcode() == TFTPPacket.Opcode.READ_RQ) {
            this.type = RequestType.READ;
        } else {
            this.type = RequestType.WRITE;
        }
        this.fname = packet.getFileName();
        this.options = packet.getOptions().getMap();

        this.address = address;
        this.port = port;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getFname() {
        return fname;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
