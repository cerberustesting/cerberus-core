package org.cerberus.crud.service;

import org.cerberus.crud.entity.InteractiveTuto;

import java.util.List;

public interface IInterractiveTutoService {

    /**
     *
     * @param id
     *          the step id
     * @param withStep
     *          if true, return all step associated to the tuto. If false, list is null
     * @return
     */
    public InteractiveTuto getInteractiveTutorial(int id, boolean withStep, String lang);

    /**
     * Return all step available
     * @return
     *          if true, return all step associated to the tuto. If false, list is null
     */
    public List<InteractiveTuto> getListInteractiveTutorial(boolean withStep, String lang);
}
