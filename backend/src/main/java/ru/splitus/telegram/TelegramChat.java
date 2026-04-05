package ru.splitus.telegram;

/**
 * Represents telegram chat.
 */
public class TelegramChat {

    private Long id;
    private String type;

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
     * Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Updates the type.
     */
    public void setType(String type) {
        this.type = type;
    }
}




