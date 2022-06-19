package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewReactiveRepository repository;

    @BeforeEach
    void setUp() {
        var reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.5),
                new Review("updateId", 3L, "wow!", 9.5),
                new Review("deleteId", 3L, "Good Movie", 7.5)
        );
        repository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown(){
        repository.deleteAll().block();
    }

    @Test
    void addReview() {
        // given
        var review = new Review(null, 1L, "Good Movie", 9.0);

        // when - then
        webTestClient.post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(r -> {
                    var saved = r.getResponseBody();
                    assertThat(saved).isNotNull();
                    assertThat(saved.getReviewId()).isNotNull();
                });
    }

    @Test
    void deleteReview() {
        // given
        var id = "deletedId";


        // when - then
        webTestClient.delete()
                .uri("/v1/reviews/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {
        // given
        var movieInfoId = 2L;
        var uri = UriComponentsBuilder.fromUriString("/v1/reviews")
                .queryParam("movieInfoId", movieInfoId)
                .build().toUri();


        // when - then
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1)
                .consumeWith(r -> {
                    var body = r.getResponseBody();
                    assertThat(body).isNotNull();
                });
    }

    @Test
    void getReviews() {
        // given

        // when - then
        webTestClient.get()
                .uri("/v1/reviews")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(5)
                .consumeWith(r -> {
                    var body = r.getResponseBody();
                    assertThat(body).isNotNull();
                });
    }

    @Test
    void updateReview() {
        // given
        var id = "updateId";
        var review = new Review(id, 3L, "fucking good", 10.0);
        var uri = UriComponentsBuilder.fromUriString("/v1/reviews/{id}")
                .buildAndExpand(id).toUri();


        // when - then
        webTestClient.put()
                .uri(uri)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(r -> {
                    var body = r.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getComment()).isEqualTo(review.getComment());
                    assertThat(body.getRating()).isEqualTo(review.getRating());
                });
    }
}
