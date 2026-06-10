package com.randomperson22.rdbc.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class ServerLauncher {

    private static final AtomicReference<byte[]> latestFrame = new AtomicReference<>();

    public static void main(String[] args) {

        int port = 8080;

        WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {

            @Override
            public void onStart() {
                System.out.println("Server started on " + port);

                // broadcaster thread
                new Thread(() -> {
                    while (true) {
                        try {
                            byte[] frame = latestFrame.getAndSet(null);

                            if (frame != null) {
                                for (WebSocket c : getConnections()) {
                                    if (c.isOpen()) {
                                        c.send(frame);
                                    }
                                }
                            }

                            Thread.sleep(15);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("Client: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                byte[] data = new byte[message.remaining()];
                message.get(data);

                // overwrite old frame (prevents backlog explosion)
                latestFrame.set(data);
            }

            @Override public void onMessage(WebSocket conn, String message) {}

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Closed: " + reason);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }
        };

        server.start();
    }
}