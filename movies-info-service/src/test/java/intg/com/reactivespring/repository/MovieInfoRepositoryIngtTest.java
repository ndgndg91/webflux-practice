package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIngtTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        //given

        //when
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        //given
        var id = "abc";

        //when
        var movieInfoMono = movieInfoRepository.findById(id).log();

        //given
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Doctor Strange in the Multiverse of Madness", 2022, List.of("Benedict Cumberbatch", "Elizabeth Olsen"), LocalDate.parse("2022-05-04"));

        //when
        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        //given
        StepVerifier.create(movieInfoMono)
                .assertNext(m -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Doctor Strange in the Multiverse of Madness", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var movieInfoMono = movieInfoRepository.findById(id).block();
        Objects.requireNonNull(movieInfoMono).setYear(2021);

        //when
        var updateMovieInfoMono = movieInfoRepository.save(movieInfoMono).log();

        //given
        StepVerifier.create(updateMovieInfoMono)
                .assertNext(m -> assertEquals(2021, m.getYear()))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";

        //when
        movieInfoRepository.deleteById(id).block();
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}