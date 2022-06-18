package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    private static final Logger log = LoggerFactory.getLogger(MoviesInfoControllerUnitTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    @Test
    void getAllMovieInfo() {
        // given
        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2085, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );
        given(movieInfoService.getAllMovieInfos()).willReturn(Flux.fromIterable(movieInfos));

        // when - then
        webTestClient.get()
                .uri("/v1/movie-infos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void addMovieInfo() {
        // given
        var movieInfo = new MovieInfo(null, "Batman begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        given(movieInfoService.addMovieInfo(isA(MovieInfo.class))).willReturn(
                Mono.just(new MovieInfo("123", "Batman begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
        );

        // when - then
        webTestClient.post()
                .uri("/v1/movie-infos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(r -> {
                    var saved = r.getResponseBody();
                    assertThat(saved).isNotNull();
                    assertThat(saved.getMovieInfoId()).isNotNull();
                    assertThat("123").isEqualTo(saved.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {
        // given
        var movieInfo = new MovieInfo(null, "", -2005, List.of(""), LocalDate.parse("2005-06-15"));

        // when - then
        webTestClient.post()
                .uri("/v1/movie-infos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(s -> {
                    var body = s.getResponseBody();
                    log.info(body);
                    assertThat(body).isNotNull();
                });
    }

    @Test
    void updateMovieInfo() {
        // given
        var id = "abc";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-16"));
        given(movieInfoService.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).willReturn(
                Mono.just(new MovieInfo(
                       id, "Dark Knight Rises1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")
                ))
        );


        // when - then
        webTestClient.put()
                .uri("/v1/movie-infos/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(r -> {
                    var updated = r.getResponseBody();
                    assertThat(updated).isNotNull();
                    assertThat(updated.getMovieInfoId()).isNotNull();
                    assertThat("Dark Knight Rises1").isEqualTo(updated.getName());
                });
    }
}
