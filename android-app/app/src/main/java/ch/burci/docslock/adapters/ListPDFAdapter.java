package ch.burci.docslock.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ciccius on 13/03/17.
 */
import java.util.ArrayList;

import ch.burci.docslock.R;
import ch.burci.docslock.controllers.MainActivity;
import ch.burci.docslock.controllers.ViewerFragment;
import ch.burci.docslock.models.MainModel;
import ch.burci.docslock.models.PDFModel;

public class ListPDFAdapter extends BaseAdapter {

    public static class PDFbox {
        public ImageView imgPDF;
        public TextView txtPDFName;
        public Integer positionBox;
    }

    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    private ArrayList<PDFModel> pdfs;
    private Context context;
    private View view;
    private PDFbox pdfBox;
    private MainActivity mainActivity;

    @Override
    public int getCount() {
        return this.pdfs.size();
    }

    @Override
    public Object getItem(int position) {
        return this.pdfs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /***
     *
     * @param context
     */
    public ListPDFAdapter(MainModel mainModel, Context context) {
        this.context = context;
        this.pdfs = mainModel.getPdfs();
        this.mainActivity = (MainActivity) context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        this.view = convertView;
        final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //if view doesn't exist
        if(this.view == null) {
            // add element
            this.pdfBox= new PDFbox();
            this.view = inflater.inflate(R.layout.cell_list_pdf_fragment, parent, false);

            this.pdfBox.imgPDF = (ImageView) this.view.findViewById(R.id.imgPDF);
            this.pdfBox.txtPDFName = (TextView) this.view.findViewById(R.id.txtPDFName);

            this.pdfBox.positionBox = position;


            this.view.setTag(this.pdfBox);
        }else{

            this.pdfBox = (PDFbox) this.view.getTag();
        }

        if(position < pdfs.size()) {

            final PDFModel pdf = pdfs.get(position);

            //set values in component
            this.pdfBox.txtPDFName.setText(pdf.getPdfName());
            this.pdfBox.imgPDF.setImageResource(pdf.getIconRes());
        }

        return this.view;
    }

}
