package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.InteractiveTutoStep;
import org.cerberus.crud.entity.InteractiveTutoStepType;
import org.cerberus.crud.factory.IFactoryInteractiveTutoStep;
import org.springframework.stereotype.Service;

@Service
public class FactoryInteractiveTutoStep implements IFactoryInteractiveTutoStep {

    @Override
    public InteractiveTutoStep create(int id, String selector, String description, InteractiveTutoStepType type, String attr1) {
        return new InteractiveTutoStep(id,selector,description,type, attr1);
    }
}
