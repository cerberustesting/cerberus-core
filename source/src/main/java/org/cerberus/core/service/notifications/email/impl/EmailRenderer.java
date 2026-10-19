/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import java.util.HashMap;
import static java.util.Map.entry;
import java.util.Locale;
import java.util.Map;
import org.cerberus.core.service.notifications.email.IEmailRenderer;
import org.cerberus.core.service.notifications.email.IEmailThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Service
public class EmailRenderer implements IEmailRenderer {

    // Same colors as the front (tailwind -500) : the icon of a mail has the color of its family.
    private static final Map<String, String> PALETTE = Map.of(
            "blue", "#3b82f6", "green", "#22c55e", "purple", "#a855f7", "orange", "#f97316",
            "red", "#ef4444", "amber", "#f59e0b", "gray", "#64748b");

    // Lucide icon (emails/icons/<icon>.png) and default color displayed before the title of each template.
    // A template can override the color with the variable titleColor (hex) or titleColorName (palette name),
    // ex : the end of campaign / execution takes the color of its result.
    private static final Map<String, String[]> TEMPLATE_STYLE = Map.ofEntries(
            entry("campaign-start", new String[]{"globe", "blue"}),
            entry("campaign-end", new String[]{"globe", "green"}),
            entry("execution-start", new String[]{"play", "blue"}),
            entry("execution-end", new String[]{"play", "green"}),
            entry("testcase-change", new String[]{"file-text", "green"}),
            entry("revision-change", new String[]{"git-branch", "blue"}),
            entry("disable-environment", new String[]{"circle-x", "red"}),
            entry("new-chain", new String[]{"workflow", "blue"}),
            entry("account-creation", new String[]{"user-plus", "green"}),
            entry("forgot-password", new String[]{"key", "blue"}));
    private static final String[] DEFAULT_STYLE = {"mail", "blue"};

    @Autowired
    private IEmailThemeService emailThemeService;

    private final TemplateEngine engine;

    public EmailRenderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("emails/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Override
    public String render(String template, String title, Map<String, Object> variables, Locale locale, String system) {
        Map<String, Object> model = new HashMap<>();
        if (variables != null) {
            model.putAll(variables);
        }
        String[] style = TEMPLATE_STYLE.getOrDefault(template, DEFAULT_STYLE);
        model.put("theme", emailThemeService.read(system));
        model.put("title", title);
        model.put("palette", PALETTE);
        model.put("titleIcon", style[0]);
        if (model.get("titleColor") == null) {
            model.put("titleColor", PALETTE.getOrDefault(String.valueOf(model.get("titleColorName")), PALETTE.get(style[1])));
        }
        return engine.process(template, new Context(locale == null ? Locale.ENGLISH : locale, model));
    }
}
