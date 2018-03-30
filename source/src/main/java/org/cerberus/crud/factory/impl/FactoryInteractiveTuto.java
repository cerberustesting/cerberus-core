package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.InteractiveTuto;
import org.cerberus.crud.factory.IFactoryInteractiveTuto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FactoryInteractiveTuto implements IFactoryInteractiveTuto {

    @Override
    public InteractiveTuto create(int id, String title, String description, String role, int order, int level) {
        return new InteractiveTuto(id,title, description, role, order, InteractiveTuto.Level.getEnum(level), null);
    }
}
