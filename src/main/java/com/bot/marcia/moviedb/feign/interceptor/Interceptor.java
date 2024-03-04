package com.bot.marcia.moviedb.feign.interceptor;


import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Interceptor {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNmJjMGIxMzU0ODJjYjk0OTE3YTcxMWFlNGY0N2IxYSIsInN1YiI6IjY1ZTQ4NzdmOTk3OWQyMDE3Y2IyNjgyNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pEBoKzhJzpJq3-wcoWycoxCAYPfjliUHnypqtHTmLH0");
            requestTemplate.header("accept", ContentType.APPLICATION_JSON.getMimeType());
            requestTemplate.header("content-type", ContentType.APPLICATION_JSON.getMimeType());
        };
    }
}
