/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Country;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountry {
    
    Country create(String country);
    }
