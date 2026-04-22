package com.example.dt7syntaxcheck.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Thread lắng nghe broadcast UDP trên port {@value #BROADCAST_PORT}.
 *
 * <p>
 * Khi nhận được tin nhắn "DISCOVER_SERVER" từ bất kỳ client nào trên LAN,
 * listener phản hồi "SERVER_HERE" về đúng địa chỉ và port của client đó.
 *
 * <p>
 * <b>Cách khởi động từ lớp Server chính:</b>
 * 
 * <pre>{@code
 * new BroadcastListener().start();
 * }</pre>
 */
public class BroadcastListener extends Thread {

    /** Port lắng nghe broadcast – client phải broadcast đúng port này */
    private static final int BROADCAST_PORT = 4999;

    private static final String DISCOVER_MSG = "DISCOVER_SERVER";
    private static final String REPLY_MSG = "SERVER_HERE";

    public BroadcastListener() {
        setName("BroadcastListener");
        setDaemon(true); // tự kết thúc khi JVM thoát
    }

    @Override
    public void run() {
        System.out.println("[BROADCAST] Đang lắng nghe discovery trên UDP port " + BROADCAST_PORT + "...");

        try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT)) {
            socket.setBroadcast(true);

            byte[] buf = new byte[256];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket inPkt = new DatagramPacket(buf, buf.length);
                socket.receive(inPkt); // chặn cho đến khi có packet đến

                String msg = new String(inPkt.getData(), 0, inPkt.getLength(), StandardCharsets.UTF_8);
                InetAddress clientAddr = inPkt.getAddress();
                int clientPort = inPkt.getPort();

                if (DISCOVER_MSG.equals(msg)) {
                    System.out.println("[BROADCAST] ✓ Nhận DISCOVER từ "
                            + clientAddr.getHostAddress() + ":" + clientPort
                            + " – đang phản hồi...");

                    byte[] reply = REPLY_MSG.getBytes(StandardCharsets.UTF_8);
                    socket.send(new DatagramPacket(reply, reply.length, clientAddr, clientPort));
                } else {
                    System.out.println("[BROADCAST] Bỏ qua tin nhắn không xác định: " + msg);
                }
            }
        } catch (Exception e) {
            System.err.println("[BROADCAST] Lỗi: " + e.getMessage());
        }
    }
}
