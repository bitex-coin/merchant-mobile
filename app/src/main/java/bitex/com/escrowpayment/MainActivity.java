package bitex.com.escrowpayment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.*;

import bitex.com.escrowpayment.websocket.WebSocketFactory;

public class MainActivity extends Activity {

    WebSocketFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView wv = (WebView) findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("Browser => ", consoleMessage.message());
                return true;
            }
        });
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        wv.loadUrl("file:///android_asset/www/escrow.html");
        this.factory = new WebSocketFactory(wv);
        wv.addJavascriptInterface(this.factory, "WebSocketFactory");
    }


    public void onStop() {
        super.onStop();
        Log.d("WebSocket", "WebSocket -> onStop");
        this.factory.stop();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("WebSocket", "WebSocket -> onDestroy");
    }


}
