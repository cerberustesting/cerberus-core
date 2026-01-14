<!DOCTYPE html>
<html class="dark" lang="${realm.locale!'en'}">
<head>
    <meta charset="UTF-8">
    <title>${msg("loginTitle", realm.name)}</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Keycloak required -->
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico">

    <!-- Your custom CSS -->
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css">

    <!-- tsParticles -->
    <script src="${url.resourcesPath}/js/tsparticles.min.js"></script>
</head>
<body class="crb_body relative min-h-screen">

    <!-- tsParticles container -->
    <div id="tsparticles" class="absolute inset-0 z-0 pointer-events-none"></div>

    <!-- Login card -->
    <div class="relative z-10 flex items-center justify-center w-full min-h-screen crb_main">
        <div class="crb_card w-full md:w-1/2 p-6 flex flex-col md:flex-row items-center gap-8">

            <!-- Logo -->
            <div class="md:w-1/2 flex justify-center items-center">
                <img src="${url.resourcesPath}/img/logo.png"
                        class="logo-login"
                        alt="Cerberus / InValue">
            </div>

            <!-- Login form -->
            <div class="md:w-1/2">

                <form id="kc-form-login"
                      action="${url.loginAction}"
                      method="post"
                      class="space-y-4">

                    <!-- Title -->
                    <h2 class="text-2xl font-bold mb-8">${msg("doLogIn")}</h2>

                    <!-- Error messages -->
                    <#if message?has_content>
                        <div class="alert-error">
                            ${message.summary}
                        </div>
                    </#if>

                    <!-- Username -->
                    <div class="mb-4">
                        <label class="block mb-1">${msg("username")}:</label>
                        <div class="flex items-center border rounded">
                            <span class="px-2 text-gray-500 flex items-center justify-center">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M16 14c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zM12 14v2a4 4 0 004 4h4a4 4 0 004-4v-2"/>
                                </svg>
                            </span>
                            <input id="username" name="username" class="flex-1 p-2 outline-none" placeholder="${msg("username")}" autofocus>
                        </div>
                    </div>

                    <!-- Password -->
                    <div class="mb-4">
                        <label class="block mb-1">${msg("password")}:</label>
                        <div class="flex items-center border rounded">
                            <span class="px-2 text-gray-500 flex items-center justify-center">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M12 11c1.657 0 3 1.343 3 3v3H9v-3c0-1.657 1.343-3 3-3zM7 11V8a5 5 0 0110 0v3"/>
                                    <rect x="7" y="14" width="10" height="6" rx="2" ry="2" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
                                </svg>
                            </span>
                            <input id="password" name="password" type="password" class="flex-1 p-2 outline-none" placeholder="${msg("password")}" autocomplete="current-password">
                        </div>
                    </div>

                    <!-- Remember me -->
                    <#if realm.rememberMe>
                        <div class="flex items-center gap-2">
                            <input type="checkbox"
                                    id="rememberMe"
                                    name="rememberMe"
                                    <#if login.rememberMe??>checked
                                    </#if>
                            />
                            <label for="rememberMe">
                                ${msg("rememberMe")}
                            </label>
                        </div>
                    </#if>

                    <!-- Submit button -->
                    <div class="flex justify-end">
                        <button type="submit" class="border rounded-lg bg-blue-600 hover:bg-blue-500 text-white px-4 py-2">${msg("doLogIn")}</button>
                    </div>

                    <!-- Forgot password -->
                    <#if realm.resetPasswordAllowed>
                        <div class="flex justify-end mt-2">
                            <a href="${url.loginResetCredentialsUrl}" class="text-blue-500 hover:underline">${msg("doForgotPassword")}</a>
                        </div>
                    </#if>
                </form>
            </div>
        </div>
    </div>
    <script>
    document.addEventListener("DOMContentLoaded", function () {
        tsParticles.load("tsparticles", {
            fullScreen: { enable: false },
            background: { color: { value: "transparent" } },
            particles: {
                number: { value: 80, density: { enable: true, area: 900 } },
                color: { value: "#3b82f6" },
                links: { enable: true, distance: 140, color: "#3b82f6", opacity: 0.35, width: 1 },
                move: { enable: true, speed: 0.4, outModes: { default: "bounce" } },
                size: { value: 2 },
                opacity: { value: 0.8 }
            },
            detectRetina: true
        });
    });
    </script>
</body>
</html>
