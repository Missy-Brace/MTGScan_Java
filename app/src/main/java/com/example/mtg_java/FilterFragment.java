package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import com.example.mtg_java.SearchResultActivity;


public class FilterFragment extends Fragment {

    EditText fName, fText, fType, fArtist, fMana;
    ToggleButton cW, cU, cB, cR, cG;
    ToggleButton idW, idU, idB, idR, idG;
    ToggleButton rCommon, rUncommon, rRare, rMythic;
    TextView tvSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_filter, container, false);

        fName = v.findViewById(R.id.fName);
        fText = v.findViewById(R.id.fText);
        fType = v.findViewById(R.id.fType);
        fArtist = v.findViewById(R.id.fArtist);
        fMana = v.findViewById(R.id.fMana);

        cW = v.findViewById(R.id.cW);
        cU = v.findViewById(R.id.cU);
        cB = v.findViewById(R.id.cB);
        cR = v.findViewById(R.id.cR);
        cG = v.findViewById(R.id.cG);

        idW = v.findViewById(R.id.idW);
        idU = v.findViewById(R.id.idU);
        idB = v.findViewById(R.id.idB);
        idR = v.findViewById(R.id.idR);
        idG = v.findViewById(R.id.idG);

        rCommon = v.findViewById(R.id.rCommon);
        rUncommon = v.findViewById(R.id.rUncommon);
        rRare = v.findViewById(R.id.rRare);
        rMythic = v.findViewById(R.id.rMythic);
        v.findViewById(R.id.btnClose).setOnClickListener(b -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        v.findViewById(R.id.btnSearch).setOnClickListener(b -> {
            Intent i = new Intent(getActivity(), SearchResultActivity.class);

            i.putExtra("name", fName.getText().toString());
            i.putExtra("text", fText.getText().toString());
            i.putExtra("type", fType.getText().toString());
            i.putExtra("artist", fArtist.getText().toString());
            i.putExtra("mana", fMana.getText().toString());

            i.putStringArrayListExtra("colors", getColors());
            i.putStringArrayListExtra("colorIdentity", getColorIdentity());
            i.putExtra("rarity", getRarity());
            i.putExtra("summary", tvSummary.getText().toString());


            startActivity(i);
        });


        tvSummary = v.findViewById(R.id.tvSummary);

        View.OnClickListener updateListener = b -> updateSummary();
        TextWatcher textWatcher = new SimpleWatcher(this::updateSummary);

        cW.setOnClickListener(updateListener);
        cU.setOnClickListener(updateListener);
        cB.setOnClickListener(updateListener);
        cR.setOnClickListener(updateListener);
        cG.setOnClickListener(updateListener);

        idW.setOnClickListener(updateListener);
        idU.setOnClickListener(updateListener);
        idB.setOnClickListener(updateListener);
        idR.setOnClickListener(updateListener);
        idG.setOnClickListener(updateListener);

        rCommon.setOnClickListener(updateListener);
        rUncommon.setOnClickListener(updateListener);
        rRare.setOnClickListener(updateListener);
        rMythic.setOnClickListener(updateListener);

        fName.addTextChangedListener(textWatcher);
        fText.addTextChangedListener(textWatcher);
        fType.addTextChangedListener(textWatcher);
        fArtist.addTextChangedListener(textWatcher);
        fMana.addTextChangedListener(textWatcher);

        v.findViewById(R.id.btnClear).setOnClickListener(b -> {
            fName.setText("");
            fText.setText("");
            fType.setText("");
            fArtist.setText("");
            fMana.setText("");

            cW.setChecked(false);
            cU.setChecked(false);
            cB.setChecked(false);
            cR.setChecked(false);
            cG.setChecked(false);

            idW.setChecked(false);
            idU.setChecked(false);
            idB.setChecked(false);
            idR.setChecked(false);
            idG.setChecked(false);

            rCommon.setChecked(false);
            rUncommon.setChecked(false);
            rRare.setChecked(false);
            rMythic.setChecked(false);

            updateSummary();
        });

        updateSummary();
        return v;
    }

    private void updateSummary() {
        ArrayList<String> parts = new ArrayList<>();

        if (!fName.getText().toString().isEmpty())
            parts.add("Name: \"" + fName.getText() + "\"");

        if (!fText.getText().toString().isEmpty())
            parts.add("Text: \"" + fText.getText() + "\"");

        if (!fType.getText().toString().isEmpty())
            parts.add("Type: \"" + fType.getText() + "\"");

        if (!fArtist.getText().toString().isEmpty())
            parts.add("Artist: \"" + fArtist.getText() + "\"");

        if (!fMana.getText().toString().isEmpty())
            parts.add("Mana: " + fMana.getText());

        // Colors
        String colors =
                (cW.isChecked() ? "W" : "") +
                        (cU.isChecked() ? "U" : "") +
                        (cB.isChecked() ? "B" : "") +
                        (cR.isChecked() ? "R" : "") +
                        (cG.isChecked() ? "G" : "");

        if (!colors.isEmpty())
            parts.add("Colors: " + colors);

        // Color Identity
        String colorId =
                (idW.isChecked() ? "W" : "") +
                        (idU.isChecked() ? "U" : "") +
                        (idB.isChecked() ? "B" : "") +
                        (idR.isChecked() ? "R" : "") +
                        (idG.isChecked() ? "G" : "");

        if (!colorId.isEmpty())
            parts.add("Color ID: " + colorId);

        // Rarity
        String rarity = getRarity();
        if (rarity != null)
            parts.add("Rarity: " + rarity);

        if (parts.isEmpty())
            tvSummary.setText("No filters selected");
        else
            tvSummary.setText(String.join(" · ", parts));
    }

    private ArrayList<String> getColors() {
        ArrayList<String> list = new ArrayList<>();
        if (cW.isChecked()) list.add("W");
        if (cU.isChecked()) list.add("U");
        if (cB.isChecked()) list.add("B");
        if (cR.isChecked()) list.add("R");
        if (cG.isChecked()) list.add("G");
        return list;
    }

    private ArrayList<String> getColorIdentity() {
        ArrayList<String> list = new ArrayList<>();
        if (idW.isChecked()) list.add("W");
        if (idU.isChecked()) list.add("U");
        if (idB.isChecked()) list.add("B");
        if (idR.isChecked()) list.add("R");
        if (idG.isChecked()) list.add("G");
        return list;
    }

    private String getRarity() {
        if (rCommon.isChecked()) return "common";
        if (rUncommon.isChecked()) return "uncommon";
        if (rRare.isChecked()) return "rare";
        if (rMythic.isChecked()) return "mythic";
        return null;
    }


    static class SimpleWatcher implements TextWatcher {
        Runnable r;
        SimpleWatcher(Runnable r) { this.r = r; }
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        public void onTextChanged(CharSequence s, int a, int b, int c) { r.run(); }
        public void afterTextChanged(Editable e) {}
    }
}
