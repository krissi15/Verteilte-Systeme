import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

public class MessageServer {
    private static final int PORT = 9999;
    private static final String SERVER_ID = "Server1"; // Eindeutiger Identifier für diesen Server
    private static final int NUM_SERVERS = 3; // Gesamtanzahl der Server im System

    // Liste zur Speicherung der Nachrichten
    private static List<String> messageList = new ArrayList<>();

    public static void main(String[] args) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(PORT);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                if (message.startsWith("SAVE ")) {
                    String content = message.substring(5);
                    String key = generateUniqueKey(content);
                    saveMessage(content, key);

                    // Synchronisierung der Nachricht mit anderen Servern
                    for (int i = 1; i <= NUM_SERVERS; i++) {
                        if (i != getServerNumberFromID(SERVER_ID)) {
                            sendSyncMessageToServer(content, key, i);
                        }
                    }

                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    String response = "KEY " + key;
                    byte[] sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                } else if (message.startsWith("GET ")) {
                    String key = message.substring(4);
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
        messageList.add(key + ": " + content);
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
        for (String message : messageList) {
            if (message.startsWith(key + ": ")) {
                return message.substring(key.length() + 2);
            }
        }

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

    // Funktion zur Sendung der Synchronisierungsnachricht an einen anderen Server
    private static void sendSyncMessageToServer(String content, String key, int serverNumber) {
        try {
            InetAddress serverAddress = InetAddress.getByName("Server" + serverNumber); // Annahme: Server-Adressen sind "Server1", "Server2", "Server3"
            DatagramSocket socket = new DatagramSocket();
            String syncMessage = "SYNC " + key + " " + content;
            byte[] sendData = syncMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            socket.send(sendPacket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Funktion zur Verarbeitung der Synchronisierungsnachricht von einem anderen Server
    private static void processSyncMessage(String key, String content) {
        saveMessage(content, key);
    }

    // Funktion zur Umwandlung des Server-Identifiers in die Servernummer (1, 2, 3)
    private static int getServerNumberFromID(String serverID) {
        return Integer.parseInt(serverID.substring(serverID.length() - 1));
    }
}
