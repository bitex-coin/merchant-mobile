package bitex.com.escrowpayment.websocket;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.net.URI;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class WebSocketFactory {
    WebView appView;
    private Vector<WebSocket> socketList = new Vector();

    class AnonymousClass1StopSocket implements Runnable {
        WebSocket socket;

        AnonymousClass1StopSocket(WebSocket s) {
            this.socket = s;
        }

        public void run() {
            this.socket.connect();
        }
    }

    class AnonymousClass2StopSocket implements Runnable {
        WebSocket socket;

        AnonymousClass2StopSocket(WebSocket s) {
            this.socket = s;
        }

        public void run() {
            this.socket.stop();
        }
    }

    public WebSocketFactory(WebView appView) {
        this.appView = appView;
    }

    @JavascriptInterface
    public WebSocket getInstance(String url) {
        Exception e;
        Log.d("WebSocketFactory", String.format("WebSocketFactory->getInstance: %s", url));
        WebSocket socket = null;
        try {
            WebSocket socket2 = new WebSocket(this.appView, new URI(url), getRandonUniqueId());
            try {
                this.socketList.add(socket2);
                safeStartSocket(socket2);
                socket = socket2;
                return socket2;
            } catch (Exception e2) {
                e = e2;
                socket = socket2;
                Log.v("WebSocketFactory", String.format("WebSocketFactory->getInstance error: %s", e.toString()));
                if (socket != null) {
                    safeStopSocket(socket);
                }
                return null;
            }
        } catch (Exception e3) {
            e = e3;
            Log.v("WebSocketFactory", String.format("WebSocketFactory->getInstance error: %s", e.toString()));
            if (socket != null) {
                safeStopSocket(socket);
            }
            return null;
        }
    }

    private void safeStartSocket(WebSocket socket) {
        new Thread(new AnonymousClass1StopSocket(socket)).start();
    }

    private void safeStopSocket(WebSocket socket) {
        new Thread(new AnonymousClass2StopSocket(socket)).start();
    }

    public void stop() {
        Iterator it = this.socketList.iterator();
        while (it.hasNext()) {
            safeStopSocket((WebSocket) it.next());
        }
        this.socketList.clear();
    }

    private String getRandonUniqueId() {
        return "WEBSOCKET." + new Random().nextInt(100);
    }
}