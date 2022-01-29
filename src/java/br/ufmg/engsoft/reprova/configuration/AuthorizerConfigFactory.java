package br.ufmg.engsoft.reprova.configuration;

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import spark.TemplateEngine;

public class AuthorizerConfigFactory implements ConfigFactory {

    private final String salt;

    private final TemplateEngine templateEngine;

    public AuthorizerConfigFactory(final String salt, final TemplateEngine templateEngine) {
        this.salt = salt;
        this.templateEngine = templateEngine;
    }

    @Override
    public Config build(final Object... parameters) {
        var parameterClient = new ParameterClient("token", new JwtAuthenticator(new SecretSignatureConfiguration(salt)));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        final var clients = new Clients("http://localhost:8080/callback", parameterClient);

        final var config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        //config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/auth"));
        config.setHttpActionAdapter(new AuthorizerHttpActionAdapter(templateEngine));
        return config;
    }
}
