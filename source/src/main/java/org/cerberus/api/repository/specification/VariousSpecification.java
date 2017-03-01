package org.cerberus.api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

/**
 * A {@link Specification} that combines {@link IndividualSearchSpecification} and {@link GlobalSearchSpecification}
 *
 * @param <T> the underlying domain class type
 * @author abourdon
 */
public class VariousSpecification<T> implements Specification<T> {

    private final Map<String, List<?>> individualSearch;

    private final String globalSearch;

    /**
     * Create a new {@link VariousSpecification} based on the mandatory parameters from the {@link IndividualSearchSpecification} and {@link GlobalSearchSpecification}
     *
     * @param individualSearch the {@link IndividualSearchSpecification}'s key-value map restriction
     * @param globalSearch     the {@link GlobalSearchSpecification}'s global search text value
     */
    public VariousSpecification(final Map<String, List<?>> individualSearch, final String globalSearch) {
        this.individualSearch = individualSearch;
        this.globalSearch = globalSearch;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(
                new IndividualSearchSpecification(individualSearch).toPredicate(root, criteriaQuery, criteriaBuilder),
                new GlobalSearchSpecification(globalSearch).toPredicate(root, criteriaQuery, criteriaBuilder)
        );
    }

}
