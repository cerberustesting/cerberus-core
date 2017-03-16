package org.cerberus.crud.repository;

import org.cerberus.api.repository.CerberusRepository;
import org.cerberus.crud.entity.Robot;
import org.springframework.stereotype.Repository;

@Repository
public interface IRobotRepository extends CerberusRepository<Robot, Integer> {

    Robot findByRobot(String robot);

}
