package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Collection;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaClass;

public class ReprovaClassesDAO {
	protected static final Logger logger = LoggerFactory.getLogger(SubjectsDAO.class);
	protected final Json json;
	protected final MongoCollection<Document> collection;

	public ReprovaClassesDAO(Mongo db, Json json) {
		if (db == null) {
			throw new IllegalArgumentException("db mustn't be null");
		}

		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.collection = db.getCollection("classes");
		this.json = json;
	}

	protected ReprovaClass parseDoc(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("document mustn't be null");
		}
		var doc = document.toJson();
		logger.info("Fetched class: " + doc);
		try {
			var myclass = json.parse(doc, ReprovaClass.class);
			return myclass;
		} catch (Exception e) {
			logger.error("Invalid document in database!", e);
			throw new IllegalArgumentException(e);
		}
	}

	public ReprovaClass get(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}
		var subject = this.collection.find(eq(new ObjectId(id))).map(this::parseDoc).first();
		if (subject == null) {
			logger.info("No such class " + id);
		}
		return subject;
	}

	public Collection<ReprovaClass> list() {
		var doc = this.collection.find();
		var result = new ArrayList<ReprovaClass>();
		doc.map(this::parseDoc).into(result);
		return result;
	}

	public boolean add(ReprovaClass myclass) {
		if (myclass == null) {
			throw new IllegalArgumentException("subject mustn't be null");
		}
		Document doc = new Document().append("code", myclass.code).append("subject", myclass.subject).append("semester",
				myclass.semester);
		var id = myclass.id;
		if (id == null) {
			this.collection.insertOne(doc);
			return true;
		} else {
			logger.warn("Subject exists with id " + myclass.id);
			return false;
		}
	}
}
