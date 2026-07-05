<jsp:include page="../transversal/ApplicationObject.html"/>
<jsp:include page="../transversal/EventHook.html"/>
<jsp:include page="../transversal/Parameter.html"/>
<jsp:include page="../transversal/File.html"/>
<jsp:include page="../transversal/Application.html"/>
<jsp:include page="../transversal/AppService.html"/>
<jsp:include page="../transversal/Campaign.html"/>
<jsp:include page="../transversal/Robot.html"/>
<jsp:include page="../transversal/TestCase.html"/>
<jsp:include page="../transversal/TestCaseProperty.html"/>
<jsp:include page="../transversal/TestCaseStep.html"/>
<jsp:include page="../transversal/TestDataLib.html"/>
<jsp:include page="../transversal/TestCaseSimpleCreation.html"/>
<jsp:include page="../transversal/TestCaseSimpleCreationAI.html"/>
<jsp:include page="../transversal/TestCaseSimpleCreationImport.html"/>
<jsp:include page="../transversal/ApplicationObjectGenerationAI.html"/>
<jsp:include page="../transversal/TestCaseExecutionQueue.html"/>
<jsp:include page="../templates/selectDropdown.html"/>
<jsp:include page="../global/datatableConfig.html"/>
<script>
    window.appContext = "${pageContext.request.contextPath}";
</script>
<script type="text/javascript" src="js/transversalobject/crbDropdown.js?v=${appVersion}"></script>
<script>
    // All .crb_modal roots share the same fixed z-index (9999), so when a modal is opened
    // from inside another one (e.g. editing the Application from the Service Library modal's
    // pencil icon), plain DOM/include order decides which one paints on top and the nested
    // modal can end up hidden behind the one that spawned it. Watch each modal's inline
    // `style` (what x-show mutates) and bring whichever one was just shown above any other
    // modal that is still open, regardless of include order.
    (function () {
        var modals = document.querySelectorAll(".crb_modal");
        var topZIndex = modals.length ? (parseInt(getComputedStyle(modals[0]).zIndex, 10) || 9999) : 9999;
        modals.forEach(function (modal) {
            new MutationObserver(function () {
                if (getComputedStyle(modal).display !== "none") {
                    topZIndex += 1;
                    modal.style.zIndex = topZIndex;
                }
            }).observe(modal, {attributes: true, attributeFilter: ["style"]});
        });
    })();
</script>
