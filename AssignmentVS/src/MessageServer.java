import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class MessageServer {
    public static void main(String[] args) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(9999);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                if (message.startsWith("SAVE ")) {
                    String content = message.substring(5); // Remove "SAVE "
                    String key = generateUniqueKey(content);
                    saveMessage(content, key);

                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    String response = "KEY " + key;
                    byte[] sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                } else if (message.startsWith("GET ")) {
                    String key = message.substring(4); // Remove "GET "
                    String content = getMessageContent(key);

                    if (content != null) {
                        InetAddress clientAddress = receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();
                        String response = "OK " + content;
                        byte[] sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        socket.send(sendPacket);
                    } else {
                        InetAddress clientAddress = receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();
                        String response = "FAILED";
                        byte[] sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        socket.send(sendPacket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private static void saveMessage(String content, String key) {
        try {
            String fileName = "/Users/krissi15/Desktop/Messages/" + key + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            writer.println(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMessageContent(String key) {
        try {
            String fileName = "/Users/krissi15/Desktop/Messages/" + key + ".txt";
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String content = reader.readLine();
            reader.close();
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    private static String generateUniqueKey(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
