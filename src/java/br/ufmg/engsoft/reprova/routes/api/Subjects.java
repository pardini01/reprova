package br.ufmg.engsoft.reprova.routes.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.SubjectsDAO;
import br.ufmg.engsoft.reprova.model.Subject;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

public class Subjects extends ReprovaRoute {
	protected static final Logger logger = LoggerFactory.getLogger(Subjects.class);

	protected final Json json;

	protected final SubjectsDAO subjectsDAO;

	public Subjects(Json json, SubjectsDAO subjectsDAO) {
		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.json = json;
		this.subjectsDAO = subjectsDAO;
	}

	public void setup(MustacheTemplateEngine templateEngine) {
		Spark.get("/api/subjects", this::get, templateEngine);
		Spark.post("/api/subjects", this::post);
		logger.info("Setup /api/subjects");
	}

	private ModelAndView get(Request request, Response response) {
		logger.info("Received GET subjects");
		response.type("application/json");
		response.status(200);
		var subjects = subjectsDAO.list();

		final Map map = new HashMap();
		map.put("response", json.render(subjects));
		return new ModelAndView(map, "response.mustache");
	}

	private Object post(Request request, Response response) {
		String body = request.body();

		logger.info("Received subjects post:" + body);

		response.type("application/json");

		Subject subject;
		try {
			subject = json.parse(body, Subject.class);
		} catch (Exception e) {
			logger.error("Invalid request payload!", e);
			response.status(400);
			return new ModelAndView(new HashMap<>(), "error400.mustache");
		}

		logger.info("Parsed " + subject.name);
		var success = subjectsDAO.add(subject);
		response.status(success ? 200 : 400);

		logger.info("Done. Responding...");

		return ok;
	}
}