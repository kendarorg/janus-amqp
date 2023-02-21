package org.kendar.amqp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        // Step 1: Establish a network connection with the client
        ServerSocket serverSocket = new ServerSocket(5672); // Use the default AMQP port
        Socket clientSocket = serverSocket.accept();
        // Step 2: Create an input stream to read binary data from the network
        InputStream inputStream = clientSocket.getInputStream();
        // Step 3: Read the AMQP header from the input stream
        byte[] header = new byte[5]; // The AMQP header consists of 5 bytes
        int bytesRead = inputStream.read(header);
        if (bytesRead != 5) {
            throw new IOException("Failed to read AMQP header");
        }
        // Verify that the protocol ID and version number match the expected values
        if (header[0] != 'A' || header[1] != 'M' || header[2] != 'Q' || header[3] != 'P' || header[4] != 0) {
            throw new IOException("Invalid AMQP header");
        }
        // Step 4: Read the protocol frame from the input stream
        byte[] frameHeader = new byte[7]; // The frame header consists of 7 bytes
        bytesRead = inputStream.read(frameHeader);
        if (bytesRead != 7) {
            throw new IOException("Failed to read frame header");
        }
        // Interpret the frame type and channel number to determine how to handle the frame payload
        byte frameType = frameHeader[0];
        int channelNumber = ((frameHeader[1] & 0xFF) << 8) | (frameHeader[2] & 0xFF);
        int payloadSize = ((frameHeader[3] & 0xFF) << 24) | ((frameHeader[4] & 0xFF) << 16)
                | ((frameHeader[5] & 0xFF) << 8) | (frameHeader[6] & 0xFF);
        // TODO: Depending on the frame type, handle the frame payload appropriately
    }

    public static void sendFrame(Socket clientSocket, byte channel, byte frameType, byte[] payload) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        // Send the frame header
        byte[] frameHeader = new byte[7]; // The frame header consists of 7 bytes
        frameHeader[0] = frameType;
        frameHeader[1] = channel;
        frameHeader[2] = 0; // Channel number (always 0 in this example)
        frameHeader[3] = (byte) ((payload.length >> 24) & 0xFF); // Payload size (in bytes)
        frameHeader[4] = (byte) ((payload.length >> 16) & 0xFF);
        frameHeader[5] = (byte) ((payload.length >> 8) & 0xFF);
        frameHeader[6] = (byte) (payload.length & 0xFF);
        outputStream.write(frameHeader);
        // Send the frame payload
        outputStream.write(payload);
        outputStream.flush();
    }
}
