/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.mapper;

import org.cerberus.crud.entity.Invariant;
import org.cerberus.dto.publicv1.InvariantDTOV1;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mlombard
 */


public class InvariantMapper {
    
    @Autowired
    private static final ModelMapper modelMapper = new ModelMapper();
    
    private InvariantMapper() {
        throw new IllegalStateException("This is a utility class");
    }
    
    public static InvariantDTOV1 convertToDto(Invariant invariant) {
        return modelMapper.typeMap(Invariant.class, InvariantDTOV1.class).addMappings(mapper -> {
            mapper.map(src -> src.getGp1(), InvariantDTOV1::setAttribute1);
            mapper.map(src -> src.getGp2(), InvariantDTOV1::setAttribute2);
            mapper.map(src -> src.getGp3(), InvariantDTOV1::setAttribute3);
            mapper.map(src -> src.getGp4(), InvariantDTOV1::setAttribute4);
            mapper.map(src -> src.getGp5(), InvariantDTOV1::setAttribute5);
            mapper.map(src -> src.getGp6(), InvariantDTOV1::setAttribute6);
            mapper.map(src -> src.getGp7(), InvariantDTOV1::setAttribute7);
            mapper.map(src -> src.getGp8(), InvariantDTOV1::setAttribute8);
            mapper.map(src -> src.getGp9(), InvariantDTOV1::setAttribute9);
        }).map(invariant);
    }

    public static Invariant convertToEntity(InvariantDTOV1 invariantDTO) {
        return modelMapper.typeMap(InvariantDTOV1.class, Invariant.class).addMappings(mapper -> {
            mapper.map(src -> src.getAttribute1(), Invariant::setGp1);
            mapper.map(src -> src.getAttribute2(), Invariant::setGp2);
            mapper.map(src -> src.getAttribute3(), Invariant::setGp3);
            mapper.map(src -> src.getAttribute4(), Invariant::setGp4);
            mapper.map(src -> src.getAttribute5(), Invariant::setGp5);
            mapper.map(src -> src.getAttribute6(), Invariant::setGp6);
            mapper.map(src -> src.getAttribute7(), Invariant::setGp7);
            mapper.map(src -> src.getAttribute8(), Invariant::setGp8);
            mapper.map(src -> src.getAttribute9(), Invariant::setGp9);
        }).map(invariantDTO);
    }
}
