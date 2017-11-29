package ch.burci.docslock;

import java.util.ArrayList;

/**
 * Created by maxime on 11/09/17.
 */

public class Group {
    private String id;
    private String name;
    private boolean isLocked;
    private ArrayList<Document> documents;

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

    public ArrayList<Document> getDocuments(){
        return this.documents;
    }
}
