package com.randomperson22.rdbc;

import com.badlogic.gdx.ApplicationAdapter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;

public class Rdbc extends ApplicationAdapter {

    private WebSocketClient client;
    private Robot robot;
    private Rectangle rect;

    @Override
    public void create() {

        System.out.println("Desktop started");

        try {
            robot = new Robot();

            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

            rect = new Rectangle(
                    0,
                    0,
                    1280,
                    720
            );

            System.out.println("Capture size: 1280x720");

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
                    System.out.println("WS connected");
                    startLoop();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("WS text: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WS closed: " + code + " " + reason);
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

    private void startLoop() {

        Thread t = new Thread(() -> {

            while (true) {

                try {

                    if (client == null || !client.isOpen()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    BufferedImage img =
                            robot.createScreenCapture(rect);

                    ByteArrayOutputStream baos =
                            new ByteArrayOutputStream();

                    ImageIO.write(img, "jpg", baos);

                    byte[] data = baos.toByteArray();

                    client.send(data);

                    Thread.sleep(100); // ~10 FPS

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(false);
        t.start();
    }

    @Override
    public void dispose() {
        if (client != null) client.close();
    }
}