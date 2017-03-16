package org.cerberus.api.repository.extension;

import java.io.Serializable;

/**
 * A marker for any {@link org.springframework.data.repository.Repository} extensions
 *
 * @param <T>  the underlying domain class type
 * @param <ID> the underlying domain class' identifier type
 * @author abourdon
 */
public interface RepositoryExtension<T, ID extends Serializable> {
}
