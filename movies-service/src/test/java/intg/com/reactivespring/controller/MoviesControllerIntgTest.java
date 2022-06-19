package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) // spin up a http server in port 8084
@TestPropertySource(
        properties = {
                "rest-client.movie-infos-url=http://127.0.0.1:8084/v1/movie-infos",
                "rest-client.reviews-url=http://127.0.0.1:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-infos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));


        // when - then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(r -> {
                    var movie = r.getResponseBody();
                    assertThat(movie).isNotNull();
                    assertThat(movie.getMovieInfo().getName()).isEqualTo("Batman Begins");
                    assertThat(movie.getReviewList().size()).isEqualTo(2);
                });
    }

    @Test
    void retrieveMovieById_404() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-infos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));


        // when - then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError();

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movie-infos" + "/" + movieId)));
    }

    @Test
    void retrieveMovieById_revies_404() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-infos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));


        // when - then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(r -> {
                    var movie = r.getResponseBody();
                    assertThat(movie).isNotNull();
                    assertThat(movie.getMovieInfo().getName()).isEqualTo("Batman Begins");
                    assertThat(movie.getReviewList().size()).isEqualTo(0);
                });
    }

    @Test
    void retrieveMovieById_5xx() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-infos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")
                ));

        /*stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));*/


        // when - then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movie-infos" + "/" + movieId)));
    }

    @Test
    void retrieveMovieById_reviews_5xx() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-infos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Not Available")
                ));


        // when - then
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewService Review Service Not Available");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}