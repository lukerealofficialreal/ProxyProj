public class FailedCacheException extends RuntimeException {
    public FailedCacheException(String fileName) {
        super("Failed to cache file '" + fileName + "'");
    }
}
