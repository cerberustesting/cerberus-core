package org.cerberus.crud.dao;

import org.cerberus.crud.entity.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository extends CrudRepository<Application, String> {
}
