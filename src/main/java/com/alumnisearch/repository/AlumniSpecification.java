package com.alumnisearch.repository;

import com.alumnisearch.dto.request.AlumniFilterCriteria;
import com.alumnisearch.entity.AlumniProfile;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AlumniSpecification {

    public static Specification<AlumniProfile> withCriteria(AlumniFilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            if (criteria == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getUniversity() != null && !criteria.getUniversity().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("university")),
                        "%" + criteria.getUniversity().trim().toLowerCase() + "%"
                ));
            }

            if (criteria.getDesignation() != null && !criteria.getDesignation().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("currentRole")),
                        "%" + criteria.getDesignation().trim().toLowerCase() + "%"
                ));
            }

            if (criteria.getPassoutYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("passoutYear"), criteria.getPassoutYear()));
            }

            if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + criteria.getLocation().trim().toLowerCase() + "%"
                ));
            }

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
