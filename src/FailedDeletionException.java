public class FailedDeletionException extends RuntimeException {
    public FailedDeletionException(String fileName) {
        super("Failed to delete file '" + fileName + "'");
    }
}
