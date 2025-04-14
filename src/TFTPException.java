public class TFTPException extends RuntimeException {
    public TFTPException(TFTPErrorPacket error) {
        super("TFTP Error (" + error.getErrorCode() + "). " + error.getErrorMsg());
    }
}
