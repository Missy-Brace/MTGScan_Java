package com.example.mtg_java;

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
import java.util.List;

// REFACTOR: FilterFragment no longer hard-codes a launch of SearchResultActivity.
// It now delivers results through a FilterListener callback interface, making it
// reusable in any context — the standalone browse flow and the collection add flow.
//
// Callers register a listener via setFilterListener() before the fragment is shown.
// If no listener is set the fragment behaves as before (standalone search).
//
// The "mode" argument (ARG_MODE) controls the label on the action button:
//   "search"     → "Search" → navigates to SearchResultActivity (original behaviour)
//   "collection" → "Apply"  → fires the listener callback with the selected params
public class FilterFragment extends Fragment {

    public static final String ARG_MODE       = "mode";
    public static final String MODE_SEARCH     = "search";
    public static final String MODE_COLLECTION = "collection";

    // ── Callback interface ─────────────────────────────────────────────────────

    public interface FilterListener {
        void onFiltersApplied(
                String name,
                String text,
                String type,
                String artist,
                String rarity,
                Integer manaCost,
                List<String> colors,
                List<String> colorIdentity
        );
    }

    private FilterListener filterListener;

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    // ── Factory helpers ────────────────────────────────────────────────────────

    public static FilterFragment forSearch() {
        FilterFragment f = new FilterFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, MODE_SEARCH);
        f.setArguments(b);
        return f;
    }

    public static FilterFragment forCollection(FilterListener listener) {
        FilterFragment f = new FilterFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, MODE_COLLECTION);
        f.setArguments(b);
        f.setFilterListener(listener);
        return f;
    }

    // ── Views ──────────────────────────────────────────────────────────────────

    EditText fName, fText, fType, fArtist, fMana;
    ToggleButton cW, cU, cB, cR, cG;
    ToggleButton idW, idU, idB, idR, idG;
    ToggleButton rCommon, rUncommon, rRare, rMythic;
    TextView tvSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_filter, container, false);

        fName   = v.findViewById(R.id.fName);
        fText   = v.findViewById(R.id.fText);
        fType   = v.findViewById(R.id.fType);
        fArtist = v.findViewById(R.id.fArtist);
        fMana   = v.findViewById(R.id.fMana);

        cW = v.findViewById(R.id.cW);  cU = v.findViewById(R.id.cU);
        cB = v.findViewById(R.id.cB);  cR = v.findViewById(R.id.cR);
        cG = v.findViewById(R.id.cG);

        idW = v.findViewById(R.id.idW); idU = v.findViewById(R.id.idU);
        idB = v.findViewById(R.id.idB); idR = v.findViewById(R.id.idR);
        idG = v.findViewById(R.id.idG);

        rCommon   = v.findViewById(R.id.rCommon);
        rUncommon = v.findViewById(R.id.rUncommon);
        rRare     = v.findViewById(R.id.rRare);
        rMythic   = v.findViewById(R.id.rMythic);
        tvSummary = v.findViewById(R.id.tvSummary);

        // ── Close ──────────────────────────────────────────────────────────────
        v.findViewById(R.id.btnClose).setOnClickListener(b ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // ── Action button — label and behaviour depend on mode ─────────────────
        String mode = getArguments() != null
                ? getArguments().getString(ARG_MODE, MODE_SEARCH)
                : MODE_SEARCH;

        android.widget.Button btnSearch = v.findViewById(R.id.btnSearch);
        btnSearch.setText(mode.equals(MODE_COLLECTION) ? "Apply" : "Search");

        btnSearch.setOnClickListener(b -> {
            if (mode.equals(MODE_COLLECTION)) {
                // Deliver params to the caller fragment via callback
                if (filterListener != null) {
                    String manaStr = fMana.getText().toString().trim();
                    Integer mana = manaStr.isEmpty() ? null : Integer.parseInt(manaStr);
                    filterListener.onFiltersApplied(
                            fName.getText().toString(),
                            fText.getText().toString(),
                            fType.getText().toString(),
                            fArtist.getText().toString(),
                            getRarity(),
                            mana,
                            getColors(),
                            getColorIdentity()
                    );
                }
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                // Original behaviour — launch SearchResultActivity
                android.content.Intent i =
                        new android.content.Intent(getActivity(), SearchResultActivity.class);
                i.putExtra("name",   fName.getText().toString());
                i.putExtra("text",   fText.getText().toString());
                i.putExtra("type",   fType.getText().toString());
                i.putExtra("artist", fArtist.getText().toString());
                i.putExtra("mana",   fMana.getText().toString());
                i.putStringArrayListExtra("colors",        getColors());
                i.putStringArrayListExtra("colorIdentity", getColorIdentity());
                i.putExtra("rarity",  getRarity());
                i.putExtra("summary", tvSummary.getText().toString());
                startActivity(i);
            }
        });

        // ── Clear ──────────────────────────────────────────────────────────────
        v.findViewById(R.id.btnClear).setOnClickListener(b -> {
            fName.setText("");  fText.setText("");
            fType.setText("");  fArtist.setText(""); fMana.setText("");
            for (ToggleButton tb : new ToggleButton[]{
                    cW,cU,cB,cR,cG, idW,idU,idB,idR,idG,
                    rCommon,rUncommon,rRare,rMythic}) {
                tb.setChecked(false);
            }
            updateSummary();
        });

        // ── Live summary ───────────────────────────────────────────────────────
        View.OnClickListener updateClick  = b -> updateSummary();
        TextWatcher          updateChange = new SimpleWatcher(this::updateSummary);

        for (ToggleButton tb : new ToggleButton[]{
                cW,cU,cB,cR,cG, idW,idU,idB,idR,idG,
                rCommon,rUncommon,rRare,rMythic}) {
            tb.setOnClickListener(updateClick);
        }
        for (EditText et : new EditText[]{fName, fText, fType, fArtist, fMana}) {
            et.addTextChangedListener(updateChange);
        }

        updateSummary();
        return v;
    }

    // ── Summary ────────────────────────────────────────────────────────────────

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

        String colors =
                (cW.isChecked() ? "W" : "") + (cU.isChecked() ? "U" : "") +
                (cB.isChecked() ? "B" : "") + (cR.isChecked() ? "R" : "") +
                (cG.isChecked() ? "G" : "");
        if (!colors.isEmpty()) parts.add("Colors: " + colors);

        String colorId =
                (idW.isChecked() ? "W" : "") + (idU.isChecked() ? "U" : "") +
                (idB.isChecked() ? "B" : "") + (idR.isChecked() ? "R" : "") +
                (idG.isChecked() ? "G" : "");
        if (!colorId.isEmpty()) parts.add("Color ID: " + colorId);

        String rarity = getRarity();
        if (rarity != null) parts.add("Rarity: " + rarity);

        tvSummary.setText(parts.isEmpty() ? "No filters selected" : String.join(" · ", parts));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ArrayList<String> getColors() {
        ArrayList<String> l = new ArrayList<>();
        if (cW.isChecked()) l.add("W"); if (cU.isChecked()) l.add("U");
        if (cB.isChecked()) l.add("B"); if (cR.isChecked()) l.add("R");
        if (cG.isChecked()) l.add("G");
        return l;
    }

    private ArrayList<String> getColorIdentity() {
        ArrayList<String> l = new ArrayList<>();
        if (idW.isChecked()) l.add("W"); if (idU.isChecked()) l.add("U");
        if (idB.isChecked()) l.add("B"); if (idR.isChecked()) l.add("R");
        if (idG.isChecked()) l.add("G");
        return l;
    }

    private String getRarity() {
        if (rCommon.isChecked())   return "common";
        if (rUncommon.isChecked()) return "uncommon";
        if (rRare.isChecked())     return "rare";
        if (rMythic.isChecked())   return "mythic";
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
