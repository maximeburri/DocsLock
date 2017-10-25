package ch.burci.docslock.controllers;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ch.burci.docslock.R;
import ch.burci.docslock.adapters.ListPDFAdapter;
import ch.burci.docslock.models.MainModel;

/**
 * Created by ciccius on 13/03/17.
 */

public class ListPDFFragment extends Fragment {

    private View rootView;
    private MainModel mainModel;
    private ListPDFAdapter listPdfAdapter;
    private ListView listViewPDF;
    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.liste_pdf_fragment, container, false);
        this.listViewPDF = (ListView) this.rootView.findViewById(R.id.listPDF);
        this.mainActivity = (MainActivity) inflater.getContext();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    /***
     * @desc Method to update the view (fragment)
     */
    public void update()
    {
        try {
            //recup mainModel to arguments of bundle
            this.mainModel = ((MainActivity) getActivity()).getMainModel();

            //creat adapter for list
            this.listPdfAdapter = new ListPDFAdapter(this.mainModel, this.getActivity());

            //add adapter to list
            this.listViewPDF.setAdapter(this.listPdfAdapter);

            this.listViewPDF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    ListPDFAdapter.PDFbox pdf = (ListPDFAdapter.PDFbox) view.getTag();
                    mainActivity.goToPdf((String) pdf.path);
                }
            });

        }catch(Exception e){}
    }

}
