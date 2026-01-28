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
    "soon": {"en": "Available Soon", "fr": "Disponible prochainement"},

    // === Section Automate ===
    "automate": { "en": "Automate", "fr": "Automate" },
    "quickstart": { "en": "Quick Start", "fr": "Démarrage Rapide" },
    "import": { "en": "Import", "fr": "Import" },
    "testdesigner": { "en": "Test Designer", "fr": "Test Designer" },

    // === Section Maintain ===
    "maintain": { "en": "Maintain", "fr": "Maintenance" },
    "testcases": { "en": "TestCases", "fr": "Cas de Test" },
    "datalibrary": { "en": "Data Library", "fr": "Librairie de Données" },
    "applicationobjects": { "en": "Application Object", "fr": "Objets Applicatifs" },
    "appservice": { "en": "Service Library", "fr": "Bibliothèque de Services" },
    "label": { "en": "Label & Tag", "fr": "Labels & Tags" },
    "impactanalysis": { "en": "Impact Analysis", "fr": "Analyse d'Impact" },

    // === Section Execute ===
    "execute": { "en": "Execute", "fr": "Exécution" },
    "runtestcase": { "en": "Run Test Case", "fr": "Exécuter un Cas de Test" },
    "scheduledrun": { "en": "Scheduled Runs", "fr": "Exécutions Planifiées" },
    "robot": { "en": "Robot Management", "fr": "Robots" },
    "campaign": { "en": "Campaign Management", "fr": "Campagnes" },
    "executioninqueue": { "en": "Executions Queue", "fr": "Exécutions en Attente" },

    // === Section Monitor ===
    "monitor": { "en": "Monitor", "fr": "Supervision" },
    "monitorautomate": { "en": "Monitor Automate", "fr": "Supervision Automate" },
    "monitorrealtime": { "en": "Real-Time Monitor", "fr": "Supervision Temps-Réel" },
    "monitorweb": { "en": "Web Monitor", "fr": "Supervision Web" },
    "monitormobile": { "en": "Mobile Monitor", "fr": "Supervision Mobile" },
    "monitorapi": { "en": "API Monitor", "fr": "Supervision API" },

    // === Section Insights ===
    "insights": { "en": "Insights", "fr": "Analyses" },
    "automatescore": { "en": "Automate Score", "fr": "Automate Score" },
    "missingroleas": { "en": "Please contact @in-value to enable that feature", "fr": "Contactez @in-value pour activer cette feature" },
    "executionhistory": { "en": "Execution History", "fr": "Historique d'Exécutions" },
    "executiontrends": { "en": "Execution Trends", "fr": "Statistiques des Exécutions" },
    "campaignreport": { "en": "Campaign Report", "fr": "Rapport de Campagne" },
    "campaignhistory": { "en": "Campaign History", "fr": "Historique des Campagnes" },
    "campaigntrends": { "en": "Campaign Trends", "fr": "Statistiques des Campagnes" },

    // === Section Settings ===
    "settings": { "en": "Settings", "fr": "Configuration" },
    "applicationlist": { "en": "Application", "fr": "Applications" },
    "environments": { "en": "Environment", "fr": "Environnements" },

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

    // === Section History ===
    "history": {"en": "History", "fr":"Historique"},
    "lastseen": {"en": "Last seen", "fr":"Elements récents"},
    "lastseentestcases": {"en": "TestCases", "fr":"Cas de Test"},
    "lastseenexecutions": {"en": "Executions", "fr":"Executions"},
    "lastseencampaigns": {"en": "Campaigns", "fr":"Campagnes"},

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

const commonLabel = {
    "all":{"en": "All", "fr":"Tous"},
    "none":{"en": "None", "fr":"Aucun"},
    "search":{"en": "Search", "fr":"Recherche"},
    "buttonclose":{"en": "Close", "fr":"Fermer"},
    "buttonadd":{"en": "Add", "fr":"Ajouter"},
    "buttonduplicate":{"en": "Duplicate", "fr":"Dupliquer"},
    "buttonsave":{"en": "Save", "fr":"Sauvegarder"}
}

const homepageLabel = {
    applicationtitle: {en: "Application", fr: "Application"},
    applicationtabselected: {en: "Selected", fr: "Selectionnées"},
    applicationtabselectedlabel: {en: "Applications (Workspaces)", fr: "Applications (Espaces de travail)"},
    applicationtabtype: {en: "Per Type", fr: "Par Type"},
    applicationtabtypelabel: {en: "Application per type", fr: "Applications par type"},
    applicationtabtotal: {en: "Total", fr: "Total"},
    applicationtabtotallabel: {en: "Total Application", fr: "Applications totales"}

}

const pageInvariantLabel = {
    title: { en: "Invariant", fr: "Invariants" },
    subtitle: { en: "Manage the application’s constants and fixed elements.", fr: "Gérer les constantes et éléments fixes de l’application." },
    notifsuccesscreation: { en: "Invariant successfully created!", fr: "Invariant créé avec succès !" },
    notifsuccessmodification: { en: "Invariant successfully modified!", fr: "Invariant modifié avec succès !" },
    notifsuccessduplication: { en: "Invariant successfully duplicated!", fr: "Invariant dupliqué avec succès !" },
    editinvarianttitle: { en: "Invariant", fr: "Invariants" },
    editinvariantsubtitle: { en: "Add / Modify Invariant", fr: "Ajouter / Modifier un invariant" },
    descriptionfield: { en: "Description", fr: "Description" },
    idnamefield: { en: "Invariant Type", fr: "Type d'invariant" },
    valuefield: { en: "Value", fr: "Valeur" },
    sortfield: { en: "Sort", fr: "Ordre" },
    veryshortdescfield: { en: "Very Short Description", fr: "Description très courte" },
    gp1field: { en: "Attribute", fr: "Attribut" },
    gp2field: { en: "Attribute 2", fr: "Attribut 2" },
    gp3field: { en: "Attribute 3", fr: "Attribut 3" },
    gp4field: { en: "Attribute 4", fr: "Attribut 4" },
    gp5field: { en: "Attribute 5", fr: "Attribut 5" },
    gp6field: { en: "Attribute 6", fr: "Attribut 6" },
    gp7field: { en: "Attribute 7", fr: "Attribut 7" },
    gp8field: { en: "Attribute 8", fr: "Attribut 8" },
    gp9field: { en: "Attribute 9", fr: "Attribut 9" },
    message_remove: { en: "Are you sure?", fr: "Êtes-vous sûr ?" }
}

const pageQuickStartLabel = {
    title:{en:"Quick Start",fr:"Démarrage Rapide"},
    subtitle:{en:"Choose your preferred method to create and manage test cases quickly",fr:"Choisissez votre méthode préférée pour créer et gérer rapidement des cas de test"},
    oneclickboostraptitle:{en:"1-Click Bootstrap",fr:"Bootstrap en 1 clic"},
    oneclickboostrapsubtitle:{en:"Automatically generate test cases from your application",fr:"Générez automatiquement des cas de test à partir de votre application"},
    oneclickboostraptag1:{en:" Auto-discovery",fr:" Découverte automatique"},
    oneclickboostraptag2:{en:" Smart test generation",fr:" Génération intelligente de tests"},
    oneclickboostraptag3:{en:" Ready to run",fr:" Prêt à l’exécution"},
    oneclickboostrapbutton:{en:"Get Started →",fr:"Commencer"},
    oneclickboostrapmodaltitle:{en:"Create Test Case",fr:"Créer un cas de test"},
    oneclickboostrapmodalsubtitle:{en:"Configure your test case settings before designing the test steps",fr:"Configurez les paramètres de votre cas de test avant de concevoir les étapes du test"},
    oneclickboostrapmodalapplication:{en:"Application",fr:"Application"},
    oneclickboostrapmodaldescription:{en:"Description",fr:"Description"},
    oneclickboostrapmodalfolder:{en:"Folder",fr:"Dossier"},
    oneclickboostrapmodalchooseapplication:{en:"Choose existing application",fr:"Choisir une application existante"},
    oneclickboostrapmodalcreateapplication:{en:"Create a new application",fr:"Créer une nouvelle application"},
    oneclickboostrapmodalcreateapplicationname:{en:"Application Name",fr:"Nom de l’application"},
    oneclickboostrapmodalcreateapplicationurl:{en:"Application URL",fr:"URL de l’application"},
    oneclickboostrapmodalcreateapplicationtype:{en:"Type",fr:"Type"},
    oneclickboostrapmodalcreateapplicationcountry:{en:"Country",fr:"Pays"},
    oneclickboostrapmodalcreateapplicationenvironment:{en:"Environment",fr:"Environnement"},
    oneclickboostrapmodalcreateapplicationdescribe:{en:"Describe shortly your TestCase",fr:"Décrivez brièvement votre cas de test"},
    oneclickboostrapmodalcreateapplicationchoosefolder:{en:"Choose or create a folder",fr:"Choisissez ou créez un dossier"},
    oneclickboostrapmodalcreateapplicationtestcase:{en:"Test Case ID",fr:"ID du cas de test"},
    recordertitle:{en:"Recorder",fr:"Enregistreur"},
    recordersubtitle:{en:"Record test cases using browser automation tools",fr:"Enregistrez des cas de test à l’aide d’outils d’automatisation de navigateur"},
    recordertag1:{en:" Katalon support",fr:" Compatible Katalon"},
    recordertag2:{en:" Selenium IDE",fr:" Selenium IDE"},
    recordertag3:{en:" Easy import",fr:" Importation facile"},
    recorderbutton:{en:"Get Started →",fr:"Commencer"},
    copilottitle:{en:"Test Creation Copilot",fr:"Copilote de création de tests"},
    copilotsubtitle:{en:"AI-assisted test case creation with natural language",fr:"Création de cas de test assistée par IA en langage naturel"},
    copilottag1:{en:" Natural language",fr:" Langage naturel"},
    copilottag2:{en:" Smart suggestions",fr:" Suggestions intelligentes"},
    copilottag3:{en:" Auto-completion",fr:" Auto-complétion"},
    copilotbutton:{en:"Get Started →",fr:"Commencer"},
    copilotmodaltitle:{en:"Generate Test Case",fr:"Générer un cas de test"},
    copilotmodalsubtitle:{en:"Describe the feature and perimeter to get some automation proposals",fr:"Décrivez la fonctionnalité et le périmètre pour obtenir des propositions d’automatisation"},
    copilotmodalapplication:{en:"Application",fr:"Application"},
    copilotmodaltestfolder:{en:"Test Folder",fr:"Dossier de test"},
    copilotmodalfeaturedescription:{en:"Feature Description",fr:"Description de la fonctionnalité"},
    copilotmodalfeaturedescriptionplaceholder:{en:"What do you want to test ?",fr:"Que souhaitez-vous tester ?"},
    copilotmodalgeneratebutton:{en:"Generate",fr:"Générer"},
    copilotmodalgeneratedresult:{en:"Generated Results :",fr:"Résultats générés :"},
    copilotmodalgeneration:{en:"Generating Results...",fr:"Génération des résultats..."},
    copilotmodalgenerationsuggestion1:{en:"As a logged-in user, I can create a new automated test to verify a feature of my application.",fr:"En tant qu'utilisateur connecté, je peux créer un nouveau test automatisé pour vérifier une fonctionnalité de mon application."},
    copilotmodalgenerationsuggestion2:{en:"As a user, I can search for a record in a list using a search field.",fr:"En tant qu'utilisateur, je peux rechercher un enregistrement dans une liste grâce à un champ de recherche."},
    copilotmodalgenerationsuggestion3:{en:"As an administrator, I can enable or disable a user from the administration panel.",fr:"En tant qu'administrateur, je peux activer ou désactiver un utilisateur depuis le panneau d'administration."},
    copilotmodalgenerationsuggestion4:{en:"As a user, I can download a PDF file after submitting a valid form.",fr:"En tant qu'utilisateur, je peux télécharger un fichier PDF après avoir soumis un formulaire valide."},
    copilotmodalgenerationsuggestion5:{en:"As a logged-in user, I can change my password from the Profile menu.",fr:"En tant qu'utilisateur connecté, je peux modifier mon mot de passe depuis le menu Profil."},
    copilotmodalgenerationsuggestion6:{en:"As a user, I can log in to the application using my email and password.",fr:"En tant qu'utilisateur, je peux me connecter à l'application en utilisant mon email et mon mot de passe."},
    copilotmodalgenerationsuggestion7:{en:"As a user, I can add a product to my cart and proceed to checkout.",fr:"En tant qu'utilisateur, je peux ajouter un produit à mon panier et passer à la commande."},
    copilotmodalgenerationsuggestion8:{en:"As a manager, I can approve or reject a pending request from the validation dashboard.",fr:"En tant que manager, je peux approuver ou rejeter une demande en attente depuis le tableau de validation."},
    copilotmodalgenerationsuggestion9:{en:"As a user, I can filter search results using advanced filters.",fr:"En tant qu'utilisateur, je peux filtrer des résultats de recherche à l'aide de filtres avancés."},
    copilotmodalgenerationsuggestion10:{en:"As a user, I can reset my password by requesting a recovery email.",fr:"En tant qu'utilisateur, je peux réinitialiser mon mot de passe en demandant un email de récupération."},
    templatetitle:{en:"Test Templates Library",fr:"Bibliothèque de modèles de test"},
    templatesubtitle:{en:"Browse and use pre-built test case templates",fr:"Parcourez et utilisez des modèles de cas de test préconstruits"},
    templatetag1:{en:" Pre-built templates",fr:" Modèles préconstruits"},
    templatetag2:{en:" Industry standards",fr:" Normes industrielles"},
    templatetag3:{en:" Quick setup",fr:" Configuration rapide"},
    templatebutton:{en:"Get Started →",fr:"Commencer"},
}

const reportingCampaignStatisticsLabel ={
    title:{en:"Campaign Statistics",fr:""},
    subtitle:{en:"",fr:""},
    filterworkspace:{en:"Workspace",fr:"Espace de travail"},
    filterapplication:{en:"Application",fr:"Application"},
    filtergroup1:{en:"Group 1",fr:"Groupe 1"}
}

window.commonLabel = commonLabel;
window.headerLabel = headerLabel;
window.homepageLabel = homepageLabel;
window.pageInvariantLabel = pageInvariantLabel;
window.pageQuickStartLabel = pageQuickStartLabel;
window.reportingCampaignStatisticsLabel = reportingCampaignStatisticsLabel;