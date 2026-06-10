package com.randomperson22.rdbc.teavm;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.websocket.WebSocket;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.JSBody;

public class HtmlClient {

    private WebSocket socket;
    private HTMLImageElement img;

    public void connect() {

        HTMLDocument doc = Window.current().getDocument();

        doc.getBody().getStyle().setProperty("margin", "0");
        doc.getBody().getStyle().setProperty("overflow", "hidden");

        img = doc.createElement("img").cast();

        img.getStyle().setProperty("width", "100%");
        img.getStyle().setProperty("height", "100%");
        img.getStyle().setProperty("object-fit", "contain");

        doc.getBody().appendChild(img);

        socket = WebSocket.create(
                "wss://remotedesktopbutcooked-1.onrender.com"
        );

        socket.setBinaryType("arraybuffer");

        socket.addEventListener("message", (MessageEvent evt) -> {

            Object data = evt.getData();

            if (data instanceof ArrayBuffer buffer) {

                Uint8Array arr =
                        Uint8Array.create(buffer);

                render(buffer);
            }
        });
    }

    @JSBody(params = "bytes", script =
            "let binary = '';" +
                    "for (let i = 0; i < bytes.length; i++) {" +
                    "  binary += String.fromCharCode(bytes[i] & 0xff);" +
                    "}" +
                    "return btoa(binary);")
    private static native String toBase64(byte[] bytes);

    private void render(ArrayBuffer buffer) {

        Uint8Array bytes = Uint8Array.create(buffer);

        byte[] arr = new byte[bytes.getLength()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) bytes.get(i);
        }

        String base64 = toBase64(arr);

        img.setSrc("data:image/jpeg;base64," + base64);
    }
}