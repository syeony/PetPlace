package kr.co.skeleton.network;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import kr.co.skeleton.common.Constant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitManager {

    private static Retrofit mRetrofit = null;
    public static RetrofitService getService() {

        if (mRetrofit == null) {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(Constant.HOST_ADDRESS)
                    .client(getUnsafeOkHttpClient().build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
        }
        return mRetrofit.create(RetrofitService.class);
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setLenient()
                .disableHtmlEscaping()
                .create();
    }

    private static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {

            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.addInterceptor(chain -> {

                Request newRequest  = chain.request().newBuilder()
                        .addHeader("X-AUTH-TOKEN", Constant.AUTH_TOKEN)
                        .addHeader("Content-Type","application/json")
                        .build();
                return chain.proceed(newRequest);
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
