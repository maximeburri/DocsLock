package ch.burci.docslock.controllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

import ch.burci.docslock.R;


import static android.content.ContentValues.TAG;

/**
 * Created by ciccius on 13/03/17.
 */

public class ViewerFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener {

    private View rootView;
    private String pdfName;
    PDFView pdfView;
    public static final String DIRECTORY_PDF = "pdf";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.pdf_viewer_fragment, container, false);
        this.pdfView = (PDFView) this.rootView.findViewById(R.id.pdfView);
        displayFromAsset(DIRECTORY_PDF+"/"+this.pdfName);

        return rootView;

    }

    private void displayFromAsset(String assetFileName) {
        pdfView.fromAsset(assetFileName)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this.getContext()))
                .load();
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
    }


    // ---------------------------------------------------------------
    // Getter/Setter  ------------------------------------------------
    // ---------------------------------------------------------------
    public void setPDFName(String pdfName) { this.pdfName = pdfName; }

}
