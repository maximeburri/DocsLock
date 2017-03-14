package ch.burci.docslock.models;

import android.widget.ImageView;

/**
 * Created by ciccius on 13/03/17.
 */

public class PDFModel {
    // ---------------------------------------------------------------
    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    Integer iconRes;
    String pdfName;


    public PDFModel(Integer iconRes, String pdfName){
        this.iconRes = iconRes;
        this.pdfName = pdfName;
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
}
