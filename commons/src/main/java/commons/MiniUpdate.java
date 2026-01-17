package commons;

public class MiniUpdate {
    private Long id;
    private String title;

    /**
     * A constructor for the MiniUpdate object - an Object package sent over
     * STOMP to reflect only small changes in recipes
     * @param id - the ID of the Object to be changed
     * @param title - the new title of the Object to be displayed
     */
    public MiniUpdate(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
