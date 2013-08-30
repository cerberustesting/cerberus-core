/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Country;
import com.redcats.tst.factory.IFactoryCountry;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryCountry implements IFactoryCountry {

    @Override
    public Country create(String country) {
        return new Country(country);
    }

}
