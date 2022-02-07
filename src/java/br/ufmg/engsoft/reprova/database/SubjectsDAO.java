package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Subject;

public class SubjectsDAO {
	protected static final Logger logger = LoggerFactory.getLogger(SubjectsDAO.class);
	protected final Json json;
	protected final MongoCollection<Document> collection;

	public SubjectsDAO(Mongo db, Json json) {
		if (db == null) {
			throw new IllegalArgumentException("db mustn't be null");
		}

		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.collection = db.getCollection("subjects");
		this.json = json;
	}

	protected Subject parseDoc(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("document mustn't be null");
		}
		var doc = document.toJson();
		logger.info("Fetched question: " + doc);
		try {
			var subject = json.parse(doc, Subject.class);
			return subject;
		} catch (Exception e) {
			logger.error("Invalid document in database!", e);
			throw new IllegalArgumentException(e);
		}
	}

	public Subject get(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}
		var subject = this.collection.find(eq(new ObjectId(id))).map(this::parseDoc).first();
		if (subject == null) {
			logger.info("No such subject " + id);
		}
		return subject;
	}

	public Collection<Subject> list() {
		var doc = this.collection.find();
		var result = new ArrayList<Subject>();
		doc.map(this::parseDoc).into(result);
		return result;
	}

	public boolean add(Subject subject) {
		if (subject == null) {
			throw new IllegalArgumentException("subject mustn't be null");
		}
		Document doc = new Document().append("name", subject.name).append("code", subject.code)
				.append("theme", subject.theme).append("description", subject.description);
		var id = subject.id;
		if (id == null) {
			this.collection.insertOne(doc);
			return true;
		} else {
			logger.warn("Subject exists with id " + subject.id);
			return false;
		}
	}
}
