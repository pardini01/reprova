package br.ufmg.engsoft.reprova.database;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Answer;
import br.ufmg.engsoft.reprova.model.User;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

/**
 * DAO for User class on MongoDB
 */
public class UsersDAO {
    /**
     * Logger instance.
     */
    protected static final Logger logger = LoggerFactory.getLogger(UsersDAO.class);

    /**
     * Json formatter.
     */
    protected final Json json;

    /**
     * Questions collection.
     */
    protected final MongoCollection<Document> collection;

    /**
     * Basic constructor.
     * @param db    the database, mustn't be null
     * @param json  the json formatter for the database's documents, mustn't be null
     * @throws IllegalArgumentException  if any parameter is null
     */
    public UsersDAO(Mongo db, Json json) {
        if (db == null) {
            throw new IllegalArgumentException("db mustn't be null");
        }

        if (json == null) {
            throw new IllegalArgumentException("json mustn't be null");
        }

        this.collection = db.getCollection("users");
        this.json = json;
    }


    protected User parseDoc(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document mustn't be null");
        }

        var doc = document.toJson();

        logger.info("Fetched user: " + doc);

        try {
            var user = json
                    .parse(doc, User.Builder.class)
                    .build();

            logger.info("Parsed user: " + user);

            return user;
        }
        catch (Exception e) {
            logger.error("Invalid document in database!", e);
            throw new IllegalArgumentException(e);
        }
    }


    public User get(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id mustn't be null");
        }

        var user = this.collection
                .find(eq(new ObjectId(id)))
                .map(this::parseDoc)
                .first();

        if (user == null) {
            logger.info("No such user " + id);
        }

        return user;
    }

    public User getByUsernameAndPassword(String username, String password) {
        var filters =
                Arrays.asList(
                        username == null ? null : eq("username", username),
                        password == null ? null : eq("password", password)
                )
                        .stream()
                        .filter(Objects::nonNull) // mongo won't allow null filters.
                        .collect(Collectors.toList());

        var doc = filters.isEmpty() // mongo won't take null as a filter.
                ? this.collection.find()
                : this.collection.find(and(filters));

        var result = new ArrayList<User>();

        doc.projection(fields(exclude("statement")))
                .map(this::parseDoc)
                .into(result);

        return result.size() > 0 ? result.get(0) : null;
    }

    public boolean add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("answer mustn't be null");
        }

        Document doc = new Document()
                .append("username", user.getUsername())
                .append("password", user.getPassword())
                .append("type", user.getType());

        String id = user.getId();
        if (id != null) {
            var result = this.collection.replaceOne(
                    eq(new ObjectId(id)),
                    doc
            );

            if (!result.wasAcknowledged()) {
                logger.warn("Failed to replace user " + id);
                return false;
            }
        }
        else {
            this.collection.insertOne(doc);
        }

        logger.info("Stored user " + doc.get("_id"));

        return true;
    }

    public boolean remove(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id mustn't be null");
        }

        var result = this.collection.deleteOne(
                eq(new ObjectId(id))
        ).wasAcknowledged();

        if (result) {
            logger.info("Deleted user " + id);
        } else {
            logger.warn("Failed to delete user " + id);
        }

        return result;
    }
}
