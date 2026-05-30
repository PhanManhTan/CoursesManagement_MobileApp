package com.example.myapplication.activities.common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.VNPayUtils;

public class VNPAYActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay);

        webView = findViewById(R.id.webViewVNPAY);
        progressBar = findViewById(R.id.progressBar);

        String paymentUrl = getIntent().getStringExtra("VNPAY_URL");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);

                // Nếu URL bắt đầu bằng ReturnUrl ảo của chúng ta
                if (url.startsWith(VNPayUtils.vnp_ReturnUrl)) {
                    Intent resultIntent = new Intent();
                    if (url.contains("vnp_ResponseCode=00")) {
                        setResult(Activity.RESULT_OK, resultIntent);
                    } else {
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                    }
                    finish(); // Đóng WebView, trở về Checkout
                }
            }
        });

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            Toast.makeText(this, "Lỗi tạo URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}