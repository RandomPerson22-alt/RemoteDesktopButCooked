package com.randomperson22.rdbc.teavm;

import org.teavm.common.binary.Blob;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.websocket.WebSocket;
import org.teavm.jso.JSBody;

public class HtmlClient {

    private WebSocket socket;
    private HTMLImageElement img;

    private String lastUrl = null;

    public void connect() {

        HTMLDocument doc = Window.current().getDocument();

        doc.getBody().getStyle().setProperty("margin", "0");
        doc.getBody().getStyle().setProperty("overflow", "hidden");

        img = doc.createElement("img").cast();

        img.getStyle().setProperty("width", "100%");
        img.getStyle().setProperty("height", "100%");
        img.getStyle().setProperty("object-fit", "contain");
        img.getStyle().setProperty("display", "block");

        doc.getBody().appendChild(img);

        socket = WebSocket.create("wss://remotedesktopbutcooked-1.onrender.com");
        socket.setBinaryType("arraybuffer");

        socket.addEventListener("message", (MessageEvent evt) -> {
            Object data = evt.getData();

            if (data instanceof ArrayBuffer buffer) {
                render(buffer);
            }
        });
    }

    @JSBody(params = "data", script = "return new Blob([data], {type:'image/jpeg'});")
    public static native Blob createBlob(Uint8Array data);

    @JSBody(params = "blob", script = "return URL.createObjectURL(blob);")
    public static native String createObjectURL(Blob blob);

    @JSBody(params = "url", script = "URL.revokeObjectURL(url);")
    public static native void revokeObjectURL(String url);

    private void render(ArrayBuffer buffer) {

        Uint8Array bytes = Uint8Array.create(buffer);
        Blob blob = createBlob(bytes);

        String url = createObjectURL(blob);

        if (lastUrl != null) {
            revokeObjectURL(lastUrl);
        }

        lastUrl = url;

        img.setSrc(url);
    }
}