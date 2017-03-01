package org.cerberus.api.repository;

import org.cerberus.api.repository.extension.impl.SingleAttributeSearchingRepositoryExtension;
import org.cerberus.api.repository.extension.impl.SingleAttributeSearchingRepositoryExtensionImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

public class CerberusRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements CerberusRepository<T, ID> {

    private final SingleAttributeSearchingRepositoryExtension<T, ID> singleAttritueSearchingRepositoryExtension;

    public CerberusRepositoryImpl(final JpaEntityInformation<T, ?> entityInformation, final EntityManager entityManager) {
        super(entityInformation, entityManager);
        singleAttritueSearchingRepositoryExtension = new SingleAttributeSearchingRepositoryExtensionImpl<>(getDomainClass(), entityManager);
    }

    @Override
    public List<?> findSingleAttribute(final String attribute, final Specification<T> specification) {
        return singleAttritueSearchingRepositoryExtension.findSingleAttribute(attribute, specification);
    }

    @Override
    public Page<?> findSingleAttribute(final String attribute, final Specification<T> specification, final Pageable pageable) {
        return singleAttritueSearchingRepositoryExtension.findSingleAttribute(attribute, specification, pageable);
    }

}
