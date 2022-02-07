package br.ufmg.engsoft.reprova.routes.api;

import br.ufmg.engsoft.reprova.database.UsersDAO;
import br.ufmg.engsoft.reprova.model.User;
import br.ufmg.engsoft.reprova.model.UserType;
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

import static spark.Spark.halt;

public class Authorizer {
    private static final String USER_ADMIN = "admin";
    private static final String USER_ADMIN_PASSWORD = "654321";
    private final UsersDAO usersDAO;

    private static final String JWT_SALT = "12345678901234567890123456789012";
    private static final int JWT_DURATION_5_MINUTES = 300000;

    protected static final Logger logger = LoggerFactory.getLogger(Authorizer.class);

    public Authorizer(UsersDAO usersDAO) {
        this.usersDAO = usersDAO;
    }

    public Authorizer() {
        this.usersDAO = null;
    }

    public void setup(MustacheTemplateEngine templateEngine) {
        Spark.get("/auth/jwt", this::jwt, templateEngine);
        logger.info("Setup /auth.");
    }

    private ModelAndView jwt(final Request request, final Response response) {
        var token = "";
        var user = getUserFromRequest(request);
        if (user != null) {
            CommonProfile commonProfile = createCommonProfile(user);
            token = generateTokenWithDuration(commonProfile);
        } else {
            halt(401);
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

    private CommonProfile createCommonProfile(User user) {
        var commonProfile = new CommonProfile();
        commonProfile.setId("admin");
        var roles = new LinkedHashSet();
        if (user.getType() == UserType.ADMIN) {
            roles.add("ROLE_ADMIN");
        } else if (user.getType() == UserType.TEACHER) {
            roles.add("ROLE_TEACHER");
        } else if (user.getType() == UserType.STUDENT) {
            roles.add("ROLE_STUDENT");
        }

        commonProfile.setRoles(roles);
        return commonProfile;
    }

    private User getUserFromRequest(Request request) {
        var user = request.headers("user");
        var password = request.headers("password");

        if (USER_ADMIN.equals(user) && USER_ADMIN_PASSWORD.equals(password)) {
            return new User.Builder().username("admin").type(UserType.ADMIN).build();
        }

        if (this.usersDAO != null) {
            var foundUser = this.usersDAO.getByUsernameAndPassword(user, password);
            if (foundUser != null) {
                return foundUser;
            }
        }

        return null;
    }
}
