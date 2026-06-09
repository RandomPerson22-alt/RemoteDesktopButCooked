package com.randomperson22.rdbc.teavm;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.websocket.WebSocket;

import java.util.Base64;

public class HtmlClient {

    private WebSocket socket;
    private HTMLImageElement img;

    private int remoteWidth;
    private int remoteHeight;

    public void connect() {

        HTMLDocument doc = Window.current().getDocument();

        img = doc.createElement("img").cast();

        img.getStyle().setProperty("width", "100%");
        img.getStyle().setProperty("height", "auto");

        doc.getBody().appendChild(img);

        socket = WebSocket.create(
                "wss://remotedesktopbutcooked-1.onrender.com"
        );

        socket.setBinaryType("arraybuffer");

        socket.addEventListener("open", evt -> {
            System.out.println("🌐 Viewer connected");
        });

        socket.addEventListener("message", evt -> {

            Object data =
                    ((MessageEvent) evt).getData();

            if (data instanceof String) {

                handlePacket((String)data);

            }
            else if (data instanceof ArrayBuffer) {

                render((ArrayBuffer)data);

            }
            else if (data instanceof Uint8Array) {

                render(
                        ((Uint8Array)data).getBuffer()
                );

            }
            else {

                System.out.println(
                        "Unknown WS type: "
                                + data
                );
            }
        });
    }

    private void handlePacket(String json) {

        System.out.println(
                "Received packet: "
                        + json
        );

        if(json.contains("\"type\":\"meta\"")) {

            remoteWidth =
                    extractInt(json, "width");

            remoteHeight =
                    extractInt(json, "height");

            System.out.println(
                    "Remote screen: "
                            + remoteWidth
                            + "x"
                            + remoteHeight
            );
        }
    }

    private int extractInt(
            String json,
            String key
    ) {

        String search =
                "\"" + key + "\":";

        int start =
                json.indexOf(search);

        if(start == -1)
            return 0;

        start += search.length();

        int end = start;

        while(end < json.length()
                && Character.isDigit(
                json.charAt(end)
        )) {

            end++;
        }

        return Integer.parseInt(
                json.substring(start, end)
        );
    }

    private void render(ArrayBuffer buffer) {

        Uint8Array bytes =
                Uint8Array.create(buffer);

        String base64 =
                toBase64(bytes);

        img.setSrc(
                "data:image/jpeg;base64,"
                        + base64
        );
    }

    private String toBase64(
            Uint8Array bytes
    ) {

        byte[] arr =
                new byte[bytes.getLength()];

        for(int i = 0; i < arr.length; i++) {

            arr[i] =
                    (byte)bytes.get(i);
        }

        return Base64.getEncoder()
                .encodeToString(arr);
    }
}