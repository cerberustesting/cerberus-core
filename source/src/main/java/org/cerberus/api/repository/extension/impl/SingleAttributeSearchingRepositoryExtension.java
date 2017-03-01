package org.cerberus.api.repository.extension.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;

/**
 * {@link org.cerberus.api.repository.extension.RepositoryExtension} for requests based on a single domain class' attribute.
 * <p>
 * Useful when requesting to the list of available columns when applying filter on a given Cerberus table
 *
 * @param <T>  the underlying domain class type
 * @param <ID> the underlying domain class' identifier type
 * @author abourdon
 */
public interface SingleAttributeSearchingRepositoryExtension<T, ID extends Serializable> {

    /**
     * Find all available values of a domain class' single attribute that match the given specification
     *
     * @param attribute     the domain class' single attribute from which getting values
     * @param specification the {@link Specification} to use to filter domain class' single attribute values
     * @return a {@link List} of domain class' single attribute values that match the given specification
     */
    List<?> findSingleAttribute(String attribute, Specification<T> specification);

    /**
     * Find all available values of a domain class' single attribute that match the given specification
     *
     * @param attribute     the domain class' single attribute from which getting values
     * @param specification the {@link Specification} to use to filter domain class' single attribute values
     * @param pageable      the {@link Pageable} to apply to page request
     * @return a {@link Page} of domain class' single attribute values that match the given specification and the given {@link Pageable}
     */
    Page<?> findSingleAttribute(String attribute, Specification<T> specification, Pageable pageable);

}
