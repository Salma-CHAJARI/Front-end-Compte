package ma.ensa.compteretrofit;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.ensa.compteretrofit.adapters.CompteAdapter;
import ma.ensa.compteretrofit.models.Compte;
import ma.ensa.compteretrofit.network.ApiInterface;
import ma.ensa.compteretrofit.service.RetrofitInstance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private boolean isXmlFormat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnAddCompte).setOnClickListener(v -> showAddCompteDialog());

        Spinner spinnerFormat = findViewById(R.id.spinner_format);
        spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    isXmlFormat = true;
                } else {
                    isXmlFormat = false;
                }
                fetchComptes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fetchComptes();
    }

    private void showAddCompteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Ajouter un compte");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);
        EditText editTextSolde = dialogView.findViewById(R.id.editTextSolde);
        EditText editTextDateCreation = dialogView.findViewById(R.id.editTextDateCreation);
        EditText editTextType = dialogView.findViewById(R.id.editTextType);

        builder.setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    double solde = Double.parseDouble(editTextSolde.getText().toString());
                    String dateCreation = editTextDateCreation.getText().toString();
                    String type = editTextType.getText().toString();

                    Compte newCompte = new Compte(null, solde, dateCreation, type);
                    addCompte(newCompte);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void addCompte(Compte compte) {
        ApiInterface api = RetrofitInstance.getInstance(isXmlFormat).create(ApiInterface.class);

        api.addCompte(compte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Compte ajouté avec succès", Toast.LENGTH_SHORT).show();
                    fetchComptes();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors de l'ajout du compte", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchComptes() {
        String acceptHeader = isXmlFormat ? "application/xml" : "application/json";
        ApiInterface api = RetrofitInstance.getInstance(isXmlFormat).create(ApiInterface.class);

        api.getAllComptes(acceptHeader).enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.body() != null) {
                    List<Compte> comptes = response.body();
                    CompteAdapter adapter = new CompteAdapter(comptes, MainActivity.this);

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            Compte compte = comptes.get(position);
                            if (direction == ItemTouchHelper.LEFT) {
                                deleteCompte(compte.getId());
                            } else if (direction == ItemTouchHelper.RIGHT) {
                                updateCompte(compte.getId(), compte);
                            }
                        }

                        @Override
                        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                if (dX > 0) {

                                    Paint paint = new Paint();
                                    paint.setColor(Color.GREEN);
                                    c.drawRect(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), dX, viewHolder.itemView.getBottom(), paint);
                                } else {

                                    Paint paint = new Paint();
                                    paint.setColor(Color.RED);
                                    c.drawRect(viewHolder.itemView.getRight() + dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom(), paint);
                                }
                            }
                        }
                    });

                    itemTouchHelper.attachToRecyclerView(recyclerView);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                Log.e("fetchComptes", "Erreur : " + t.getMessage());
            }
        });
    }


    public void updateCompte(Long id, Compte compte) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Modifier le compte");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_compte, null);
        EditText editTextSolde = dialogView.findViewById(R.id.editTextSolde);
        EditText editTextDateCreation = dialogView.findViewById(R.id.editTextDateCreation);
        EditText editTextType = dialogView.findViewById(R.id.editTextType);

        editTextSolde.setText(String.valueOf(compte.getSolde()));
        editTextDateCreation.setText(compte.getDateCreation());
        editTextType.setText(compte.getType());

        builder.setView(dialogView)
                .setPositiveButton("Mettre à jour", (dialog, which) -> {
                    double newSolde = Double.parseDouble(editTextSolde.getText().toString());
                    String newDateCreation = editTextDateCreation.getText().toString();
                    String newType = editTextType.getText().toString();

                    Compte updatedCompte = new Compte(id, newSolde, newDateCreation, newType);
                    updateCompteInDatabase(id, updatedCompte);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void updateCompteInDatabase(Long id, Compte updatedCompte) {
        ApiInterface api = RetrofitInstance.getInstance(isXmlFormat).create(ApiInterface.class);

        api.updateCompte(id, updatedCompte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Compte mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    fetchComptes();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                Log.e(TAG, "Erreur lors de la mise à jour du compte : " + t.getMessage());
                Toast.makeText(MainActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void deleteCompte(Long id) {
        ApiInterface api = RetrofitInstance.getInstance(isXmlFormat).create(ApiInterface.class);
        api.deleteCompte(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show();
                    fetchComptes();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Erreur lors de la suppression du compte : " + t.getMessage());
                Toast.makeText(MainActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
