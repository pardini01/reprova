package br.ufmg.engsoft.reprova.routes;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.database.UsersDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import br.ufmg.engsoft.reprova.routes.api.Questions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

public class LoginRoutes extends ReprovaRoute {
    /**
     * Logger instance.
     */
    protected static final Logger logger = LoggerFactory.getLogger(Questions.class);


    /**
     * Json formatter.
     */
    protected final Json json;
    /**
     * DAO for Question.
     */
    protected final UsersDAO usersDAO;

    /**
     * Instantiate the questions endpoint.
     * The setup method must be called to install the endpoint.
     * @param json          the json formatter
     * @param usersDAO  the DAO for User
     * @throws IllegalArgumentException  if any parameter is null
     */
    public LoginRoutes(Json json, UsersDAO usersDAO) {
        if (json == null) {
            throw new IllegalArgumentException("json mustn't be null");
        }

        if (usersDAO == null) {
            throw new IllegalArgumentException("usersDAO mustn't be null");
        }

        this.json = json;
        this.usersDAO = usersDAO;
    }


    /**
     * Install the endpoint in Spark.
     * Methods:
     * - get
     * - post
     * - delete
     * @param templateEngine
     */
    public void setup(MustacheTemplateEngine templateEngine) {
        Spark.get("/login", this::get, templateEngine);
        Spark.post("/login", this::post);
        logger.info("Setup /login.");
    }

    private ModelAndView post(Request request, Response response) {
        return null;
    }

    /**
     * Get endpoint: lists all questions, or a single question if a 'id' query parameter is
     * provided.
     */
    private ModelAndView get(Request request, Response response) {
        logger.info("Received login get:");

        return null;
    }
}
