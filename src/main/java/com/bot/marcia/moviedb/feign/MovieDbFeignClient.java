package com.bot.marcia.moviedb.feign;

import com.bot.marcia.dto.MovieDbMovie;
import com.bot.marcia.dto.MoviedbPopular;
import com.bot.marcia.moviedb.dto.RequestTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "moviedbservice", url = "https://api.themoviedb.org/")
public interface MovieDbFeignClient {
    @PostMapping(value = "/3/authentication/session/convert/4", consumes = MediaType.APPLICATION_JSON_VALUE)
    RequestTokenDTO createSession(String body);

    @PostMapping(value = "/4/auth/request_token", consumes = MediaType.APPLICATION_JSON_VALUE)
    RequestTokenDTO createRequestToken();

    @PostMapping(value = "/4/auth/access_token", consumes = MediaType.APPLICATION_JSON_VALUE)
    RequestTokenDTO createAccessToken(String body);

    @GetMapping(value = "/3/movie/{movie-id}")
    MovieDbMovie getMovie(@PathVariable(value = "movie-id") String movieId);

    @GetMapping(value = "/4/account/{account_object_id}/movie/recommendations")
    MoviedbPopular getRecommendMovies(@PathVariable(name = "account_object_id") String accountObjectId, @RequestParam(value = "page") String page);
}
