package ch.burci.docslock.models;

/**
 * Created by ciccius on 13/03/17.
 */

public class PDFModel {
    // ---------------------------------------------------------------
    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    Integer iconRes;
    String pdfName;
    String pdfPath;


    public PDFModel(Integer iconRes, String pdfName, String pdfPath){
        this.iconRes = iconRes;
        this.pdfName = pdfName;
        this.pdfPath = pdfPath;
    }

    public Integer getIconRes() {
        return iconRes;
    }

    public void setIconRes(Integer iconRes) {
        this.iconRes = iconRes;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getPdfPath() { return pdfPath;}

    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath;}
}
