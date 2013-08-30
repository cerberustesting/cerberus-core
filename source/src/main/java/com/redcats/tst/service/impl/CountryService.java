/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ICountryService;
import com.redcats.tst.service.IInvariantService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class CountryService implements ICountryService {

    @Autowired
    private IInvariantService invariantService;

    @Override
    public String getDescriptionFromCountry(String countryCode) {
        try {
            return this.invariantService.findInvariantByIdValue("COUNTRY", countryCode).getDescription();
        } catch (CerberusException ex) {
            Logger.getLogger(CountryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
