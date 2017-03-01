package org.cerberus.api.repository.extension.impl;

import org.cerberus.api.repository.extension.AbstractRepositoryExtension;
import org.cerberus.util.reflection.ReflectionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public class SingleAttributeSearchingRepositoryExtensionImpl<T, ID extends Serializable> extends AbstractRepositoryExtension<T, ID> implements SingleAttributeSearchingRepositoryExtension<T, ID> {

    public SingleAttributeSearchingRepositoryExtensionImpl(final Class<T> domainClass, final EntityManager entityManager) {
        super(domainClass, entityManager);
    }

    @Override
    public List<?> findSingleAttribute(final String attribute, final Specification<T> specification) {
        try {
            return createSingleAttributeBaseQuery(attribute, specification).getResultList();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<?> findSingleAttribute(final String attribute, final Specification<T> specification, final Pageable pageable) {
        try {
            final List<?> result = createSingleAttributeBaseQuery(attribute, specification)
                    .setFirstResult(pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
            return new PageImpl<>(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a base query for any single attribute searching query
     *
     * @param attribute     the domain class' attribute from which searching values
     * @param specification the {@link Specification} to apply to filter result
     * @return a pre-configured {@link TypedQuery} for any single attribute search query
     * @throws ReflectiveOperationException if an {@link ReflectiveOperationException} occurs when searching domain class' attribute
     */
    private TypedQuery<?> createSingleAttributeBaseQuery(final String attribute, final Specification<T> specification) throws ReflectiveOperationException {
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        @SuppressWarnings("rawtypes") final CriteriaQuery query = criteriaBuilder.createQuery(ReflectionUtil.getInstance().getAttributeType(getDomainClass(), attribute));
        final Root<T> root = query.from(getDomainClass());
        return getEntityManager()
                .createQuery(query
                        .select(root.get(attribute))
                        .distinct(true)
                        .where(specification.toPredicate(root, query, criteriaBuilder)));
    }

}
