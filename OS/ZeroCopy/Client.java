package ClientOS;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.io.*;
import java.util.Scanner;
public class Client {


    public static void main(String[] args) {
        try {   
            String serverIP = "192.168.1.19";
            System.out.println("Connecting to server...");
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 55555));
            Socket clientSocket = socketChannel.socket();
            System.out.println("Connect successful");

            Scanner sc = new Scanner(System.in);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line;
            while (!clientSocket.isClosed()) {
                printChoice();
                line = sc.nextLine();
                switch (line) {
                    case "exit":
                        System.out.println("Bye bye!");
                        sc.close();
                        clientSocket.close();
                        break;
                    case "1":
                        out.println(line);
                        int size = Integer.parseInt(in.readLine());
                        System.out.println(CC.PURPLE_BOLD_BRIGHT+"FILE LIST :"+CC.RESET);
                        for (int i = 0; i < size; i++) {
                            System.out.println(CC.GREEN_BOLD_BRIGHT+in.readLine()+CC.RESET);
                        }
                        break;
                    case "2":
                        try {
                            System.out.println(CC.BLUE_BOLD_BRIGHT+"ZERO COPY"+CC.RESET);
                            System.out.print(CC.CYAN_BOLD_BRIGHT+"Please input file name : "+CC.RESET);
                            String filename = sc.nextLine();
                            System.out.println(CC.RED_BOLD_BRIGHT+"Waiting..."+CC.RESET);


                            SocketChannel socketChannel2 = SocketChannel.open(new InetSocketAddress(serverIP, 55555));
                            new Downloader(socketChannel2, filename, "ZEROCOPY").start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "3":
                        try {
                            System.out.println(CC.YELLOW_BOLD_BRIGHT+"TRADITIONAL"+CC.RESET);
                            System.out.print(CC.CYAN_BOLD_BRIGHT+"Please input file name : "+CC.RESET);
                            String filename = sc.nextLine();
                            System.out.println(CC.RED_BOLD_BRIGHT+"Waiting..."+CC.RESET);


                            SocketChannel socketChannel2 = SocketChannel.open(new InetSocketAddress(serverIP, 55555));
                            new Downloader(socketChannel2, filename, "TRADITIONAL").start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        System.out.println("Wrong input");
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection error");
        }

    }
    public static void printChoice() {
        System.out.println("\n\n");
        System.out.println(CC.WHITE_BOLD_BRIGHT+"Type "+CC.PURPLE_BOLD_BRIGHT+"1"+CC.WHITE_BOLD_BRIGHT+" to see "+CC.PURPLE_BOLD_BRIGHT+"all files");
        System.out.println(CC.WHITE_BOLD_BRIGHT+"Type "+CC.BLUE_BOLD_BRIGHT+"2"+CC.WHITE_BOLD_BRIGHT+" to download with "+CC.BLUE_BOLD_BRIGHT+"Zero Copy Method");
        System.out.println(CC.WHITE_BOLD_BRIGHT+"Type "+CC.YELLOW_BOLD_BRIGHT+"3"+CC.WHITE_BOLD_BRIGHT+" to download with "+CC.YELLOW_BOLD_BRIGHT+"Traditional Method");
        System.out.println("\n"+CC.RED_UNDERLINED+CC.RED_BOLD_BRIGHT+"Type exit to exit the program"+CC.RESET);
        System.out.println("\n");
    }

    public static class Downloader extends Thread {
        private long start;
        private long end;
        private SocketChannel client;
        private Socket clientS;
        private String fileName;
        private String type;

        public Downloader(SocketChannel client, String fileName, String type) throws IOException {
            this.client = client;
            this.clientS = client.socket();
            this.fileName = fileName;
            this.type = type;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientS.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientS.getInputStream()));
                FileOutputStream fos = new FileOutputStream(".//" + fileName);
                switch (type) {
                    case "TRADITIONAL":
                        out.println("TRADITIONAL");
                        out.println(fileName);
                        long ends = Long.parseLong(in.readLine());
                
                        byte[] buffer = new byte[64 * 1024];
                    

                        start = System.currentTimeMillis();

                        RandomAccessFile raf = new RandomAccessFile(".//" + fileName, "rw");
                       
                        int bytesRead;
                        InputStream in2 = clientS.getInputStream();
                        long starts = 0;
                        while (starts < ends && (bytesRead = in2.read(buffer)) != -1) {
                            raf.seek(starts);
                            raf.write(buffer);
                            starts += bytesRead;
                        }
                        end = System.currentTimeMillis();

                        raf.close();
                        clientS.close();


                     

                        System.out.println(CC.YELLOW_BOLD+"TRADITIONAL TIME USED : " +CC.GREEN_BOLD_BRIGHT+ (end - start)+CC.RESET);

                        break;
                    case "ZEROCOPY":
                        out.println("ZEROCOPY");
                        out.println(fileName);

                        long fileSize = Long.parseLong(in.readLine());

                        FileChannel fileChannel = fos.getChannel();
                        
                        long transferred = 0;
                        start = System.currentTimeMillis();
                        while (transferred < fileSize) {
                            long count = fileChannel.transferFrom(client, transferred, fileSize - transferred);
                            transferred += count;
                        }
                        end = System.currentTimeMillis();

                        System.out.println(CC.BLUE_BOLD+"ZEROCOPY TIME USED : " +CC.GREEN_BOLD_BRIGHT+(end - start)+CC.RESET);
                        fileChannel.close();
                        fos.close();

                        break;
                    default:

                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
