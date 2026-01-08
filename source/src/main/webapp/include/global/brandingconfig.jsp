<!--BRANDING CONTENT-->
<script>
    window.__CERBERUS_BRANDING__ = <%= application.getAttribute("brandingConfig") %>;
</script>
<%
    String favicon = (String) application.getAttribute("brandingFavicon");
    String faviconType = (String) application.getAttribute("brandingFaviconType");
    if (favicon == null) {
        favicon = "images/favicon.ico.png";
        faviconType = "image/png";
    }
%>
<link rel="icon" type="<%= faviconType %>" href="<%= favicon %>">