import java.net.*;
import java.nio.channels.Channels;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        final int PORT = 55555;
        System.out.println("Starting server..");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Listening to port " + PORT);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Connection >" + clientSocket);
                new ClientHandler(clientSocket).start();
            }

        } catch (Exception e) {
            System.out.println("Server failed");
        }
    }

    static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String serverFilePath;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.serverFilePath = "C:\\Users\\oneda\\Desktop\\OsProject\\ServerFiles\\";
        }

        public void sendFileList() {
            File files = new File(serverFilePath);
            if (files.list().length == 0) {
                out.println(1);
                out.println("No file available");
            } else {
                out.println(files.list().length);
                for (String fileName : files.list()) {
                    out.println(fileName);
                }
            }
        }

        @Override
        public void run() {
            String clientInput;
            try {
                while (((clientInput = in.readLine()) != null)) {
                    switch (clientInput) {
                        case "1":
                            sendFileList();
                            break;
                        case "2":
                            String fileName = in.readLine();
                            File file = new File("C:\\Users\\oneda\\Desktop\\OsProject\\ServerFiles\\" + fileName);
                            if(file.exists()){
                                out.println(file.length());
                            }
                            else{
                                out.println(-1);
                            }
                            break;
                        case "DOWNLOAD":
                        
                            String fileN = in.readLine();
                            long start = Long.parseLong(in.readLine());
                            long end = Long.parseLong(in.readLine());

                            RandomAccessFile raf = new RandomAccessFile(new File(serverFilePath + fileN), "r");
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            raf.seek(start);
                            OutputStream os = clientSocket.getOutputStream();

                            while (start < end && (bytesRead = raf.read(buffer)) != -1) {
                                os.write(buffer);
                                start += bytesRead;
                            }
                            raf.close();
                            os.flush();
                            os.close();
                            break;
                        default:
                            System.out.println("Wrong input from user");
                            break;
                    }
                }
            } catch (

            Exception e) {
                System.out.println("CLIENT DISCONNECT");
            }
        }
    }
}

//