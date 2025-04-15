import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

//pi.cs.oswego.edu

public class ProxyClient extends Thread{
    public static final String URL_TEMPLATE = "https://www.cs.oswego.edu/~lmurphy6/";

    public static final String IMAGE_DIR = "images/";
    public static final String SAVE_DIR = "downloaded/";
    public static final String img1 = "pixels.jpg";
    public static final String img2 = "dice.png";
    public static final String img3 = "cow.jpeg";
    public static final String img4 = "self_portrait.png";
    public static final String img5 = "toilet.png";
    public static final String img6 = "Wet_Handshake.png";
    public static final String img7 = "yummy_salad.png";

    public static final int PORT = 27020;
    public static final int SERVER_PORT = 27020;

    //1. Request image with URL
    //2. Wait for data
    //3. Receive data
    //4. Unencrypt data
    //5. write to file named after the image in IMAGE_DIR
    //6. Open and display the image
    public static void main(String[] args) {
        System.out.println("Client is running");

        //Test images
        /*
        openImage(TEST_DIR, img1);
        openImage(TEST_DIR, img2);
        openImage(TEST_DIR, img3);
        openImage(TEST_DIR, img4);
        openImage(TEST_DIR, img5);
        openImage(TEST_DIR, img6);
        openImage(TEST_DIR, img7);
        */
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server address: ");
         //Test with localhost
        /*
        InetAddress serverAddr = null;
        try {
            serverAddr = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println("Unknown host.");
            System.exit(1);
        }
        */

        InetAddress serverAddr = null;
        try {
            serverAddr = InetAddress.getByName(scanner.nextLine());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host.");
            System.exit(1);
        }

        while(true) {
            String url = urlFromConsole(URL_TEMPLATE);
            String fname = URLToFileName(url);
            if (fname.equals("quit")) {
                System.exit(0);
            }

            //Make default options
            HashMap<String, String> defaultOptions = TFTPSocket.DEFAULT_OPTIONS;

            //Get extra options from client
            appendOptionsFromConsole(defaultOptions);

            //Prepare request
            Request request = new Request(Request.RequestType.READ, url, defaultOptions, serverAddr, SERVER_PORT);

            try (TFTPSocket socket = new TFTPSocket(PORT)) {

                System.out.println("TFTPSocket opened on local port: " + PORT);

                //Send request and receive image
                byte[] imgData;
                try {
                    imgData = socket.read(request);
                } catch (TFTPException e) {
                    System.out.println("read failed: " + e.getMessage());
                    continue;
                }
                //Save image
                saveImage(SAVE_DIR, fname, imgData);

                //compare images
                try {
                    boolean identical = areFilesEqual(IMAGE_DIR + fname, SAVE_DIR + fname);
                    if(identical) {
                        System.out.println("downloaded file == known file");
                    } else {
                        System.out.println("downloaded file != known file");
                    }
                } catch (IOException e) {
                    System.err.println("Error comparing files: " + e.getMessage());
                }

                //Open image
                openImage(SAVE_DIR, fname);

            } catch (SocketException e) {
                System.err.println("Error opening DatagramSocket: " + e.getMessage());
            } catch (UnknownHostException e) {
                System.err.println("Unknown Host: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveImage(String dir, String fname, byte[] data) {
        try (FileOutputStream out = new FileOutputStream(dir + fname)) {
            out.write(data);
            System.out.println("Image saved successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void openImage(String img_dir, String fname) {
        String imagePath = img_dir + fname; // Replace with your image path

        JFrame frame = new JFrame("Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new FlowLayout());

        ImageIcon icon = new ImageIcon(imagePath);
        JLabel label = new JLabel(icon);
        frame.getContentPane().add(label);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void appendOptionsFromConsole(HashMap<String,String> options) {
        String userInput = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter TFTP options or 'cont' to continue");
        System.out.println("Valid options include: \ndrops,<Integer>\nwindowSize,<Integer>");
        while(true) {
            userInput = scanner.nextLine();
            String[] split = userInput.split(",");
            if(userInput.equals("cont")) {
                return;
            } else {
                if(split.length < 2)
                    continue;
                options.put(split[0], split[1]);
            }
        }
    }

    public static String urlFromConsole(String urlTemplate) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter image file name or quit: ");
        System.out.print("https://www.cs.oswego.edu/~lmurphy6/");
        String img = scanner.nextLine();
        return urlTemplate + img;
    }

    public static String URLToFileName(String urlStr) {
        return urlStr.substring(urlStr.lastIndexOf('/')+1);
    }

    public static boolean areFilesEqual(String filePath1, String filePath2) throws IOException {
        Path path1 = Paths.get(filePath1);
        Path path2 = Paths.get(filePath2);

        long mismatch = Files.mismatch(path1, path2);
        return mismatch == -1;
    }
}
