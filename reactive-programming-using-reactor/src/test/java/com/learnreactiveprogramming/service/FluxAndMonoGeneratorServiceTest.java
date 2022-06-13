package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();


    @Test
    void namesFlux() {
        // given

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        // then
        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNext("alex")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFluxMap() {
        //given

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxMap();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFluxMapImmutability() {
        //given

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxMapImmutability();

        //then
        StepVerifier.create(namesFlux)
//                .expectNext("ALEX", "BEN", "CHLOE")
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void testNamesFluxMap() {
        //given
        var stringLength = 3;

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMap() {
        //given
        var stringLength = 3;

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","C","H","L","O","E")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMapAsync() {
        //given
        var stringLength = 3;

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLength);

        //then
        StepVerifier.create(namesFlux)
//                .expectNext("A","L","E","X","C","H","L","O","E")
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFluxConcatMap() {
        //given
        var stringLength = 3;

        //when
        var namesFlux = fluxAndMonoGeneratorService.namesFluxConcatMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","C","H","L","O","E")
//                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMap() {
        int stringLength = 3;

        var value = fluxAndMonoGeneratorService.namesMonoFlatMap(stringLength);

        StepVerifier.create(value)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMapMany() {
        // given
        var stringLength = 3;

        // when
        var value = fluxAndMonoGeneratorService.namesMonoFlatMapMany(stringLength);

        // then
        StepVerifier.create(value)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        // given
        var stringLength = 3;

        // when
        var value = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);

        // then
        StepVerifier.create(value)
                .expectNext("A","L","E","X","C","H","L","O","E")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform2() {
        // given
        var stringLength = 10;

        // when
        var value = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);

        // then
        StepVerifier.create(value)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformSwitchIfEmpty() {
        // given
        var stringLength = 6;

        // when
        var value = fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(stringLength);

        // then
        StepVerifier.create(value)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }

    @Test
    void exploreConcat() {
        // given

        // when
        var value = fluxAndMonoGeneratorService.exploreConcat();

        // then
        StepVerifier.create(value)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreConcatWith() {
        // given

        // when
        var value = fluxAndMonoGeneratorService.exploreConcatWith();

        // then
        StepVerifier.create(value)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreMonoConcatWith() {
        // given

        // when
        var value = fluxAndMonoGeneratorService.exploreMonoConcatWith();

        // then
        StepVerifier.create(value)
                .expectNext("A", "B")
                .verifyComplete();
    }
}