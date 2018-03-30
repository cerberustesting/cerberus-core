package org.cerberus.crud.dao;

import org.cerberus.crud.entity.InteractiveTuto;

import java.util.List;

public interface IInterractiveTutoDAO {

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
     * @param id
     *          The step id
     * @return
     *          if true, return all step associated to the tuto. If false, list is null
     */
    public List<InteractiveTuto> getListInteractiveTutorial(boolean withStep, String lang);
}
