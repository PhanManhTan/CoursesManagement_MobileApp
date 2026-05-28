package com.example.myapplication.utils;

import com.example.myapplication.BuildConfig;

public class Constants {
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY;
    public static final String SUPABASE_STORAGE_BUCKET = BuildConfig.SUPABASE_STORAGE_BUCKET;

    // VNPay Sandbox Configuration
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_TMN_CODE = BuildConfig.VNP_TMN_CODE;
    public static final String VNP_HASH_SECRET = BuildConfig.VNP_HASH_SECRET;
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_RETURN_URL = "app://vnpay_return";
}
