package uber.com.uber.Retrofit;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uber.com.uber.utils.AppConstants;

/**
 * Created by SAKSHI on 1/18/2018.
 */

public class RetrofitClient {

    public static Retrofit getClient(Context context){


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
        httpClient.addInterceptor(logging);

        httpClient.connectTimeout(10, TimeUnit.SECONDS);
        httpClient.readTimeout(30,TimeUnit.SECONDS);
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(AppConstants.baseURl)
                        .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build());



        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit;
    }



    public static Retrofit retrofitService() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.baseURl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit;
    }

}
