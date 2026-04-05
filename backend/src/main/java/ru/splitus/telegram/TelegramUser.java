package ru.splitus.telegram;

/**
 * Represents telegram user.
 */
public class TelegramUser {

    private Long id;
    private String username;
    private String firstName;

    /**
     * Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Updates the id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Updates the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates the first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}




