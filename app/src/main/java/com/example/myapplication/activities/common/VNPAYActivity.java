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
import com.example.myapplication.utils.LanguageManager;

public class VnPayActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_vnpay);

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

                // SỬA Ở ĐÂY: Chỉ cần URL chứa từ khóa kết quả của VNPay là xử lý luôn
                if (url.contains("vnp_ResponseCode") && url.contains("vnp_TransactionNo")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("FULL_RETURN_URL", url);

                    android.net.Uri uri = android.net.Uri.parse(url);
                    String txnNo = uri.getQueryParameter("vnp_TransactionNo");
                    if (txnNo != null) {
                        resultIntent.putExtra("vnp_TransactionNo", txnNo);
                    }

                    // Kiểm tra nếu mã thành công là 00
                    if (url.contains("vnp_ResponseCode=00") || url.contains("vnp_TransactionStatus=00")) {
                        setResult(Activity.RESULT_OK, resultIntent);
                    } else {
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                    }
                    finish();
                }
            }
        });

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            Toast.makeText(this, R.string.vnpay_url_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
