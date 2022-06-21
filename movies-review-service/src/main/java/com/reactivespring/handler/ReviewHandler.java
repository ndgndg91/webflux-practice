package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final Validator validator;
    private final ReviewReactiveRepository repository;

    Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(repository::save)
                .doOnNext(saved -> reviewsSink.tryEmitNext(saved))
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));

    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("ConstraintViolations : {}", constraintViolations);
        if (constraintViolations.size() > 0) {
        var errorMessage = constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        Flux<Review> reviewFlux;
        if (movieInfoId.isPresent()) {
            reviewFlux = repository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else {
            reviewFlux = repository.findAll();
        }
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        return repository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given id " + reviewId)))
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return reqReview;
                        })
                        .flatMap(repository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        return repository.findById(reviewId)
                .flatMap(review -> repository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
