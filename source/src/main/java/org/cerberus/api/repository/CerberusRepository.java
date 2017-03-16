package org.cerberus.api.repository;

import org.cerberus.api.repository.extension.impl.SingleAttributeSearchingRepositoryExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * The base type for any Cerberus {@link Repository}
 *
 * @param <T>  the underlying domain class type
 * @param <ID> the underlying domain class' identifier type
 * @author abourdon
 */
@NoRepositoryBean
public interface CerberusRepository<T, ID extends Serializable> extends
        // Just to mark this interface as a Repository
        Repository<T, ID>,

        // A Cerberus Repository is based on a JPA base repository...
        JpaRepository<T, ID>,
        JpaSpecificationExecutor<T>,

        // ... with additional specific extensions
        SingleAttributeSearchingRepositoryExtension<T, ID> {
}
