package ch.burci.docslock;

/**
 * Created by maxime on 11/09/17.
 */

public class Group {
    private String id;
    private String name;
    private boolean isLocked;

    public Group() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
