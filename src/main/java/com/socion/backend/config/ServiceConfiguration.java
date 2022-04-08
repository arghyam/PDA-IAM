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

import java.util.concurrent.TimeUnit;

@Configuration
public class ServiceConfiguration {

    @Autowired
    AppContext appContext;

    private OkHttpClient setClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.THIRTY_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.THIRTY_SECONDS_L, TimeUnit.SECONDS)
                .writeTimeout(Constants.THIRTY_SECONDS_L, TimeUnit.SECONDS);

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
/* This is a temporary change since we haven’t enabled SSL for the backend services. 
This change can be skipped if we have a valid hostname with a signed certificate for each backend service. */