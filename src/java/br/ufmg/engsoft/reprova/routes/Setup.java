package br.ufmg.engsoft.reprova.routes;

import br.ufmg.engsoft.reprova.database.UsersDAO;
import br.ufmg.engsoft.reprova.routes.api.Authorizer;
import org.pac4j.sparkjava.SecurityFilter;
import spark.Spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.AnswersDAO;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.routes.api.Answers;
import br.ufmg.engsoft.reprova.routes.api.Questions;
import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.routes.api.Questionnaires;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import spark.template.mustache.MustacheTemplateEngine;

import static spark.Spark.before;

/**
 * Service setup class.
 * This class is static.
 */
public class Setup {
    /**
     * Static class.
     */
    protected Setup() {
    }

    /**
     * Logger instance.
     */
    protected static Logger logger = LoggerFactory.getLogger(Setup.class);

    /**
     * The port for the webserver.
     */
    protected static final int port = Environments.getInstance().getPort();

    /**
     * Setup the service routes.
     * This sets up the routes under the routes directory,
     * and also static files on '/public'.
     *
     * @param json           the json formatter
     * @param questionsDAO   the DAO for Question
     * @param jwtFilter
     * @param templateEngine
     * @throws IllegalArgumentException if any parameter is null
     */
    public static void routes(Json json, QuestionsDAO questionsDAO, UsersDAO usersDAO, SecurityFilter jwtFilter,
                              MustacheTemplateEngine templateEngine) {
        if (json == null) {
            throw new IllegalArgumentException("json mustn't be null");
        }

        if (questionsDAO == null) {
            throw new IllegalArgumentException("questionsDAO mustn't be null");
        }

        Spark.port(Setup.port);
        logger.info("Spark on port " + Setup.port);

        logger.info("Setting up static resources.");
        Spark.staticFiles.location("/public");
        before("/api/*", jwtFilter);

        logger.info("Setting up questions route:");
        var questions = new Questions(json, questionsDAO);
        questions.setup(templateEngine);

        if (usersDAO != null) {
            logger.info("Setting up login route:");
            var loginRoute = new LoginRoutes(json, usersDAO);
            loginRoute.setup(templateEngine);
        }
    }

    public static void answerRoutes(Json json, AnswersDAO answersDAO, MustacheTemplateEngine templateEngine) {
        logger.info("Setting up answers route:");
        if (answersDAO == null) {
            throw new IllegalArgumentException("answersDAO mustn't be null");
        }
        var answers = new Answers(json, answersDAO);
        answers.setup(templateEngine);
    }

    public static void questionnaireRoutes(Json json, QuestionnairesDAO questionnairesDAO, QuestionsDAO questionsDAO,
                                           MustacheTemplateEngine templateEngine) {
        logger.info("Setting up questionnaires route:");
        if (questionnairesDAO == null) {
            throw new IllegalArgumentException("questionnairesDAO mustn't be null");
        }
        var questionnaires = new Questionnaires(json, questionnairesDAO, questionsDAO);
        questionnaires.setup(templateEngine);
    }

    public static void authRoutes(MustacheTemplateEngine templateEngine, Authorizer authorizer) {
        logger.info("Setting up auth route:");
        authorizer.setup(templateEngine);
    }
}
