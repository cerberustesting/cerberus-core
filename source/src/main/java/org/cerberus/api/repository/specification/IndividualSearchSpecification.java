package org.cerberus.api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

/**
 * A {@link Specification} that restrict result values according to an expected key-value map
 * <p>
 * Each key represents the domain class' attribute name
 * Each value represents a list of allowed values
 *
 * @param <T> the underlying domain class type
 * @author abourdon
 */
public class IndividualSearchSpecification<T> implements Specification<T> {

    private final Map<String, List<?>> individualSearch;

    /**
     * Create a new {@link IndividualSearchSpecification} based on the given key-value map restriction
     *
     * @param individualSearch the key-value map restriction
     */
    public IndividualSearchSpecification(final Map<String, List<?>> individualSearch) {
        this.individualSearch = individualSearch;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.and();
        for (final Map.Entry<String, List<?>> criteria : individualSearch.entrySet()) {
            predicate = criteriaBuilder.and(
                    predicate,
                    root.get(criteria.getKey()).in(criteria.getValue())
            );
        }
        return predicate;
    }

}
