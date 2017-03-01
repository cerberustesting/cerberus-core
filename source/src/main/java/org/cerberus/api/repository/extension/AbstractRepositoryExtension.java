package org.cerberus.api.repository.extension;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Base class for any {@link RepositoryExtension}
 *
 * @param <T>  the underlying domain class type
 * @param <ID> the underlying domain class' identifier type
 * @author abourdon
 */
public abstract class AbstractRepositoryExtension<T, ID extends Serializable> implements RepositoryExtension<T, ID> {

    private final Class<T> domainClass;

    private final EntityManager entityManager;

    public AbstractRepositoryExtension(final Class<T> domainClass, final EntityManager entityManager) {
        this.domainClass = domainClass;
        this.entityManager = entityManager;
    }

    public Class<T> getDomainClass() {
        return domainClass;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
