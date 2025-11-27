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
package org.cerberus.core.service.ai.impl;

import com.anthropic.models.messages.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIConfig {

    @Autowired
    ParameterService params;

    private static final Logger LOG = LogManager.getLogger(AIConfig.class);

    public String apiKey() {
        return params.getParameterStringByKey("cerberus_anthropic_apikey", "", "apikey");
    }

    public int maxTokens() {
        return params.getParameterIntegerByKey("cerberus_anthropic_maxtoken", "", 1024);
    }

    public double priceInput() {
        return params.getParameterDoubleByKey("cerberus_anthropic_price_input_per_million", "", 3.0);
    }

    public double priceOutput() {
        return params.getParameterDoubleByKey("cerberus_anthropic_price_output_per_million", "", 12.0);
    }

    /** RAW MODEL STRING */
    public String modelName() {
        return params.getParameterStringByKey("cerberus_anthropic_defaultmodel", "","claude-sonnet-4-5-20250929");
    }

    public Model aiModel() {
        String raw = modelName();

        if (raw == null || raw.isEmpty()) {
            return Model.CLAUDE_SONNET_4_5_20250929;
        }

        String cleaned = raw.replace("-", "_").replace(".", "_").toUpperCase().trim();

        try {
            LOG.debug(Model.class.getSimpleName() + ": raw=" + raw + ", cleaned=" + cleaned);
            return Model.of(cleaned);
        } catch (IllegalArgumentException e) {
            LOG.warn("Unknown Anthropic model : " + raw + " — using CLAUDE_SONNET_4_5_20250929 instead.");
            return Model.CLAUDE_SONNET_4_5_20250929;
        }
    }
}
