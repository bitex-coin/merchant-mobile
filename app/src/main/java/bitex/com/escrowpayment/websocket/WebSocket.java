package bitex.com.escrowpayment.websocket;

/**
 * Created by cleba on 11/15/2017.
 */

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.apache.http.util.ByteArrayBuffer;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnFrame;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

public class WebSocket implements OnTextMessage, OnBinaryMessage, OnFrame {
    private static String BLANK_MESSAGE = "";
    private static String EVENT_ON_CLOSE = "onclose";
    private static String EVENT_ON_MESSAGE = "onmessage";
    private static String EVENT_ON_OPEN = "onopen";
    private final WebView appView;
    private boolean binary;
    private ByteArrayBuffer buffer;
    private WebSocketClient client;
    private Connection conn;
    private WebSocketClientFactory factory;
    private FrameConnection frame;
    private String id = null;
    private URI uri = null;

    public WebSocket(WebView appView, URI uri, String id) {
        this.id = id;
        this.uri = uri;
        this.appView = appView;
        try {
            this.factory = new WebSocketClientFactory();
            this.factory.start();
            this.client = this.factory.newWebSocketClient();
            this.client.setMaxTextMessageSize(100000);
            this.client.setMaxBinaryMessageSize(-1);
        } catch (Exception e) {
            Log.e("WebSocket", "WebSocket -> error: ", e);
        }
    }

    public void connect() {
        try {
            this.client.open(this.uri, this, 20000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e("WebSocket", "WebSocket -> connect error: ", e);
        }
    }

    public void stop() {
        Log.d("WebSocket", "WebSocket -> stop");
        try {
            release();
            if (this.conn != null) {
                if (this.conn.isOpen()) {
                    this.conn.close();
                }
                this.conn = null;
            }
            this.factory.stop();
        } catch (Exception e) {
            Log.d("WebSocket", "WebSocket -> stop error: ", e);
        }
    }

    @JavascriptInterface
    public String getId() {
        return this.id;
    }

    @JavascriptInterface
    public void send(String text) {
        Log.e("WebSocket", "WebSocket -> tx" + text);
        try {
            this.conn.sendMessage(text);
        } catch (IOException e) {
            Log.e("WebSocket", "WebSocket -> send error", e);
        }
    }

    private void release() {
        if (this.buffer != null) {
            this.buffer.clear();
            this.buffer = null;
        }
        if (this.frame != null) {
            if (this.frame.isOpen()) {
                this.frame.close();
            }
            this.frame = null;
        }
    }

    private String buildJavaScriptData(String event, String msg) {
        return "javascript:WebSocket." + event + "(" + "{" + "\"_target\":\"" + this.id + "\"," + "\"data\":'" + msg.replaceAll("'", "\\\\'") + "'" + "}" + ")";
    }

    public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
        if (this.frame.isBinary(opcode) || (this.frame.isContinuation(opcode) && this.binary)) {
            this.binary = true;
            this.buffer.append(data, offset, length);
            if (this.frame.isMessageComplete(flags)) {
                this.binary = false;
                onMessage(this.buffer.buffer(), 0, this.buffer.length());
                this.buffer.clear();
            }
            return true;
        } else if (!this.frame.isClose(opcode)) {
            return false;
        } else {
            release();
            return false;
        }
    }

    public void onOpen(Connection conn) {
        this.conn = conn;
        this.appView.post(new Runnable() {
            public void run() {
                WebSocket.this.appView.loadUrl(WebSocket.this.buildJavaScriptData(WebSocket.EVENT_ON_OPEN, WebSocket.BLANK_MESSAGE));
            }
        });
    }

    public void onClose(int code, String reason) {
        Log.d("WebSocket", "WebSocket -> onClose");
        this.appView.post(new Runnable() {
            public void run() {
                WebSocket.this.appView.loadUrl(WebSocket.this.buildJavaScriptData(WebSocket.EVENT_ON_CLOSE, WebSocket.BLANK_MESSAGE));
            }
        });
    }

    public void onMessage(byte[] data, int offset, int length) {
        Log.d("WebSocket", "WebSocket -> onMessage(byte[] data, int offset, int length)");
    }

    public void onMessage(final String data) {
        Log.e("WebSocket", "WebSocket -> rx" + data);
        this.appView.post(new Runnable() {
            public void run() {
                WebSocket.this.appView.loadUrl(WebSocket.this.buildJavaScriptData(WebSocket.EVENT_ON_MESSAGE, data));
            }
        });
    }

    public void onHandshake(FrameConnection connection) {
        this.frame = connection;
    }
}