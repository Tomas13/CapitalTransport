package kz.itsolutions.businformator.model;

/**
 * Created by jean on 7/12/2016.
 */

public class News {
    private String date, message;

    public News(String date, String message) {
        this.date = date;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
