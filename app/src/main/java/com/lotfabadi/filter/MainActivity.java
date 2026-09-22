package com.lotfabadi.filter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {

    private static final int FILE_CHOOSER = 1001;
    private static final int CREATE_BACKUP = 1002;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private String pendingBackupData;
    private String pendingBackupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(true);

        CookieManager.getInstance().setAcceptCookie(true);

        WebViewAssetLoader loader =
                new WebViewAssetLoader.Builder()
                        .addPathHandler(
                                "/assets/",
                                new WebViewAssetLoader.AssetsPathHandler(this)
                        )
                        .build();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    WebResourceRequest request
            ) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                String url = request.getUrl().toString();

                if (url.startsWith("tel:")
                        || url.startsWith("sms:")
                        || url.startsWith("mailto:")
                        || url.startsWith("whatsapp:")) {

                    try {
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        request.getUrl()
                                )
                        );
                    } catch (ActivityNotFoundException ignored) {
                    }

                    return true;
                }

                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params
            ) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }

                filePathCallback = callback;

                Intent intent =
                        new Intent(Intent.ACTION_OPEN_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");

                try {
                    startActivityForResult(
                            intent,
                            FILE_CHOOSER
                    );
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    return false;
                }

                return true;
            }
        });

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "Android"
        );

        webView.loadUrl(
                "https://appassets.androidplatform.net/assets/index.html"
        );
    }

    public class AndroidBridge {

        @android.webkit.JavascriptInterface
        public void saveBackup(
                String json,
                String filename
        ) {

            pendingBackupData = json;
            pendingBackupName = filename;

            runOnUiThread(() -> {

                Intent intent =
                        new Intent(Intent.ACTION_CREATE_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");

                intent.putExtra(
                        Intent.EXTRA_TITLE,
                        filename
                );

                try {
                    startActivityForResult(
                            intent,
                            CREATE_BACKUP
                    );
                } catch (ActivityNotFoundException e) {

                    Toast.makeText(
                            MainActivity.this,
                            "امکان ذخیره فایل پیدا نشد",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == FILE_CHOOSER) {

            if (filePathCallback == null) {
                return;
            }

            Uri[] results = null;

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                results = new Uri[]{
                        data.getData()
                };
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;

        } else if (requestCode == CREATE_BACKUP) {

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null
                    && pendingBackupData != null) {

                try {

                    java.io.OutputStream output =
                            getContentResolver()
                                    .openOutputStream(
                                            data.getData()
                                    );

                    output.write(
                            pendingBackupData.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

                    output.close();

                    Toast.makeText(
                            this,
                            "بکاپ با موفقیت ذخیره شد",
                            Toast.LENGTH_SHORT
                    ).show();

                } catch (Exception e) {

                    Toast.makeText(
                            this,
                            "خطا در ذخیره بکاپ",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            pendingBackupData = null;
            pendingBackupName = null;
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
