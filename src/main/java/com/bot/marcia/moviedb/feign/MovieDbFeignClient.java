package com.bot.marcia.moviedb.feign;

import com.bot.marcia.dto.MovieDbMovie;
import com.bot.marcia.dto.MoviedbPopular;
import com.bot.marcia.moviedb.dto.RequestTokenDTO;
import com.bot.marcia.moviedb.dto.ResponseDTO;
import com.bot.marcia.moviedb.dto.UpdateListRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/3/account/{account_id}/{list_id}")
    ResponseDTO addToList(@PathVariable(name = "list_id") String listId, @PathVariable(name = "account_id") Integer accountId,
                          @RequestParam(value = "session_id") String sessionId, @RequestBody UpdateListRequestDTO body);

    @GetMapping(value = "/3/account/{account_id}/{list}/movies")
    MoviedbPopular getList(@PathVariable(name = "list") String list, @PathVariable(name = "account_id") Integer accountId,
                           @RequestParam(value = "session_id") String sessionId, @RequestParam(value = "page") String page, @RequestParam(value = "sort_by") String sort);
}
