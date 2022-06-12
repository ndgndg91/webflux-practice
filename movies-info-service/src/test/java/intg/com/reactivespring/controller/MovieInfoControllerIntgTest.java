package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerIntgTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2085, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );
        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when - then
        webTestClient
                .post()
                .uri("/v1/movie-infos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(m -> {
                     var responseBody = m.getResponseBody();
                     assertNotNull(responseBody);
                     assertNotNull(responseBody.getMovieInfoId());
                });
    }

    @Test
    void getAllMovieInfos() {
        //given

        //when - then
        webTestClient
                .get()
                .uri("/v1/movie-infos")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        //given
        var id = "abc";

        //when - then
        webTestClient
                .get()
                .uri("/v1/movie-infos/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
//                .expectBody(MovieInfo.class)
//                .consumeWith(m -> {
//                    var responseBody = m.getResponseBody();
//                    assertNotNull(responseBody);
//                });
                .expectBody()
                .jsonPath("$.name" ).isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises2", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when - then
        webTestClient
                .put()
                .uri("/v1/movie-infos/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(m -> {
                    var responseBody = m.getResponseBody();
                    assertNotNull(responseBody);
                    assertNotNull(responseBody.getMovieInfoId());
                    assertEquals("Dark Knight Rises2", responseBody.getName());
                });
    }

    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";

        //when - then
        webTestClient
                .delete()
                .uri("/v1/movie-infos/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}