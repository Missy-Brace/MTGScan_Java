package com.example.mtg_java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

public class FilterFragment extends Fragment {

    EditText fName, fText, fType, fArtist, fMana;
    ToggleButton cW, cU, cB, cR, cG;
    ToggleButton idW, idU, idB, idR, idG;
    ToggleButton rCommon, rUncommon, rRare, rMythic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_filter, container, false);

        // text fields
        fName   = v.findViewById(R.id.fName);
        fText   = v.findViewById(R.id.fText);
        fType   = v.findViewById(R.id.fType);
        fArtist = v.findViewById(R.id.fArtist);
        fMana   = v.findViewById(R.id.fMana);

        // color
        cW = v.findViewById(R.id.cW);
        cU = v.findViewById(R.id.cU);
        cB = v.findViewById(R.id.cB);
        cR = v.findViewById(R.id.cR);
        cG = v.findViewById(R.id.cG);

        // color identity
        idW = v.findViewById(R.id.idW);
        idU = v.findViewById(R.id.idU);
        idB = v.findViewById(R.id.idB);
        idR = v.findViewById(R.id.idR);
        idG = v.findViewById(R.id.idG);

        // rarity
        rCommon   = v.findViewById(R.id.rCommon);
        rUncommon = v.findViewById(R.id.rUncommon);
        rRare     = v.findViewById(R.id.rRare);
        rMythic   = v.findViewById(R.id.rMythic);

        v.findViewById(R.id.btnApply).setOnClickListener(b -> {

            Bundle args = new Bundle();

            args.putString("name", fName.getText().toString());
            args.putString("text", fText.getText().toString());
            args.putString("type", fType.getText().toString());
            args.putString("artist", fArtist.getText().toString());
            args.putString("mana", fMana.getText().toString());

            args.putString("colors",
                    (cW.isChecked()?"W":"") +
                            (cU.isChecked()?"U":"") +
                            (cB.isChecked()?"B":"") +
                            (cR.isChecked()?"R":"") +
                            (cG.isChecked()?"G":""));

            args.putString("colorId",
                    (idW.isChecked()?"W":"") +
                            (idU.isChecked()?"U":"") +
                            (idB.isChecked()?"B":"") +
                            (idR.isChecked()?"R":"") +
                            (idG.isChecked()?"G":""));

            String rarity = "";
            if (rCommon.isChecked()) rarity = "Common";
            if (rUncommon.isChecked()) rarity = "Uncommon";
            if (rRare.isChecked()) rarity = "Rare";
            if (rMythic.isChecked()) rarity = "Mythic";
            args.putString("rarity", rarity);

            FilterResultFragment f = new FilterResultFragment();
            f.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, f)
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }
}
