package com.randomperson22.rdbc;

import com.badlogic.gdx.ApplicationListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.Iterator;

public class Rdbc implements ApplicationListener {

    private WebSocketClient client;
    private Robot robot;
    private Rectangle rect;

    private volatile boolean running = true;

    public void start() {
        try {
            robot = new Robot();

            rect = new Rectangle(0, 0, 1280, 720);

            connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

                @Override public void onMessage(String message) {}
                @Override public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Closed: " + reason);
                }
                @Override public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            client.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] encodeJpeg(BufferedImage img, float quality) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        writer.write(null, new IIOImage(img, null, null), param);

        ios.close();
        writer.dispose();

        return baos.toByteArray();
    }

    private void startLoop() {

        Thread t = new Thread(() -> {

            while (running) {
                try {

                    if (client == null || !client.isOpen()) {
                        Thread.sleep(300);
                        continue;
                    }

                    BufferedImage frame = robot.createScreenCapture(rect);

                    byte[] data = encodeJpeg(frame, 0.55f);

                    client.send(data);

                    Thread.sleep(66); // ~15 FPS sweet spot

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    @Override
    public void create() {

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}