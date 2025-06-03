/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.samples.petclinic.api.application.CustomersServiceClient;
import org.springframework.samples.petclinic.api.application.VisitsServiceClient;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

// Import các thư viện logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maciej Szarlinski
 */
@RestController
@RequestMapping("/api/gateway")
public class ApiGatewayController {

    // Khởi tạo logger cho class này
    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayController.class);

    private final CustomersServiceClient customersServiceClient;

    private final VisitsServiceClient visitsServiceClient;

    private final ReactiveCircuitBreakerFactory cbFactory;

    public ApiGatewayController(CustomersServiceClient customersServiceClient,
                                VisitsServiceClient visitsServiceClient,
                                ReactiveCircuitBreakerFactory cbFactory) {
        this.customersServiceClient = customersServiceClient;
        this.visitsServiceClient = visitsServiceClient;
        this.cbFactory = cbFactory;
    }

    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        // Ghi log khi bắt đầu xử lý yêu cầu getOwnerDetails
        LOG.info("Request received for owner details with ID: {}", ownerId);

        return customersServiceClient.getOwner(ownerId)
            .flatMap(owner ->
                visitsServiceClient.getVisitsForPets(owner.getPetIds())
                    .transform(it -> {
                        ReactiveCircuitBreaker cb = cbFactory.create("getOwnerDetails");
                        return cb.run(it, throwable -> {
                            // Ghi log khi circuit breaker fallback xảy ra
                            LOG.warn("Circuit breaker fallback for getOwnerDetails. Error: {}", throwable.getMessage());
                            return emptyVisitsForPets();
                        });
                    })
                    .map(addVisitsToOwner(owner))
            )
            .doOnSuccess(ownerDetails -> LOG.info("Successfully fetched owner details for ID: {}", ownerDetails.getId()))
            .doOnError(throwable -> LOG.error("Error fetching owner details: {}", throwable.getMessage()));
    }

    /**
     * Endpoint mới để kiểm tra logs và traceId/spanId.
     * Khi truy cập endpoint này, một dòng log sẽ được ghi.
     */
    @GetMapping(value = "/test-log")
    public Mono<String> testLogEndpoint() {
        // Ghi một dòng log đơn giản.
        // TraceId và SpanId sẽ tự động được thêm vào dòng log này nhờ cấu hình Logback và Micrometer Tracing.
        LOG.info("Test log endpoint hit. Checking if traceId and spanId are present.");
        return Mono.just("Log message sent. Check your API Gateway logs for traceId and spanId!");
    }


    private Function<Visits, OwnerDetails> addVisitsToOwner(OwnerDetails owner) {
        return visits -> {
            owner.pets()
                .forEach(pet -> pet.visits()
                    .addAll(visits.items().stream()
                        .filter(v -> v.petId() == pet.id())
                        .toList())
                );
            return owner;
        };
    }

    private Mono<Visits> emptyVisitsForPets() {
        return Mono.just(new Visits(List.of()));
    }
}
