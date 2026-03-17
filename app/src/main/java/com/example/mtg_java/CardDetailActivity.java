package com.example.mtg_java;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardFace;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.utils.SessionManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// FIX 1: Added isFinishing()/isDestroyed() guards in all Retrofit callbacks to
//         prevent callbacks from running against a destroyed Activity context.
// FIX 2: bindLegalities() now guards against re-adding chips if already populated,
//         preventing redundant inflation when showCard() is called more than once.
public class CardDetailActivity extends AppCompatActivity {

    private ImageView imgCard;
    private ProgressBar progress;

    private TextView txtBasic;
    private TextView txtTypeMana;
    private TextView txtRulings;
    private TextView txtPrints;

    private TextView txtUsd;
    private TextView txtFoil;
    private TextView txtEur;
    private TextView txtPriceAsOf;
    private View layoutPrice;

    private View layoutRulings;
    private View layoutPrints;

    private TextView txtStats;
    private TextView txtRules;
    private TextView txtFlavor;

    private ImageButton btnFlip;
    private MaterialToolbar toolbar;
    private View layoutStats;
    private View layoutRules;
    private View layoutFlavor;

    private Card card;
    private int faceIndex = 0;

    private SessionManager session;
    private GroupApiManager groupApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        session  = new SessionManager(this);
        groupApi = new GroupApiManager();

        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        imgCard     = findViewById(R.id.imgCard);
        progress    = findViewById(R.id.progress);
        txtBasic    = findViewById(R.id.txtBasic);
        txtTypeMana = findViewById(R.id.txtTypeMana);
        txtStats    = findViewById(R.id.txtStats);
        txtRules    = findViewById(R.id.txtRules);
        txtFlavor   = findViewById(R.id.txtFlavor);
        layoutStats = findViewById(R.id.layoutStats);
        layoutRules = findViewById(R.id.layoutRules);
        layoutFlavor= findViewById(R.id.layoutFlavor);
        txtUsd      = findViewById(R.id.txtUsd);
        txtFoil     = findViewById(R.id.txtFoil);
        txtEur      = findViewById(R.id.txtEur);
        txtPriceAsOf= findViewById(R.id.txtPriceAsOf);
        layoutPrice = findViewById(R.id.layoutPrice);
        btnFlip     = findViewById(R.id.btnFlip);

        String cardId = getIntent().getStringExtra("CARD_ID");
        if (cardId == null) {
            finish();
            return;
        }

        loadCard(cardId);
        btnFlip.setOnClickListener(v -> flipCard());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showAddToCollectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCard(String id) {
        progress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getCardDetail(id).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                // FIX: guard against destroyed activity before touching any views
                if (isFinishing() || isDestroyed()) return;
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    card = response.body();
                    showCard();
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return; // FIX
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void showCard() {
        toolbar.setTitle(card.getName());

        if (card.getImageUrl() != null) {
            Glide.with(this).load(card.getImageUrl()).into(imgCard);
        }

        txtBasic.setText(
                "Rarity - " + safe(card.getRarity()) + "\n" +
                "Set - " + safe(card.getSetId()) + "\n" +
                "Set Name - " + safe(card.getSetName()) + "\n" +
                "Language - " + safe(card.getLanguage()) + "\n" +
                "Released At - " + safe(card.getReleasedAt())
        );

        txtTypeMana.setText(
                "Type - " + safe(card.getType()) + "\n" +
                "Subtype - " + safe(card.getSubtype()) + "\n" +
                "Mana Cost - " + safe(card.getManaCost()) + "\n" +
                "Artist - " + safe(card.getArtist()) + "\n" +
                "Colors - " + list(card.getColors()) + "\n" +
                "Color Identity - " + list(card.getColorIdentity()) + "\n" +
                "Keywords - " + list(card.getKeywords())
        );

        if (card.getPower() != null && card.getToughness() != null) {
            txtStats.setText("Power - " + card.getPower() + "\nToughness - " + card.getToughness());
            layoutStats.setVisibility(View.VISIBLE);
        } else if (card.getLoyalty() != null) {
            txtStats.setText("Loyalty - " + card.getLoyalty());
            layoutStats.setVisibility(View.VISIBLE);
        } else if (card.getDefense() != null) {
            txtStats.setText("Defense - " + card.getDefense());
            layoutStats.setVisibility(View.VISIBLE);
        } else {
            layoutStats.setVisibility(View.GONE);
        }

        if (card.getText() == null || card.getText().isEmpty()) {
            layoutRules.setVisibility(View.GONE);
        } else {
            txtRules.setText(card.getText());
            layoutRules.setVisibility(View.VISIBLE);
        }

        if (card.getFlavorText() == null || card.getFlavorText().isEmpty()) {
            layoutFlavor.setVisibility(View.GONE);
        } else {
            txtFlavor.setText(card.getFlavorText());
            layoutFlavor.setVisibility(View.VISIBLE);
        }

        btnFlip.setVisibility(
                card.getFaces() != null && card.getFaces().size() == 2
                        ? View.VISIBLE
                        : View.GONE
        );

        bindLegalities(card);
        bindPrice(card);
    }

    private void flipCard() {
        if (card.getFaces() == null || card.getFaces().size() != 2) return;

        faceIndex = faceIndex == 0 ? 1 : 0;
        CardFace face = card.getFaces().get(faceIndex);

        toolbar.setTitle(face.getName());
        txtTypeMana.setText("Type - " + safe(face.getType()) + "\nMana Cost - " + safe(face.getManaCost()));
        txtRules.setText(safe(face.getText()));

        if (face.getPower() != null && face.getToughness() != null) {
            txtStats.setText("Power - " + face.getPower() + "\nToughness - " + face.getToughness());
        } else {
            txtStats.setText("-");
        }

        if (face.getImageUrl() != null) {
            Glide.with(this).load(face.getImageUrl()).into(imgCard);
        }
    }

    private String safe(String s)      { return s == null || s.isEmpty() ? "-" : s; }
    private String safeNum(Double d)   { return d == null ? "—" : String.format("%.2f", d); }
    private String list(List<String> l){ return l == null || l.isEmpty() ? "-" : String.join(", ", l); }

    private void showAddToCollectionDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_add_collection, null);
        sheet.setContentView(view);

        LinearLayout listLayout = view.findViewById(R.id.listCollections);
        EditText edtNew = view.findViewById(R.id.edtNewCollection);

        view.findViewById(R.id.btnCreateAdd).setOnClickListener(v -> {
            String name = edtNew.getText().toString().trim();
            if (!name.isEmpty()) {
                createGroupAndAddCard(name);
                sheet.dismiss();
            }
        });

        groupApi.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                // FIX: guard — the sheet may already be dismissed or activity destroyed
                if (isFinishing() || isDestroyed()) return;
                for (Group g : groups) {
                    TextView item = new TextView(CardDetailActivity.this);
                    item.setText(g.getName());
                    item.setTextSize(16);
                    item.setPadding(8, 16, 8, 16);
                    item.setTextColor(Color.WHITE);
                    item.setOnClickListener(v -> {
                        addCardToGroup(g);
                        sheet.dismiss();
                    });
                    listLayout.addView(item);
                }
            }

            @Override
            public void onError(String msg) {
                if (isFinishing() || isDestroyed()) return; // FIX
                Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        sheet.show();
    }

    private void createGroupAndAddCard(String name) {
        groupApi.createGroup(session, name, new GroupApiManager.ObjectCallback() {
            @Override
            public void onSuccess(Group g) {
                if (isFinishing() || isDestroyed()) return; // FIX
                addCardToGroup(g);
            }

            @Override
            public void onError(String msg) {
                if (isFinishing() || isDestroyed()) return; // FIX
                Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCardToGroup(Group group) {
        groupApi.addCard(
                session,
                group.getId(),
                card.getUniversalId(),
                new GroupApiManager.SimpleCallback() {
                    @Override
                    public void onDone() {
                        if (isFinishing() || isDestroyed()) return; // FIX
                        Toast.makeText(
                                CardDetailActivity.this,
                                "Added to " + group.getName(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String msg) {
                        if (isFinishing() || isDestroyed()) return; // FIX
                        Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void bindLegalities(Card card) {
        FlexboxLayout layoutLegal    = findViewById(R.id.layoutLegal);
        FlexboxLayout layoutNotLegal = findViewById(R.id.layoutNotLegal);

        // FIX: only build chips once — re-adding them on every showCard() call
        // (e.g. triggered by face-flip) inflated unnecessary views every time.
        if (layoutLegal.getChildCount() > 0 || layoutNotLegal.getChildCount() > 0) return;

        List<String> legalFormats = card.getLegalFormats();
        if (legalFormats != null) {
            for (String format : legalFormats) {
                layoutLegal.addView(createChip(format));
            }
        }

        List<String> notLegalFormats = card.getNotLegalFormats();
        if (notLegalFormats != null) {
            for (String format : notLegalFormats) {
                layoutNotLegal.addView(createChip(format));
            }
        }
    }

    private void bindPrice(Card card) {
        if (card.getCurrentPrice() == null) {
            layoutPrice.setVisibility(View.GONE);
            return;
        }
        Card.CurrentPrice p = card.getCurrentPrice();
        txtUsd.setText("USD\n$" + safeNum(p.getUsd()));
        txtFoil.setText("Foil\n$" + safeNum(p.getUsdFoil()));
        txtEur.setText("EUR\n€" + safeNum(p.getEur()));
        txtPriceAsOf.setText("As of: " + (p.getAsOf() != null ? p.getAsOf() : "—"));
    }

    private TextView createChip(String text) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(Color.WHITE);
        chip.setBackgroundResource(R.drawable.bg_chips);

        FlexboxLayout.LayoutParams params =
                new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(5, 5, 5, 5);
        chip.setLayoutParams(params);
        return chip;
    }
}
