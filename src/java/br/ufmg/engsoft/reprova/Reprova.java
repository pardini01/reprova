package br.ufmg.engsoft.reprova;

import br.ufmg.engsoft.reprova.configuration.AuthorizerConfigFactory;
import br.ufmg.engsoft.reprova.database.AnswersDAO;
import br.ufmg.engsoft.reprova.database.Mongo;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.database.ReprovaClassesDAO;
import br.ufmg.engsoft.reprova.database.SubjectsDAO;
import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.routes.Setup;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
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

        Setup.routes(json, questionsDAO, jwtFilter, templateEngine);
        Setup.authRoutes(templateEngine);

        Environments envs = Environments.getInstance();

        if (envs.getEnableAnswers()) {
            var answersDAO = new AnswersDAO(db, json);
            Setup.answerRoutes(json, answersDAO, templateEngine);
        }

        if (envs.getEnableQuestionnaires()) {
            var questionnairesDAO = new QuestionnairesDAO(db, json);
            Setup.questionnaireRoutes(json, questionnairesDAO, questionsDAO, templateEngine);
        }

        var subjectsDAO = new SubjectsDAO(db, json);
        Setup.subjectsRoutes(json, templateEngine, subjectsDAO);

        var classesDAO = new ReprovaClassesDAO(db, json);
        Setup.reprovaClassesRoutes(json, templateEngine, classesDAO);
    }
}
