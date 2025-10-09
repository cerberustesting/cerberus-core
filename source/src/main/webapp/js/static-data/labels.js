/*
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


const headerLabel = {
    "workspace": { "en": "Workspace", "fr": "Espace de travail" },
    // === Section Test ===
    "test": { "en": "Test", "fr": "Test" },
    "createtestcase": { "en": "Create TestCase", "fr": "Création de TestCase" },
    "testcaselist": { "en": "TestCase List", "fr": "Liste de TestCases" },
    "edittestcase": { "en": "Edit TestCase Script", "fr": "Script de Test" },
    "label": { "en": "Label", "fr": "Labels" },
    "impactanalysis": { "en": "Impact Analysis", "fr": "Analyse d'Impact" },
    "testfolder": { "en": "Test Folder", "fr": "Dossiers de Test" },

    // === Section Data ===
    "data": { "en": "Data", "fr": "Données" },
    "datalibrary": { "en": "Data Library", "fr": "Librairie de Données" },

    // === Section Run ===
    "run": { "en": "Run", "fr": "Exécution" },
    "runtestcase": { "en": "Run Test Case", "fr": "Exécuter un Cas de Test" },
    "robot": { "en": "Robot", "fr": "Robots" },
    "campaign": { "en": "Campaign", "fr": "Campagnes" },
    "executioninqueue": { "en": "Executions in queue", "fr": "Exécutions en Attente" },

    // === Section Reporting ===
    "reportingexecution": { "en": "Execution Report", "fr": "Rapports d’Exécution" },
    "reportingexecutionlist": { "en": "Execution List", "fr": "Liste des Exécutions" },
    "reportingmonitor": { "en": "Execution Monitor", "fr": "Moniteur d’Exécution" },
    "reportingexecutionovertime": { "en": "Execution History", "fr": "Historique des Exécutions" },
    "reportingexecutionbytag": { "en": "Campaign Execution Report", "fr": "Rapport de Campagne" },
    "reportingcampaignovertime": { "en": "Campaign History", "fr": "Historique des Campagnes" },
    "reportingcampaignstatistics": { "en": "Campaign Statistics", "fr": "Statistiques des Campagnes" },

    // === Section Application ===
    "application": { "en": "Application", "fr": "Application" },
    "applicationlist": { "en": "Application", "fr": "Applications" },
    "applicationobjects": { "en": "Application Object", "fr": "Objets Applicatifs" },
    "appservice": { "en": "Service Library", "fr": "Bibliothèque de Services" },
    "deploytype": { "en": "Deployment Type", "fr": "Types de Déploiement" },

    // === Section Integration ===
    "integration": { "en": "Integration", "fr": "Intégration" },
    "integrationstatus": { "en": "Integration Status", "fr": "Statut des Intégrations" },
    "environments": { "en": "Environment", "fr": "Environnements" },
    "buildrevision": { "en": "Versioning Definition", "fr": "Définition des Versions" },
    "buildcontent": { "en": "Build Content", "fr": "Contenu du Build" },
    "batchinvariant": { "en": "Environment Events", "fr": "Événements Environnement" },

    // === Section Administration ===
    "admin": { "en": "Administration", "fr": "Administration" },
    "usersmanager": { "en": "User Management", "fr": "Gestion des Utilisateurs" },
    "logviewer": { "en": "Log Viewer", "fr": "Journaux" },
    "databasemaintenance": { "en": "Database Maintenance", "fr": "Maintenance Base" },
    "parameter": { "en": "Parameters", "fr": "Paramètres" },
    "invariants": { "en": "Invariants", "fr": "Invariants" },
    "monitoring": { "en": "Cerberus Monitoring", "fr": "Surveillance Cerberus" },

    // === Section Developer ===
    "dev": { "en": "Developer", "fr": "Développeur" },
    "swagger": { "en": "Swagger API", "fr": "API Swagger" },
    "eventhooks": { "en": "Event Hooks", "fr": "Hooks d’Événements" },

    // === Section Help ===
    "help": { "en": "Help", "fr": "Aide" },
    "documentationd1": { "en": "User Documentation", "fr": "Documentation Utilisateur" },
    "documentationd2": { "en": "Administrator Documentation", "fr": "Documentation Administrateur" },
    "documentationd3": { "en": "Usecase Documentation", "fr": "Documentation Cas d’Usage" },
    "interactivetuto": { "en": "Interactive Tutorial", "fr": "Tutoriel Interactif" },

    // === Section User ===
    "theme": {"en": "Theme", "fr":"Thème"},
    "lang": {"en": "Language", "fr":"Langue"},
    "lasttestcases": {"en":"Last seen Testcases","fr":"Derniers cas de test vus"},
    "lasttestexecutions": {"en":"Last seen Test Execution","fr":"Dernières exécutions vues"},
    "lastcampaignexecutions": {"en":"Last seen Campaigns","fr":"Dernières campagnes vues"}

};

const headerNewLabel = {
    "automate":{"en": "Automate", "fr":"Automate"},
    "quickstart":{"en": "Quick Start", "fr":"Démarrage Rapide"},
    "testdesigner":{"en": "Test Designer", "fr":"Test Designer"},
    "campaigns": { "en": "Campaigns", "fr": "Campagnes" },
    "testcases": { "en": "Test Cases", "fr": "Cas de Test" },
    "testdata": { "en": "Test Data", "fr": "Données de Test" },
    "objectrepository": { "en": "Object Repository", "fr": "Objets" },
    "steplibrary": { "en": "Step Library", "fr": "Bibliothèque d'Étapes" },
    "maintain": { "en": "Maintain", "fr": "Maintenance" },
    "systems": { "en": "Systems", "fr": "Systèmes" },
    "countries": { "en": "Countries", "fr": "Pays" },
    "environments": { "en": "Environments", "fr": "Environnements" },
    "applications": { "en": "Applications", "fr": "Applications" },
    "batches": { "en": "Batches", "fr": "Lots" },
    "execute": { "en": "Execute", "fr": "Exécution" },
    "runtests": { "en": "Run Tests", "fr": "Lancer les Tests" },
    "campaignscheduler": { "en": "Campaign Scheduler", "fr": "Planificateur de Campagnes" },
    "executionqueue": { "en": "Execution Queue", "fr": "File d’Exécution" },
    "reports": { "en": "Reports", "fr": "Rapports" },
    "monitor": { "en": "Monitor", "fr": "Surveillance" },
    "monitorautomate": { "en": "Monitor Automate", "fr": "Surveiller Automate" },
    "realtimemonitor": { "en": "Real-Time Monitor", "fr": "Surveillance Temps Réel" },
    "webmonitor": { "en": "Web Monitor", "fr": "Surveillance Web" },
    "mobilemonitor": { "en": "Mobile Monitor", "fr": "Surveillance Mobile" },
    "apimonitor": { "en": "API Monitor", "fr": "Surveillance API" },
    "analytics": { "en": "Analytics", "fr": "Analytique" },
    "dashboards": { "en": "Dashboards", "fr": "Tableaux de Bord" },
    "kpis": { "en": "KPIs", "fr": "Indicateurs (KPI)" },
    "analyticsreports": { "en": "Reports", "fr": "Rapports" },
    "configuration": { "en": "Configuration", "fr": "Configuration" },
    "parameters": { "en": "Parameters", "fr": "Paramètres" },
    "integrations": { "en": "Integrations", "fr": "Intégrations" },
    "notifications": { "en": "Notifications", "fr": "Notifications" },
    "scheduler": { "en": "Scheduler", "fr": "Planificateur" },
    "administration": { "en": "Administration", "fr": "Administration" },
    "users": { "en": "Users", "fr": "Utilisateurs" },
    "roles": { "en": "Roles", "fr": "Rôles" },
    "permissions": { "en": "Permissions", "fr": "Permissions" },
    "auditlogs": { "en": "Audit Logs", "fr": "Journaux d’Audit" },
    "developer": { "en": "Developer", "fr": "Développeur" },
    "apiexplorer": { "en": "API Explorer", "fr": "Explorateur API" },
    "webhooks": { "en": "Webhooks", "fr": "Webhooks" },
    "plugins": { "en": "Plugins", "fr": "Plugins" },
    "help": { "en": "Help", "fr": "Aide" },
    "documentation": { "en": "Documentation", "fr": "Documentation" },
    "support": { "en": "Support", "fr": "Support" },
    "about": { "en": "About", "fr": "À Propos" }
};

const pageInvariantLabel = {
    title:{en:"Invariant",fr:"Invariants"},
    subtitle:{en: "Manage the application’s constants and fixed elements.",fr:"Gérer les constantes et éléments fixes de l’application."},
}

window.headerLabel = headerLabel;
window.pageInvariantLabel = pageInvariantLabel;