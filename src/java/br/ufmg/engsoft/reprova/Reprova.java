package br.ufmg.engsoft.reprova;

import br.ufmg.engsoft.reprova.configuration.AuthorizerConfigFactory;
import br.ufmg.engsoft.reprova.database.*;
import br.ufmg.engsoft.reprova.routes.Setup;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.routes.api.Authorizer;
import org.pac4j.sparkjava.SecurityFilter;
import spark.template.mustache.MustacheTemplateEngine;

public class Reprova {

    private static final String JWT_SALT = "12345678901234567890123456789012";
    protected static final MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

    public static void main(String[] args) {
        var json = new Json();

        Mongo db;

        try {
            db = new Mongo("reprova");
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        var questionsDAO = new QuestionsDAO(db, json);

        var config = new AuthorizerConfigFactory(JWT_SALT, templateEngine).build();
        final var jwtFilter = new SecurityFilter(config, "ParameterClient");

        Environments envs = Environments.getInstance();

        UsersDAO usersDAO = null;
        Authorizer authorizer = null;
        if (envs.getEnableUserTypes()) {
            usersDAO = new UsersDAO(db, json);
            authorizer = new Authorizer(usersDAO);
        } else {
            authorizer = new Authorizer();
        }

        Setup.routes(json, questionsDAO, usersDAO, jwtFilter, templateEngine);
        Setup.authRoutes(templateEngine, authorizer);

        if (envs.getEnableAnswers()) {
            var answersDAO = new AnswersDAO(db, json);
            Setup.answerRoutes(json, answersDAO, templateEngine);
        }

        if (envs.getEnableQuestionnaires()) {
            var questionnairesDAO = new QuestionnairesDAO(db, json);
            Setup.questionnaireRoutes(json, questionnairesDAO, questionsDAO, templateEngine);
        }
    }
}
