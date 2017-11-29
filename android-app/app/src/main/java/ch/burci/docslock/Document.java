package ch.burci.docslock;

/**
 * Created by maxime on 21.11.17.
 */

public class Document {
    private String id;
    private String filename;

    public Document(){
    }

    public String getId() {
        return this.id;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getDownloadLink(){
        return DocsLockService.getDownloadLinkDocument(this);
    }

    @Override
    public boolean equals(Object v) {
        // Return if id are equals
        return (v instanceof Document) && ((Document)v).id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(this.id);
    }
}
