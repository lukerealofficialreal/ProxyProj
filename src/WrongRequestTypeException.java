public class WrongRequestTypeException extends RuntimeException {
    public WrongRequestTypeException(String badRequestType, String goodRequestType) {
        super("Invalid request type '" + badRequestType + ",' expected '" + goodRequestType + ".'");
    }
}
