package br.ufmg.engsoft.reprova.routes.api;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Authorizer {

    private static final String USER_ADMIN = "admin";
    private static final String USER_ADMIN_PASSWORD = "654321";

    private static final String JWT_SALT = "12345678901234567890123456789012";
    private static final int JWT_DURATION_5_MINUTES = 300000;

    protected static final Logger logger = LoggerFactory.getLogger(Authorizer.class);

    public void setup(MustacheTemplateEngine templateEngine) {
        Spark.get("/auth/jwt", this::jwt, templateEngine);
        logger.info("Setup /auth.");
    }

    private ModelAndView jwt(final Request request, final Response response) {
        var token = "";
        if (isValidUserRequest(request)) {
            CommonProfile commonProfile = createCommonProfile();
            token = generateTokenWithDuration(commonProfile);
        }
        response.type("application/json");
        final var map = new HashMap();
        map.put("token", token);
        return new ModelAndView(map, "jwt.mustache");
    }

    private String generateTokenWithDuration(CommonProfile commonProfile) {
        String token;
        var generator = new JwtGenerator(new SecretSignatureConfiguration(JWT_SALT));
        var dateNow = new Date();
        generator.setExpirationTime(new Date(dateNow.getTime()+ JWT_DURATION_5_MINUTES));
        token = generator.generate(commonProfile);
        return token;
    }

    private CommonProfile createCommonProfile() {
        var commonProfile = new CommonProfile();
        commonProfile.setId("admin");
        var roles = new LinkedHashSet();
        roles.add("ROLE_ADMIN");
        commonProfile.setRoles(roles);
        return commonProfile;
    }

    private boolean isValidUserRequest(Request request) {
        var user = request.headers("user");
        var password = request.headers("password");

        return USER_ADMIN.equals(user) && USER_ADMIN_PASSWORD.equals(password);
    }
}
