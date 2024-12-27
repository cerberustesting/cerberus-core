/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.crud.interactivetuto;

import org.cerberus.core.crud.entity.InteractiveTuto;
import org.cerberus.core.crud.entity.InteractiveTutoStep;
import org.cerberus.core.crud.service.impl.InteractiveTutoService;
import org.cerberus.core.dto.InteractiveTutoDTO;
import org.cerberus.core.dto.InteractiveTutoStepDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interactiveTuto")
public class InteractiveTutoController {
    @Autowired
    private InteractiveTutoService interactiveTutoService;

    @RequestMapping("/get")
    public ResponseEntity<InteractiveTutoDTO> getInteractiveTuto(final int id, HttpServletRequest request) {
        String lang = (String) request.getSession().getAttribute("MyLang");

        if(lang == null) {
            lang = "fr";
        }

        InteractiveTuto it = interactiveTutoService.getInteractiveTutorial(id, true, lang);

        if (it == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // TODO create a converter
        InteractiveTutoDTO result = new InteractiveTutoDTO(it.getId(), it.getTitle(), it.getDescription(), it.getRole(), it.getOrder(), it.getLevel().getValue());
        if (!CollectionUtils.isEmpty(it.getSteps())) {
            result.setSteps(new LinkedList<>());
            for (InteractiveTutoStep step : it.getSteps()) {
                result.getSteps().add(new InteractiveTutoStepDTO(step.getId(), step.getSelectorJquery(), step.getText(), step.getType(), step.getAttr1()));
            }
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/list")
    public ResponseEntity<List<InteractiveTutoDTO>> getListInteractiveTuto(HttpServletRequest request) {
        String lang = (String) request.getSession().getAttribute("MyLang");

        if(lang == null) {
            lang = "en";
        }

        List<InteractiveTuto> it = interactiveTutoService.getListInteractiveTutorial(false, lang);

        if (CollectionUtils.isEmpty(it)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(listInteractiveTuto(it), HttpStatus.OK);
    }


    private InteractiveTutoDTO convertInteractiveTuto(InteractiveTuto it) {
        InteractiveTutoDTO result = new InteractiveTutoDTO(it.getId(), it.getTitle(), it.getDescription(), it.getRole(), it.getOrder(), it.getLevel().getValue());
        if (!CollectionUtils.isEmpty(it.getSteps())) {
            result.setSteps(new LinkedList<>());
            for (InteractiveTutoStep step : it.getSteps()) {
                result.getSteps().add(new InteractiveTutoStepDTO(step.getId(), step.getSelectorJquery(), step.getText(), step.getType(), step.getAttr1()));
            }
        }
        return result;
    }

    private List<InteractiveTutoDTO> listInteractiveTuto(List<InteractiveTuto> itlist) {
        return itlist.stream().map(it -> convertInteractiveTuto(it)).collect(Collectors.toList());
    }
}
