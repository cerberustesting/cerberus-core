package org.cerberus.api.repository.specification;

import org.cerberus.util.reflection.ReflectionUtil;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * A {@link Specification} that search a text on all domain class' attributes
 *
 * @param <T> the underlying domain class type
 * @author abourdon
 */
public class GlobalSearchSpecification<T> implements Specification<T> {

    private static final String CONTAINS_LIKE_FORMAT = "%%%s%%";

    private final String globalSearch;

    /**
     * Create a new {@link GlobalSearchSpecification} based on the given text to search
     *
     * @param globalSearch the text to search from the underlying domain class' attributes
     */
    public GlobalSearchSpecification(final String globalSearch) {
        this.globalSearch = globalSearch;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.not(criteriaBuilder.and());
        for (final String entityAttribute : ReflectionUtil.getInstance().getSimplyAttributes(root, String.class)) {
            predicate = criteriaBuilder.or(
                    predicate,
                    criteriaBuilder.like(
                            criteriaBuilder.upper(root.<String>get(entityAttribute)),
                            String.format(CONTAINS_LIKE_FORMAT, globalSearch.toUpperCase())
                    )
            );
        }
        return predicate;
    }

}
