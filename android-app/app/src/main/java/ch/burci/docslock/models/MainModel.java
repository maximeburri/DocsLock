package ch.burci.docslock.models;

import java.util.ArrayList;

/**
 * Created by ciccius on 13/03/17.
 */

public class MainModel {

    // ---------------------------------------------------------------
    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    private ArrayList<PDFModel> pdfs;

    // ---------------------------------------------------------------
    // Constructeur  -------------------------------------------------
    // ---------------------------------------------------------------
    public MainModel()
    {
        this.pdfs = new ArrayList<>();
    }

    /*Getter/Setter*/
    public ArrayList<PDFModel> getPdfs() {
        return pdfs;
    }

    public void setPdfs(ArrayList<PDFModel> pdfs) {
        this.pdfs = pdfs;
    }
}
