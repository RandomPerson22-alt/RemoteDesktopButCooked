package com.randomperson22.rdbc;

import com.badlogic.gdx.ApplicationAdapter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import com.google.gson.Gson;
import com.randomperson22.rdbc.net.Packets;

public class Rdbc extends ApplicationAdapter {

    private WebSocketClient client;

    private Robot robot;
    private Rectangle screenRect;
    private final Gson gson = new Gson();

    @Override
    public void create() {

        System.out.println("Desktop started 🚀");

        try {

            robot = new Robot();

            Dimension size =
                    Toolkit.getDefaultToolkit().getScreenSize();

            screenRect = new Rectangle(size);

            System.out.println(
                    "Capturing screen: "
                            + size.width
                            + "x"
                            + size.height
            );

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        connect();
    }

    private void connect() {

        try {

            client = new WebSocketClient(
                    new URI("wss://remotedesktopbutcooked-1.onrender.com")
            ) {

                @Override
                public void onOpen(ServerHandshake handshake) {

                    System.out.println("[WS] Connected!");

                    Packets.MetaPacket meta =
                            new Packets.MetaPacket(
                                    screenRect.width,
                                    screenRect.height
                            );

                    client.send(gson.toJson(meta));

                    System.out.println(
                            "Sent metadata: "
                                    + screenRect.width
                                    + "x"
                                    + screenRect.height
                    );

                    startCaptureLoop();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("[WS] Text: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[WS] Closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            client.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCaptureLoop() {

        Thread thread = new Thread(() -> {

            while (true) {

                try {

                    if (client == null || !client.isOpen()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    BufferedImage screenshot =
                            robot.createScreenCapture(screenRect);

                    ByteArrayOutputStream baos =
                            new ByteArrayOutputStream();

                    ImageIO.write(
                            screenshot,
                            "jpg",
                            baos
                    );

                    byte[] frame = baos.toByteArray();

                    System.out.println(
                            "Sending frame: "
                                    + (frame.length / 1024)
                                    + " KB"
                    );

                    client.send(frame);

                    Thread.sleep(200); // 5 FPS

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void dispose() {

        if (client != null) {
            client.close();
        }
    }
}