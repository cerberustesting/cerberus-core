/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.factory;

import org.cerberus.entity.SoapLibrary;

/**
 *
 * @author cte
 */
public interface IFactorySoapLibrary {
    
    SoapLibrary create(String type,String name,String envelope,String description, String servicePath, String parsingAnswer, String method);
}
