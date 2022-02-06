package br.ufmg.engsoft.reprova.model;

public class User {
    /**
     * Unique id of the user
     */
    private String id;

    /**
     * username for this user. Mustn't be null nor empty.
     */
    private final String username;

    /**
     * Password for this user. Mustn't be null nor empty.
     */
    private final String password;

    /**
     * Type of this user
     */
    private final UserType type;

    public String getId() {
        return this.id;
    }

    /**
     * Protected constructor, should only be used by the builder.
     */
    protected User(String id, String username, String password, UserType type) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserType getType() {
        return type;
    }

    public static class Builder {
        protected String id;
        protected String username;
        protected String password;
        protected UserType type;

        public User.Builder id(String id) {
            this.id = id;
            return this;
        }

        public User.Builder username(String username) {
            this.username = username;
            return this;
        }

        public User.Builder password(String password) {
            this.password = password;
            return this;
        }

        public User.Builder type(UserType type) {
            this.type = type;
            return this;
        }

        public User build() {
            return new User(
                    this.id,
                    this.username,
                    this.password,
                    this.type
            );
        }
    }
}
