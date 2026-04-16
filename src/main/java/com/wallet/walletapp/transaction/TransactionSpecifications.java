package com.wallet.walletapp.transaction;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> byFilters(
            UUID tenantId,
            @Nullable UUID walletId,
            @Nullable TransactionType type,
            @Nullable LocalDateTime dateFrom,
            @Nullable LocalDateTime dateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (walletId != null) {
                predicates.add(criteriaBuilder.equal(root.get("walletId"), walletId));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("occurredAt"), dateTo));
            }

            query.orderBy(
                    criteriaBuilder.desc(root.get("occurredAt")),
                    criteriaBuilder.desc(root.get("createdAt"))
            );
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
