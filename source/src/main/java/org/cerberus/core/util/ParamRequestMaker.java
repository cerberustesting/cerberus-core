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
package org.cerberus.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

/**
 * A <{@link String}, {@link String}> typed {@link Properties} used to make an
 * URL {@link String} parameter line
 * 
 * @author abourdon
 */
public class ParamRequestMaker {

	public static final String PARAM_SEPARATOR = "&";

	private Properties params = new Properties();

	public ParamRequestMaker() {
	}

	/**
	 * Adds a param to this {@link ParamRequestMaker}
	 * 
	 * <p>
	 * If value is <code>null</code> or empty then the param is not added
	 * </p>
	 * 
	 * @param param
	 * @param value
	 */
	public void addParam(String param, String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		params.put(param, value);
	}

	public void removeParam(String param) {
		params.remove(param);
	}

	public String getParam(String param) {
		return (String) params.get(param);
	}

	public int size() {
		return params.size();
	}

	/**
	 * Constructs the {@link String} representation of this
	 * {@link ParamRequestMaker} by joining every key/value pairs with the
	 * {@link #PARAM_SEPARATOR}
	 * 
	 * @return
	 */
	public String mkString() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Object, Object> param : params.entrySet()) {
			builder.append(param.getKey());
			builder.append("=");
			builder.append(param.getValue());
			builder.append(PARAM_SEPARATOR);
		}
		// Remove the last PARAM_SEPARATOR
		builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	}

	/**
	 * Same as {@link #mkString()} but by encoding each key/value pair
	 * 
	 * @param charset
	 *            the charset use to encode each key/value pair
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String mkString(String charset) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Object, Object> param : params.entrySet()) {
			builder.append(param.getKey());
			builder.append("=");
			builder.append(URLEncoder.encode((String) param.getValue(), charset));
			builder.append(PARAM_SEPARATOR);
		}
		// Remove the last PARAM_SEPARATOR
		builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	}

}
