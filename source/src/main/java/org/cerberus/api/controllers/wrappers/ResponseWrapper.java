/*
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

package org.cerberus.api.controllers.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cerberus.api.dto.views.View;

import java.util.Collection;

@JsonView(View.Public.GET.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResponseWrapper<T> {

    private T data;
    private String message;
    private int length;
    private int statusCode;

    public static <T> ResponseWrapper<T> wrap(T t) {
        ResponseWrapper<T> responseWrapper = new ResponseWrapper<>();
        if (t instanceof Collection<?>) {
            responseWrapper.setLength(((Collection<?>) t).size());
            responseWrapper.setData(t);
        } else {
            responseWrapper.setData(t);
            responseWrapper.setLength(1);
        }
        return responseWrapper;
    }
}
