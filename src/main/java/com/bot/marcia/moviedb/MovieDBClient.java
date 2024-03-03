package com.bot.marcia.moviedb;

import com.bot.marcia.dto.MoviedbPopular;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@Service
public class MovieDBClient {

    public MoviedbPopular getPopularMovies(int pagenumber) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=%d&sort_by=popularity.desc"
                        .formatted(pagenumber))
                .build().get().uri(UriBuilder::build).header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNmJjMGIxMzU0ODJjYjk0OTE3YTcxMWFlNGY0N2IxYSIsInN1YiI6IjY1ZTQ4NzdmOTk3OWQyMDE3Y2IyNjgyNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pEBoKzhJzpJq3-wcoWycoxCAYPfjliUHnypqtHTmLH0").retrieve();
        return client.bodyToMono(MoviedbPopular.class).block();
    }

    public MoviedbPopular searchAMovie(String keyword) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://api.themoviedb.org/3/search/movie?query=%s&include_adult=false&language=en-US&page=1"
                        .formatted(keyword))
                .build().get().uri(UriBuilder::build)
                .header("Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNmJjMGIxMzU0ODJjYjk0OTE3YTcxMWFlNGY0N2IxYSIsInN1YiI6IjY1ZTQ4NzdmOTk3OWQyMDE3Y2IyNjgyNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pEBoKzhJzpJq3-wcoWycoxCAYPfjliUHnypqtHTmLH0").retrieve();
        return client.bodyToMono(MoviedbPopular.class).block();
    }
}
