import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MessageClient {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter command (SAVE/GET message key): ");
                String command = scanner.nextLine();
                byte[] sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 9999);
                socket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Server response: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}