package eu.imind.imindlib;

import android.app.Application;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS;

public class IMindLibApplication extends Application {

    private static final int MEDIA_CACHE_SIZE = 24 * 1024 * 1024;

    private OkHttpClient mHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initHttpClient();
    }

    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    private void initHttpClient() {
        HttpLoggingInterceptor li = new HttpLoggingInterceptor();
        li.setLevel(HEADERS);

        MediaCacheInterceptor ci = new MediaCacheInterceptor();

        mHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(getCacheDir(), MEDIA_CACHE_SIZE))
                .addInterceptor(li)
                .addInterceptor(ci)
                .build();
    }

    private static class MediaCacheInterceptor implements Interceptor {

        MediaCacheInterceptor() { }

        @Override
        public Response intercept(Chain chain) throws IOException {
            try {
                return chain.proceed(chain.request());
            } catch (Exception e) {
                Request offlineRequest = chain.request().newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                return chain.proceed(offlineRequest);
            }
        }

    }

}
