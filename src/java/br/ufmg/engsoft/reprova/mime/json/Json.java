package br.ufmg.engsoft.reprova.mime.json;

import java.lang.reflect.Type;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import br.ufmg.engsoft.reprova.model.Answer;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Semester;
import br.ufmg.engsoft.reprova.model.Subject;
import br.ufmg.engsoft.reprova.model.ReprovaClass;


/**
 * Json format for Reprova's types.
 */
public class Json {
	protected static class ReprovaClassDeserializer implements JsonDeserializer<ReprovaClass> {
		@Override
		public ReprovaClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			var obj = json.getAsJsonObject();
			var code = obj.get("code").getAsString();
			var subject = obj.get("subject").getAsString();
			var semester = obj.get("semester").getAsString();
			return new ReprovaClass(code, subject, semester);
		}
	}

	protected static class SubjectDeserializer implements JsonDeserializer<Subject> {
		@Override
		public Subject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			Gson gson = new Gson();
			var obj = json.getAsJsonObject();
			var id = obj.get("_id").getAsJsonObject().get("$oid").getAsString();
			var name = obj.get("name").getAsString();
			var code = obj.get("code").getAsString();
			var themes = gson.fromJson(obj.get("themes").getAsJsonArray(), List.class);
			var description = obj.get("description").getAsString();
			return new Subject(id, name, code, themes, description);
		}
	}

  /**
   * Deserializer for Semester.
   */
  protected static class SemesterDeserializer implements JsonDeserializer<Semester> {
    /**
     * The semester format is:
     * "year/ref"
     * Where ref is 1 or 2.
     */
    @Override
    public Semester deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      String[] values = json.getAsString().split("/");

      if (values.length != 2) {
        throw new JsonParseException("invalid semester");
      }

      var year = Integer.parseInt(values[0]);

      var ref = Semester.Reference.fromInt(Integer.parseInt(values[1]));

      return new Semester(year, ref);
    }
  }


  /**
   * Deserializer for Question.Builder.
   */
  protected static class QuestionBuilderDeserializer
    implements JsonDeserializer<Question.Builder>
  {
    @Override
    public Question.Builder deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      var parserBuilder = new GsonBuilder();

      parserBuilder.registerTypeAdapter( // Question has a Semester field.
        Semester.class,
        new SemesterDeserializer()
      );

      var questionBuilder = parserBuilder
        .create()
        .fromJson(
          json.getAsJsonObject(),
          Question.Builder.class
        );

      // Mongo's id property doesn't match Question.id:
      var _id = json.getAsJsonObject().get("_id");

      if (_id != null) {
        questionBuilder.id(
          _id.getAsJsonObject()
            .get("$oid")
            .getAsString()
        );
      }

      return questionBuilder;
    }
  }
  
  /**
   * Deserializer for Answer.Builder.
   */
  protected static class AnswerBuilderDeserializer
    implements JsonDeserializer<Answer.Builder> {
    @Override
    public Answer.Builder deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      var parserBuilder = new GsonBuilder();


      var answerBuilder = parserBuilder
        .create()
        .fromJson(
          json.getAsJsonObject(),
          Answer.Builder.class
        );

        // Mongo's id property doesn't match Question.id:
        var _id = json.getAsJsonObject().get("_id");

        if (_id != null) {
          answerBuilder.id(
            _id.getAsJsonObject()
              .get("$oid")
              .getAsString()
          );
        }

        return answerBuilder;
      }
    }
  /**
   * Deserializer for Questionnaire.Builder.
   */
  protected static class QuestionnaireBuilderDeserializer
    implements JsonDeserializer<Questionnaire.Builder>
  {
    @Override
    public Questionnaire.Builder deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      var parserBuilder = new GsonBuilder();

      parserBuilder.registerTypeAdapter( // Questionnaire has Question fields.
        Question.Builder.class,
        new QuestionBuilderDeserializer()
      );
      
      parserBuilder.registerTypeAdapter( // Question has a Semester field.
        Semester.class,
        new SemesterDeserializer()
      );

      var questionnaireBuilder = parserBuilder
        .create()
        .fromJson(
          json.getAsJsonObject(),
          Questionnaire.Builder.class
        );

      // Mongo's id property doesn't match Questionnaire.id:
      var _id = json.getAsJsonObject().get("_id");

      if (_id != null)
        questionnaireBuilder.id(
          _id.getAsJsonObject()
            .get("$oid")
            .getAsString()
        );

      return questionnaireBuilder;
    }
  }

  /**
   * Deserializer for Questionnaire.Generator.
   */
  protected static class QuestionnaireGeneratorDeserializer
    implements JsonDeserializer<Questionnaire.Generator>
  {
    @Override
    public Questionnaire.Generator deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context
    ) {
      var parserBuilder = new GsonBuilder();

      parserBuilder.registerTypeAdapter( // Questionnaire has Question fields.
        Question.Builder.class,
        new QuestionBuilderDeserializer()
      );
      
      parserBuilder.registerTypeAdapter( // Question has a Semester field.
        Semester.class,
        new SemesterDeserializer()
      );

      var questionnaireGenerator = parserBuilder
        .create()
        .fromJson(
          json.getAsJsonObject(),
          Questionnaire.Generator.class
        );

      // Mongo's id property doesn't match Questionnaire.id:
      var _id = json.getAsJsonObject().get("_id");

      if (_id != null)
        questionnaireGenerator.id(
          _id.getAsJsonObject()
            .get("$oid")
            .getAsString()
        );

      return questionnaireGenerator;
    }
  }

  /**
   * The json formatter.
   */
  protected final Gson gson;

  /**
   * Instantiate the formatter for Reprova's types.
   * Currently, it supports only the Question type.
   */
  public Json() {
    var parserBuilder = new GsonBuilder().serializeSpecialFloatingPointValues();

    parserBuilder.registerTypeAdapter(
      Question.Builder.class,
      new QuestionBuilderDeserializer()
    );
    
    parserBuilder.registerTypeAdapter(
      Answer.Builder.class,
      new AnswerBuilderDeserializer()
    );

    parserBuilder.registerTypeAdapter(
      Questionnaire.Builder.class,
      new QuestionnaireBuilderDeserializer()
    );
    
    parserBuilder.registerTypeAdapter(
      Questionnaire.Generator.class,
      new QuestionnaireGeneratorDeserializer()
    );

    //parserBuilder.registerTypeAdapter(ReprovaClass.class, new ReprovaClassDeserializer());

    //parserBuilder.registerTypeAdapter(Subject.class, new SubjectDeserializer());

    this.gson = parserBuilder.create();
  }



  /**
   * Parse an object in the given class.
   * @throws JsonSyntaxException  if json is not a valid representation for the given class
   */
  public <T> T parse(String json, Class<T> cls) {
    return this.gson.fromJson(json, cls);
  }


  /**
   * Render an object of the given class.
   */
  public <T> String render(T obj) {
    return this.gson.toJson(obj);
  }
}
