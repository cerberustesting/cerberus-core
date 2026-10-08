/*
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
    "testcases": { "en": "Test Case", "fr": "Cas de Test" },
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
    "campaignhistory": { "en": "Campaign Trends", "fr": "Historique des Campagnes" },
    "campaigntrends": { "en": "Campaign Statistics", "fr": "Statistiques des Campagnes" },

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
    "mcpinspector": { "en": "MCP Inspector", "fr": "Inspecteur MCP" },
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
    title: { en: "Invariants", fr: "Invariants" },
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

const pageParameterLabel = {
    title: { en: "Parameters", fr: "Paramètres" },
    subtitle: { en: "Manage the application’s parameters.", fr: "Gérer les paramètres de l’application." }
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

const applicationObjectLabel = {
    title: {en: "Application Object", fr: "Objets d'Application"},
    subtitle: {
        en: "Technical element of the application (ID, XPath, URI, locator), shared across all configurations (country, environment) and reusable in all tests.",
        fr: "Element technique de l’Application (ID, XPath, URI, locator), commun à toutes les configurations (pays, environnement) et réutilisable dans tous les tests."
    },
    application: {en: "Application", fr: "Application"},
    webpage: {en: "Page/Screen Name", fr: "Nom de Page/Ecran"},
    screenshot: {en: "Fullpage Screenshot", fr: "Capture écran"},
    html: {en: "HTML page code", fr: "Code de la page HTML"},
    firstmessage: {
        en: "Hello! Upload an <span class='text-sky-500 font-semibold'>HTML page</span> and a <span class='text-sky-500 font-semibold'>Screenshot</span> so I can analyze the UI elements and suggest Application Objects. Define The <span class='text-sky-500 font-semibold'>Application</span> and the <span class='text-sky-500 font-semibold'>Page Name</span> so I can attach the objects to it.",
        fr: "Bonjour ! Chargez une <span class='text-sky-500 font-semibold'>page HTML</span> et un <span class='text-sky-500 font-semibold'>Screenshot</span> pour que j'analyse les éléments de l'interface et vous propose des Application Objects. Définissez également une <span class='text-sky-500 font-semibold'>Application</span> et un <span class='text-sky-500 font-semibold'>Nom de Page</span> que je puisse y attacher les objets."
    },
    modaltitle: {en:"Application Object Generator", fr:"Generateur d'objets d'Application"},
    modalsubtitle: {en:"AI Assistant to create your technical objects", fr:"Assistant IA pour créer vos objets techniques"},
    modalcontext:  {en:"Context", fr:"Contexte"},
    file:  {en:"files", fr:"fichiers"},
    upload:  {en:"Upload an HTML file and a screenshot", fr:"Déposez un fichier HTML et un Screenshot"},
    clicktoselect:  {en:"or click to select a file", fr:"ou cliquez pour sélectionner"},
    application:  {en:"Application", fr:"Application"},
    webpage:  {en:"Page Name", fr:"Nom de la Page"},
    webpageplaceholder:  {en:"Page / Screen Name", fr:"Nom de la Page / de l'Ecran"},
    chatplaceholder:  {en:"Describe elements to identify", fr:"Décrivez les éléments à identifier..."}

}

const testcaseSimpleCreationImportLabel = {
    modaltitle: {en:"Import Recording", fr:"Importation d'enregistrement"},
    modalsubtitle: {en:"Import test cases from Selenium IDE or Katalon Recorder", fr:"Importer des cas de test depuis Selenium IDE ou Katalon Recorder"}
}

const testcaseSimpleCreationLabel = {
    modaltitle: {en:"Create Test Case", fr:"Créer un Cas de Test"},
    modalsubtitle: {en:"Configure your test case settings before designing the test steps", fr:"Configurer le cas de test avant le design des pas de test"}
}

const testcaseSimpleExecutionLabel = {
    modaltitle: {en:"Run Test Case", fr:"Exécuter un Cas de Test"},
    modalsubtitle: {en:"Run your test case on specific environments, defining robots and parameters", fr:"Exécuter le cas de test sur les environnements selectionnés, avec les robots et paramètres choisis."}
}

const pageRunTestsLabel = {
    title: { en: "Run Tests", fr: "Exécuter des tests" },
    modetests: { en: "Select test cases", fr: "Choisir des cas de test" },
    modecampaign: { en: "Load a campaign", fr: "Charger une campagne" },
    pickcampaign: { en: "Pick a campaign...", fr: "Choisir une campagne..." },
    campaignhint: { en: "The campaign fills the parameters below, you can override them before the run.", fr: "La campagne remplit les paramètres ci-dessous, vous pouvez les surcharger avant l'exécution." },
    summary: { en: "{0} test case(s), {1} execution(s)", fr: "{0} cas de test, {1} exécution(s)" },
    run: { en: "Run", fr: "Exécuter" },
    runsee: { en: "Run (and see result)", fr: "Exécuter (et voir le résultat)" },
    runcampaign: { en: "Run campaign", fr: "Exécuter la campagne" },
    runcampaignsee: { en: "Run campaign (and see result)", fr: "Exécuter la campagne (et voir le résultat)" },
    campaigntests: { en: "Campaign test cases", fr: "Cas de test de la campagne" },
    testcases: { en: "Test cases", fr: "Cas de test" },
    filters: { en: "Filters", fr: "Filtres" },
    any: { en: "Any", fr: "Tous" },
    search: { en: "Search", fr: "Rechercher" },
    resultsize: { en: "Result size", fr: "Nombre de résultats" },
    quickfilter: { en: "Filter the list...", fr: "Filtrer la liste..." },
    selectall: { en: "Select all", fr: "Tout sélectionner" },
    selectnone: { en: "Select none", fr: "Tout désélectionner" },
    loading: { en: "Loading...", fr: "Chargement..." },
    pickcampaignfirst: { en: "Pick a campaign to see its test cases.", fr: "Choisissez une campagne pour voir ses cas de test." },
    filterloaderror: { en: "Unable to load the values of the filter {0}.", fr: "Impossible de charger les valeurs du filtre {0}." },
    filterenvs: { en: "Filter environments...", fr: "Filtrer les environnements..." },
    filterrobots: { en: "Filter robots...", fr: "Filtrer les robots..." },
    executors: { en: "{0} executor(s)", fr: "{0} exécuteur(s)" },
    timeouthint: { en: "Override timeout (ms)", fr: "Surcharger le timeout (ms)" },
    priorityhint: { en: "Override priority (default 1000)", fr: "Surcharger la priorité (défaut 1000)" },
    notestcase: { en: "No test case available to run. Change the filters, select another system or create some.", fr: "Aucun cas de test à exécuter. Modifiez les filtres, choisissez un autre système ou créez-en." },
    test: { en: "Test", fr: "Test" },
    testcase: { en: "Test case", fr: "Cas de test" },
    application: { en: "Application", fr: "Application" },
    description: { en: "Description", fr: "Description" },
    label: { en: "Label", fr: "Label" },
    status: { en: "Status", fr: "Statut" },
    creator: { en: "Creator", fr: "Créateur" },
    implementer: { en: "Implementer", fr: "Implémenteur" },
    type: { en: "Type", fr: "Type" },
    priority: { en: "Priority", fr: "Priorité" },
    system: { en: "System", fr: "Système" },
    campaign: { en: "Campaign", fr: "Campagne" },
    target: { en: "Environment and country", fr: "Environnement et pays" },
    automatic: { en: "Automatic", fr: "Automatique" },
    manual: { en: "Manual", fr: "Manuel" },
    environments: { en: "Environments", fr: "Environnements" },
    disabled: { en: "[currently disabled]", fr: "[actuellement désactivé]" },
    myhost: { en: "My host", fr: "Mon host" },
    mycontextroot: { en: "My context root", fr: "Mon context root" },
    myloginrelativeurl: { en: "My login relative URL", fr: "Mon URL relative de login" },
    myenvdata: { en: "My data environment", fr: "Mon environnement de données" },
    countries: { en: "Countries", fr: "Pays" },
    robot: { en: "Robot", fr: "Robot" },
    editrobot: { en: "Edit robot", fr: "Modifier le robot" },
    newrobot: { en: "New robot", fr: "Nouveau robot" },
    customconfig: { en: "Custom configuration", fr: "Configuration personnalisée" },
    seleniumip: { en: "Robot server IP", fr: "IP du serveur robot" },
    seleniumport: { en: "Robot server port", fr: "Port du serveur robot" },
    browser: { en: "Browser", fr: "Navigateur" },
    andmore: { en: "(and {0} more...)", fr: "(et {0} de plus...)" },
    noexecutor: { en: "No executor found...", fr: "Aucun exécuteur trouvé..." },
    multirobot: { en: "{0} robots selected: one execution per robot.", fr: "{0} robots sélectionnés : une exécution par robot." },
    execution: { en: "Execution parameters", fr: "Paramètres d'exécution" },
    saveprefs: { en: "Save as my preferences", fr: "Enregistrer comme préférences" },
    resetprefs: { en: "Reset preferences", fr: "Réinitialiser les préférences" },
    prefssaved: { en: "Preferences saved", fr: "Préférences enregistrées" },
    tag: { en: "Tag", fr: "Tag" },
    verbose: { en: "Verbose", fr: "Verbose" },
    screenshot: { en: "Screenshot", fr: "Capture d'écran" },
    video: { en: "Video", fr: "Vidéo" },
    pagesource: { en: "Page source", fr: "Source de la page" },
    robotlog: { en: "Robot log", fr: "Log du robot" },
    consolelog: { en: "Console log", fr: "Log de la console" },
    timeout: { en: "Timeout", fr: "Timeout" },
    retries: { en: "Retries", fr: "Nouvelles tentatives" },
    manualexecution: { en: "Manual execution", fr: "Exécution manuelle" },
    selectonetestcase: { en: "Select at least one test case.", fr: "Sélectionnez au moins un cas de test." },
    selectonecampaign: { en: "Pick a campaign.", fr: "Choisissez une campagne." },
    selectoneenv: { en: "Select at least one environment.", fr: "Sélectionnez au moins un environnement." },
    selectonecountry: { en: "Select at least one country.", fr: "Sélectionnez au moins un pays." },
    errrobot: { en: "{0} executions not added due to <b>empty robot</b>.", fr: "{0} exécutions non ajoutées car <b>robot vide</b>." },
    errtcnotactive: { en: "{0} executions not added due to <b>test case not active</b>.", fr: "{0} exécutions non ajoutées car <b>cas de test inactif</b>." },
    errtcnotallowed: { en: "{0} executions not added due to <b>test case not allowed on the group of environment</b>.", fr: "{0} exécutions non ajoutées car <b>cas de test non autorisé sur le groupe d'environnement</b>." },
    errenv: { en: "{0} executions not added due to <b>environment/country not active or not existing</b>.", fr: "{0} exécutions non ajoutées car <b>environnement/pays inactif ou inexistant</b>." },
    openexecution: { en: "Open execution", fr: "Ouvrir l'exécution" },
    reportbytag: { en: "Report by tag", fr: "Rapport par tag" }
};

const pageReportingMonitorWebLabel = {
    title: { en: "Web Monitor", fr: "Supervision Web" },
    favorites: { en: "Favorites", fr: "Favoris" },
    choosetestcase: { en: "Choose a test case...", fr: "Choisir un cas de test..." },
    testcase: { en: "Test case", fr: "Cas de test" },
    searchfolder: { en: "Search test folder...", fr: "Rechercher un dossier de test..." },
    searchtestcase: { en: "Search test case...", fr: "Rechercher un cas de test..." },
    nofolder: { en: "No test folder matches", fr: "Aucun dossier de test" },
    notestcase: { en: "No test case matches", fr: "Aucun cas de test" },
    back: { en: "Back to the folders", fr: "Retour aux dossiers" },
    addfavorite: { en: "Add to favorites", fr: "Ajouter aux favoris" },
    removefavorite: { en: "Remove from favorites", fr: "Retirer des favoris" },
    period: { en: "Period", fr: "Période" },
    browser: { en: "Browser", fr: "Navigateur" },
    environment: { en: "Environment", fr: "Environnement" },
    country: { en: "Country", fr: "Pays" },
    refresh: { en: "Refresh", fr: "Rafraîchir" },
    refreshed: { en: "refreshed {0}", fr: "actualisé {0}" },
    loading: { en: "Loading...", fr: "Chargement..." },
    empty: { en: "Choose a test case, or open a favorite, to see its network behavior.", fr: "Choisissez un cas de test, ou ouvrez un favori, pour voir son comportement réseau." },
    nodata: { en: "No network statistics for this test case over the period. They are recorded when the execution captures the network traffic (HAR).", fr: "Aucune statistique réseau pour ce cas de test sur la période. Elles sont enregistrées quand l'exécution capture le trafic réseau (HAR)." },
    loaderror: { en: "Unable to load the data: {0}", fr: "Impossible de charger les données : {0}" },
    responsetime: { en: "Network time", fr: "Temps réseau" },
    executionscount: { en: "{0} executions", fr: "{0} exécutions" },
    vsprevious: { en: "vs previous period", fr: "vs période précédente" },
    novsprevious: { en: "no previous period data", fr: "pas de données sur la période précédente" },
    totaltime: { en: "Total", fr: "Total" },
    internaltime: { en: "Internal", fr: "Interne" },
    statusperexecution: { en: "Status per execution - click to inspect", fr: "Statut par exécution - cliquer pour inspecter" },
    traffic: { en: "Network traffic", fr: "Trafic réseau" },
    avgtransfer: { en: "average transfer", fr: "transfert moyen" },
    successrate: { en: "Success rate", fr: "Taux de réussite" },
    failures: { en: "Failures", fr: "Échecs" },
    requests: { en: "Requests", fr: "Requêtes" },
    thirdparties: { en: "Third party hosts", fr: "Hôtes tiers" },
    weightbytype: { en: "Weight by content type", fr: "Poids par type de contenu" },
    totalof: { en: "{0} in total", fr: "{0} au total" },
    thirdpartyhosts: { en: "Third party hosts", fr: "Hôtes tiers" },
    thirdpartysub: { en: "calls outside the main domain", fr: "appels hors domaine principal" },
    nothirdparty: { en: "No third party host for this execution.", fr: "Aucun hôte tiers pour cette exécution." },
    execution: { en: "Execution #{0}", fr: "Exécution #{0}" },
    open: { en: "Open the execution", fr: "Ouvrir l'exécution" },
    internaltimelabel: { en: "Internal time", fr: "Temps interne" },
    transfer: { en: "Transfer", fr: "Transfert" },
    latest: { en: "Latest executions", fr: "Dernières exécutions" },
    execcol: { en: "Execution", fr: "Exécution" },
    status: { en: "Status", fr: "Statut" },
    robot: { en: "Robot", fr: "Robot" },
    size: { en: "Size", fr: "Taille" },
    other: { en: "Other", fr: "Autre" },
    typeimg: { en: "Images", fr: "Images" },
    typejs: { en: "JavaScript", fr: "JavaScript" },
    typecss: { en: "CSS", fr: "CSS" },
    typehtml: { en: "HTML", fr: "HTML" },
    typemedia: { en: "Media", fr: "Médias" },
    typeother: { en: "Other", fr: "Autre" }
};

const pageScheduledRunsLabel = {
    title: { en: "Scheduled Runs", fr: "Exécutions planifiées" },
    history: { en: "History", fr: "Historique" },
    campaign: { en: "Campaign", fr: "Campagne" },
    allcampaigns: { en: "All campaigns", fr: "Toutes les campagnes" },
    refresh: { en: "Refresh", fr: "Rafraîchir" },
    newschedule: { en: "New schedule", fr: "Nouvelle planification" },
    close: { en: "Close", fr: "Fermer" },
    refreshed: { en: "refreshed {0}", fr: "actualisé {0}" },
    cronzone: { en: "cron evaluated in {0}", fr: "cron évalué dans le fuseau {0}" },
    pickcampaign: { en: "Pick a campaign...", fr: "Choisir une campagne..." },
    cronlabel: { en: "Cron (Quartz: sec min hour day-of-month month day-of-week)", fr: "Cron (Quartz : sec min heure jour-du-mois mois jour-de-semaine)" },
    description: { en: "Description", fr: "Description" },
    create: { en: "Create", fr: "Créer" },
    presets: { en: "Presets", fr: "Préréglages" },
    preset15: { en: "Every 15 min", fr: "Toutes les 15 min" },
    presethourly: { en: "Hourly", fr: "Toutes les heures" },
    presetdaily: { en: "Daily 02:00", fr: "Tous les jours à 02:00" },
    presetweekdays: { en: "Weekdays 08:00", fr: "Jours ouvrés à 08:00" },
    presetsunday: { en: "Sunday 22:00", fr: "Dimanche à 22:00" },
    kpiactive: { en: "Active schedules", fr: "Planifications actives" },
    kpiactivesub: { en: "out of {0} ({1} paused)", fr: "sur {0} ({1} en pause)" },
    kpinext: { en: "Next run", fr: "Prochain run" },
    kpinextnone: { en: "nothing planned", fr: "rien de prévu" },
    kpifired: { en: "Fired", fr: "Déclenchées" },
    kpifiredsub: { en: "over the last {0} day(s)", fr: "sur les {0} derniers jour(s)" },
    kpisuccess: { en: "Trigger success", fr: "Succès du déclenchement" },
    kpisuccesssub: { en: "campaigns accepted by the queue", fr: "campagnes acceptées par la file" },
    kpierrors: { en: "Errors", fr: "Erreurs" },
    kpierrorslast: { en: "last: {0}", fr: "dernière : {0}" },
    kpierrorsnone: { en: "none on the period", fr: "aucune sur la période" },
    upcoming: { en: "Upcoming runs", fr: "Prochains runs" },
    upcomingempty: { en: "Nothing planned: no active schedule.", fr: "Rien de prévu : aucune planification active." },
    when: { en: "When", fr: "Quand" },
    in: { en: "In", fr: "Dans" },
    schedules: { en: "Schedules", fr: "Planifications" },
    schedulesempty: { en: "No schedule yet. Use \"New schedule\" to run a campaign on a cron.", fr: "Aucune planification. Utilisez « Nouvelle planification » pour lancer une campagne sur un cron." },
    cron: { en: "Cron", fr: "Cron" },
    state: { en: "State", fr: "État" },
    lastrun: { en: "Last run", fr: "Dernier run" },
    success: { en: "Success", fr: "Succès" },
    active: { en: "active", fr: "active" },
    paused: { en: "paused", fr: "en pause" },
    pause: { en: "Pause", fr: "Mettre en pause" },
    resume: { en: "Resume", fr: "Reprendre" },
    delete: { en: "Delete", fr: "Supprimer" },
    historytitle: { en: "History ({0})", fr: "Historique ({0})" },
    historyempty: { en: "No scheduled execution on this period.", fr: "Aucune exécution planifiée sur cette période." },
    scheduled: { en: "Scheduled", fr: "Planifiée" },
    status: { en: "Status", fr: "Statut" },
    detail: { en: "Detail", fr: "Détail" },
    showmore: { en: "Show more ({0} left)", fr: "Afficher plus ({0} restants)" },
    justnow: { en: "just now", fr: "à l'instant" },
    ago: { en: "{0} ago", fr: "il y a {0}" },
    now: { en: "now", fr: "maintenant" },
    inx: { en: "in {0}", fr: "dans {0}" },
    loaderror: { en: "Could not read the schedules. {0}", fr: "Impossible de lire les planifications. {0}" },
    loadunreachable: { en: "Could not read the schedules (server unreachable).", fr: "Impossible de lire les planifications (serveur injoignable)." },
    opfailed: { en: "Operation failed.", fr: "L'opération a échoué." },
    opunreachable: { en: "Operation failed (server unreachable).", fr: "L'opération a échoué (serveur injoignable)." },
    pickboth: { en: "Pick a campaign and a cron definition.", fr: "Choisissez une campagne et une définition cron." },
    confirmdelete: { en: "Delete the schedule \"{0}\" of campaign {1} ?", fr: "Supprimer la planification « {0} » de la campagne {1} ?" }
};

const datePickerLabel = {
    year: {en:"Year", fr:"Année"},
    month: {en:"Month", fr:"Mois"},
    day: {en:"Day", fr:"Jour"},
    hour: {en:"Hour", fr:"Heure"}
}

window.commonLabel = commonLabel;
window.headerLabel = headerLabel;
window.homepageLabel = homepageLabel;
window.pageInvariantLabel = pageInvariantLabel;
window.pageParameterLabel = pageParameterLabel;
window.pageQuickStartLabel = pageQuickStartLabel;
window.reportingCampaignStatisticsLabel = reportingCampaignStatisticsLabel;
window.applicationObjectLabel = applicationObjectLabel;
window.testcaseSimpleCreationImportLabel = testcaseSimpleCreationImportLabel;
window.testcaseSimpleCreationLabel = testcaseSimpleCreationLabel;
window.testcaseSimpleExecutionLabel = testcaseSimpleExecutionLabel;
window.pageScheduledRunsLabel = pageScheduledRunsLabel;
window.pageReportingMonitorWebLabel = pageReportingMonitorWebLabel;
window.pageRunTestsLabel = pageRunTestsLabel;
window.datePickerLabel = datePickerLabel;