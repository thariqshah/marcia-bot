package com.bot.marcia.moviedb;

import com.bot.marcia.dto.MovieDbAccountInfo;
import com.bot.marcia.dto.MovieDbMovie;
import com.bot.marcia.dto.MoviedbPopular;
import com.bot.marcia.moviedb.dto.RequestTokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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


    public MovieDbAccountInfo getAccountId(String sessionId) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://api.themoviedb.org/3/account?api_key=06bc0b135482cb94917a711ae4f47b1a&session_id=%s".formatted(sessionId))
                .build().get().uri(UriBuilder::build)
                .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNmJjMGIxMzU0ODJjYjk0OTE3YTcxMWFlNGY0N2IxYSIsInN1YiI6IjY1ZTQ4NzdmOTk3OWQyMDE3Y2IyNjgyNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pEBoKzhJzpJq3-wcoWycoxCAYPfjliUHnypqtHTmLH0")
                .header("content-type","application/json").retrieve();
        return client.bodyToMono(MovieDbAccountInfo.class).block();
    }

    public MoviedbPopular getWatchList(Integer accountId,String sessionId,String page) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://api.themoviedb.org/3/account/%d/watchlist/movies?language=en-US&sort_by=created_at.desc".formatted(accountId))
                .build().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("session_id", sessionId)
                        .queryParam("page", page).build())
                .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNmJjMGIxMzU0ODJjYjk0OTE3YTcxMWFlNGY0N2IxYSIsInN1YiI6IjY1ZTQ4NzdmOTk3OWQyMDE3Y2IyNjgyNCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pEBoKzhJzpJq3-wcoWycoxCAYPfjliUHnypqtHTmLH0")
                .header("content-type","application/json").retrieve();
        return client.bodyToMono(MoviedbPopular.class).block();
    }
}
