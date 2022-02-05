package br.ufmg.engsoft.reprova.routes.api;

import spark.ModelAndView;
import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import br.ufmg.engsoft.reprova.mime.json.Json;
import spark.template.mustache.MustacheTemplateEngine;


/**
 * Questions route.
 */
public class Questions extends ReprovaRoute {
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
  protected final QuestionsDAO questionsDAO;

  /**
   * Instantiate the questions endpoint.
   * The setup method must be called to install the endpoint.
   * @param json          the json formatter
   * @param questionsDAO  the DAO for Question
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Questions(Json json, QuestionsDAO questionsDAO) {
    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }

    if (questionsDAO == null) {
      throw new IllegalArgumentException("questionsDAO mustn't be null");
    }

    this.json = json;
    this.questionsDAO = questionsDAO;
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
    Spark.get("/api/questions", this::get, templateEngine);
    Spark.post("/api/questions", this::post);
    Spark.delete("/api/questions", this::delete);
    Spark.delete("/api/questions/deleteAll", this::deleteAll);

    logger.info("Setup /api/questions.");
  }

  /**
   * Get endpoint: lists all questions, or a single question if a 'id' query parameter is
   * provided.
   */
  private ModelAndView get(Request request, Response response) {
    logger.info("Received questions get:");

    var id = request.queryParams("id");

    if (id == null) {
    	return get(request, response, true);
    }
     
    return get(request, response, id, true);
  }

  /**
   * Get id endpoint: fetch the specified question from the database.
   * If not authorised, and the given question is private, returns an error message.
   */
  private ModelAndView get(Request request, Response response, String id, boolean auth) {
    if (id == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }

    response.type("application/json");
    logger.info(String.format("Fetching question %s", id));

    var question = questionsDAO.get(id);

    if (question == null) {
      logger.error("Invalid request!");
      response.status(400);
      return new ModelAndView(new HashMap<>(), "error400.mustache");
    }

    logger.info("Done. Responding...");

    response.status(200);

    final Map map = new HashMap();
    map.put("response", json.render(question));
    return new ModelAndView(map, "response.mustache");
  }

  /**
   * Get all endpoint: fetch all questions from the database.
   * If not authorized, fetches only public questions.
   */
  private ModelAndView get(Request request, Response response, boolean auth) {
    response.type("application/json");

    logger.info("Fetching questions.");

    var questions = questionsDAO.list(
      null, // theme filtering is not implemented in this endpoint.
      auth ? null : false
    );

    logger.info("Done. Responding...");

    response.status(200);

    final Map map = new HashMap();
    map.put("response", json.render(questions));
    return new ModelAndView(map, "response.mustache");
  }


  /**
   * Post endpoint: add or update a question in the database.
   * The question must be supplied in the request's body.
   * If the question has an 'id' field, the operation is an update.
   * Otherwise, the given question is added as a new question in the database.
   * This endpoint is for authorized access only.
   */
  private Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received questions post:" + body);

    response.type("application/json");

    Question question;
    try {
      question = json
        .parse(body, Question.Builder.class)
        .build();
    }
    catch (Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return new ModelAndView(new HashMap<>(), "error400.mustache");
    }

    logger.info("Parsed " + question.toString());
    logger.info("Adding question.");

    var success = questionsDAO.add(question);

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return ok;
  }


  /**
   * Delete endpoint: remove a question from the database.
   * The question's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  private Object delete(Request request, Response response) {
    logger.info("Received questions delete:");

    response.type("application/json");

    var id = request.queryParams("id");

    if (id == null) {
      logger.error("Invalid request!");
      response.status(400);
      return new ModelAndView(new HashMap<>(), "error400.mustache");
    }

    logger.info("Deleting question " + id);

    var success = questionsDAO.remove(id);

    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }

  /**
   * Delete All endpoint: remove all questions from the database.
   * This endpoint is for authorized access only.
   */
  protected Object deleteAll(Request request, Response response) {
    logger.info("Received questions delete all:");

    response.type("application/json");

    boolean success = false;
    logger.info("Deleting all questions");
    ArrayList<Question> questions = new ArrayList<Question>(questionsDAO.list(null, null));
    for (Question question : questions){
      String id = question.id;
      logger.info("Deleting question " + id);
      
      success = questionsDAO.remove(id);
      if (!success){
        break;
      }
    }
      
    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }
}
