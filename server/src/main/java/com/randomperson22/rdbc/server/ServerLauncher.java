package com.randomperson22.rdbc.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ServerLauncher {

    public static void main(String[] args) {

        String portEnv = System.getenv("PORT");
        StatusLineManager status = new StatusLineManager();

        int port;
        try {
            port = (portEnv != null) ? Integer.parseInt(portEnv) : 8080;
        } catch (Exception e) {
            port = 8080;
        }

        WebSocketServer server = new WebSocketServer(
                new InetSocketAddress("0.0.0.0", port)) {

            @Override
            public void onStart() {
                status.newLine("🚀 Server started on port: " + getPort());
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                status.newLine("🔌 Connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {

                status.newLine("📩 Text: " + message);

                for (WebSocket client : getConnections()) {

                    if (client != conn && client.isOpen()) {
                        client.send(message);
                    }
                }
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {

                int size = message.remaining();

                byte[] data = new byte[size];
                message.get(data);

                for (WebSocket client : getConnections()) {
                    if (client != conn && client.isOpen()) {
                        client.send(data);
                    }
                }

                status.newLine(
                        "📦 Binary message received (" +
                                size +
                                " bytes), forwarded to " +
                                Math.max(0, getConnections().size() - 1) +
                                " clients"
                );
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                status.newLine("❌ Disconnected: " + reason);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                status.newLine("💥 Error: " + ex.getMessage());
            }
        };

        server.start();
        System.out.println("🟢 Booting...");
    }
}