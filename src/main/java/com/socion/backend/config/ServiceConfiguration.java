package com.socion.backend.config;

import com.socion.backend.dao.KeycloakDao;
import com.socion.backend.dao.RegistryDao;
import com.socion.backend.utils.Constants;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.concurrent.TimeUnit;

@Configuration
public class ServiceConfiguration {

    @Autowired
    AppContext appContext;

    private OkHttpClient setClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.THIRTY_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.THIRTY_SECONDS_L, TimeUnit.SECONDS)
                .writeTimeout(Constants.THIRTY_SECONDS_L, TimeUnit.SECONDS)
		.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        //TODO: Make this more restrictive
                        return true;
                    }
                });

        return httpClient.build();


    }

    @Bean
    public KeycloakDao initKeyCloak() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(setClient())
                .baseUrl(appContext.getKeyCloakServiceUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(KeycloakDao.class);

    }

    @Bean
    public RegistryDao initRegistry() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(setClient())
                .baseUrl(appContext.getRegistryBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(RegistryDao.class);
    }


}
