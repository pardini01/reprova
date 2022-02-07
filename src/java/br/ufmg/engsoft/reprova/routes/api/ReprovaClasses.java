package br.ufmg.engsoft.reprova.routes.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.ReprovaClassesDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import br.ufmg.engsoft.reprova.model.ReprovaClass;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

public class ReprovaClasses extends ReprovaRoute {
	protected static final Logger logger = LoggerFactory.getLogger(Subjects.class);

	protected final Json json;
	
	protected final ReprovaClassesDAO classesDAO;
	
	public ReprovaClasses(Json json, ReprovaClassesDAO classesDAO) {
		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.json = json;
		this.classesDAO = classesDAO;
	}

	public void setup(MustacheTemplateEngine templateEngine) {
		Spark.get("/api/classes", this::get, templateEngine);
		Spark.post("/api/classes", this::post);
		logger.info("Setup /api/subjects");
	}
	
	private ModelAndView get(Request request, Response response) {
		logger.info("Received GET subjects");
		response.type("application/json");
		response.status(200);
		var classes = classesDAO.list();

		final Map map = new HashMap();
		map.put("response", json.render(classes));
		return new ModelAndView(map, "response.mustache");
	}

	private Object post(Request request, Response response) {
		String body = request.body();

		logger.info("Received classes post:" + body);

		response.type("application/json");

		ReprovaClass myclass;
		try {
			myclass = json.parse(body, ReprovaClass.class);
		} catch (Exception e) {
			logger.error("Invalid request payload!", e);
			response.status(400);
			return new ModelAndView(new HashMap<>(), "error400.mustache");
		}

		logger.info("Parsed " + myclass.code);
		var success = classesDAO.add(myclass);
		response.status(success ? 200 : 400);

		logger.info("Done. Responding...");

		return ok;
	}
}
