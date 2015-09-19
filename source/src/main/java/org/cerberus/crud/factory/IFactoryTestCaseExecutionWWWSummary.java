/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.factory;

import org.cerberus.crud.entity.StatisticSummary;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionWWWSummary {
    
    StatisticSummary create(int totNbHits,int totTps,int totSize,int nbRc2xx,int nbRc3xx,int nbRc4xx,
            int nbRc5xx,int imgNb,int imgTps,int imgSizeTot,int imgSizeMax,String imgSizeMaxUrl,
            int jsNb,int jsTps,int jsSizeTot,int jsSizeMax,String jsSizeMaxUrl,int cssNb,int cssTps,
            int cssSizeTot,int cssSizeMax,String cssSizeMaxUrl);
}
