import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Proxy {
    public static final String URL_TEMPLATE = "https://www.cs.oswego.edu/~lmurphy6/";
    public static final String img1 = "pixels.jpg";
    public static final String img2 = "dice.png";
    public static final String img3 = "cow.jpeg";
    public static final String img4 = "self_portrait.png";
    public static final String img5 = "toilet.png";
    public static final String img6 = "Wet_Handshake.png";
    public static final String img7 = "yummy_salad.png";


    public static final Set<String> VALID_EXTENSIONS = new HashSet<>(Arrays.asList(".png", ".jpg", ".jpeg"));

    public static final File CACHE_DIR = new File("cache/");

    public static final int MAX_CACHES = 3; //The maximum number of images to cache

    public static final int PORT = 27020;
    public static final int SERVER_PORT = 27020;



    public static void main(String[] args) {
        //1. Clear cache from previous runs
        //2. Receive packet
        //3. Decrypt packet
        //4. put packet into packet object
        //5. obtain URL
        //6. Get image from URL or Cache if it is available
        //7. Cache image if it was not in cache
        //8. Put image into packet
        //9. convert packet object into bytes
        //10. encrypt packet
        //11. Send packets

        //1. Clear cache from previous runs
        try {
            clearCache(CACHE_DIR);
        } catch (FailedDeletionException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        //ProxyClient thread = new ProxyClient();
        //thread.start();
        while(true) {
            Request request = null;
            //2. Receive packet
            try (TFTPSocket socket = new TFTPSocket(SERVER_PORT)) {
                try {
                    request = socket.acceptReadRequest();
                } catch (TFTPException e) {
                    //socket.denyReadRequest();
                    continue;
                }


                //5. obtain URL
                assert request != null;
                String urlStr = request.getFname();

                System.out.println(urlStr);

                if (!ValidateExtension(urlStr)) {
                    System.err.println("Invalid file extension!");
                    continue;
                }

                //6. Get image from URL or Cache if it is available
                String fname = URLToFileName(urlStr);
                //System.out.println(fname);
                byte[] imgBytes = imageFromCache(CACHE_DIR, fname);

                //if imgBytes is null, it was not present in cache
                if (imgBytes == null) {
                    System.out.println("'" + fname + "' not found in cache. Downloading through HTTP...");
                    try {
                        imgBytes = imageFromURL(urlStr);
                        System.out.println("Image '" + fname + "' successfully downloaded!");

                    } catch (URISyntaxException e) {
                        System.err.println("invalid URL:");
                        System.err.println(e.getMessage());

                        socket.denyReadRequest();
                        //System.exit(1);
                        continue;
                    } catch (IOException e) {
                        System.err.println("failed to read file:");
                        System.err.println(e.getMessage());

                        socket.denyReadRequest();
                        //System.exit(1);
                        continue;
                    }

                    //7. Cache image if it was not in cache
                    try {
                        cacheImage(CACHE_DIR, fname, imgBytes);
                    } catch (FailedDeletionException | FailedCacheException e) {
                        System.err.println(e.getMessage());
                    }
                } else {
                    System.out.println("'" + fname + "' successfully read from cache.");
                }

                //TEST. Save images to test folder
                //saveToTest(fname, imgBytes);

                //Send the image to the client
                try {
                    socket.serveReadRequest(imgBytes, request.getOptions());
                } catch(TFTPException e) {
                    System.err.println(e.getMessage());
                    continue;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static long advanceKey(long r)
    {
        r ^= r << 13; r ^= r >>> 7; r ^= r << 17; return r;
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    //Always follow with a call to advance key
    private static byte[] cryptMessage(long key, final byte[] msg)
    {
        //Convert both to byte[]
        byte[] keyBytes = longToBytes(key);
        byte[] cryptedMsg = new byte[msg.length];

        for(int i = 0; i < msg.length; i++) {
            cryptedMsg[i] = (byte) (msg[i] ^ keyBytes[i%keyBytes.length]);
        }

        return cryptedMsg;
    }

    //Makes an image from a string which represents a valid URL
    public static byte[] imageFromURL(String urlStr) throws URISyntaxException, IOException {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream input = connection.getInputStream();

        byte[] bytes = input.readAllBytes();

        connection.disconnect();
        return bytes;
    }

    //Get the filename portion of a URL.
    //TODO: test that a URL that ends in a '/' with no filename portion does not throw an unhandled exception
    public static String URLToFileName(String urlStr) {
        return urlStr.substring(urlStr.lastIndexOf('/')+1);
    }

    public static boolean ValidateExtension(String fname) {
        String extension = fname.substring(fname.lastIndexOf('.'));
        return VALID_EXTENSIONS.contains(extension);
    }

    //If cacheDir contains MAX_CACHES files, delete a file and cache the image
    public static void cacheImage(File cacheDir, String fname, byte[] data)
            throws FailedDeletionException, FailedCacheException {
        File[] dirContents = cacheDir.listFiles();
        //check if a file needs to be deleted to make room. Delete if necessary
        if(dirContents != null && MAX_CACHES <= dirContents.length) {
            if(!dirContents[0].delete()) {
                throw new FailedDeletionException(dirContents[0].getName());
            }
        }
        //cache the file
        try(FileOutputStream out = new FileOutputStream(new File(cacheDir, fname));) {
            out.write(data);
        } catch (IOException e) {
            throw new FailedCacheException(fname);
        }
    }

    //Returns an image as bytes if it exists
    //Returns null if the passed image is not cached
    public static byte[] imageFromCache(File cacheDir, String fname) {
        File file = new File(cacheDir, fname);
        if(!file.exists()) {
            return null; //File is not in cache
        }
        try (FileInputStream input = new FileInputStream(file)) {
            return input.readAllBytes(); //Read data and return
        } catch(IOException e) {
            System.err.println("File '" + fname + "' found in cache was invalid");
            file.delete(); //Something is wrong with this file. Try to delete it and return null
            return null;
        }
    }

    public static void clearCache(File cacheDir /*the directory to clear*/)
            throws FailedDeletionException {
        File[] dirContents = cacheDir.listFiles();
        if (dirContents != null) {
            for (File file : dirContents) {
                if(!file.delete()) {
                    throw new FailedDeletionException(file.getName());
                }
            }
        }
    }

    public static void saveToTest(String fname, byte[] data) {
        try(FileOutputStream out = new FileOutputStream("test/" + fname);) {
            out.write(data);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            //System.exit(1);
        }
    }

    public static String urlFromConsole(String urlTemplate) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter image file name or quit: ");
        System.out.print("https://www.cs.oswego.edu/~lmurphy6/");
        String img = scanner.nextLine();
        String urlToSend = urlTemplate + img;
        return urlToSend;
    }

}
