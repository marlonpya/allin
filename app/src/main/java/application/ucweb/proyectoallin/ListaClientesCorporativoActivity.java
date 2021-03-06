package application.ucweb.proyectoallin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import application.ucweb.proyectoallin.adapter.ListaClientesCorporativoAdapter;
import application.ucweb.proyectoallin.adapter.ListaClientesCorporativoRAdapter;
import application.ucweb.proyectoallin.aplicacion.BaseActivity;
import application.ucweb.proyectoallin.aplicacion.Configuracion;
import application.ucweb.proyectoallin.interfaz.IActividad;
import application.ucweb.proyectoallin.model.Corporativo;
import application.ucweb.proyectoallin.model.EnLista;
import application.ucweb.proyectoallin.model.ItemSimple;
import application.ucweb.proyectoallin.util.Constantes;
import butterknife.BindView;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ListaClientesCorporativoActivity extends BaseActivity implements IActividad{
    public static final String TAG = ListaClientesCorporativoActivity.class.getSimpleName();

    @BindView(R.id.rrv_clientes_corporativo) RecyclerView recyclerView;
    @BindView(R.id.sontoolbar) Toolbar toolbar;
    @BindView(R.id.tvTituloSonToolbar) ImageView icono_toolbar;
    @BindView(R.id.fondo_lista_clientes_corporativo) ImageView fondo;
    //private Realm realm;
    //private ListaClientesCorporativoRAdapter adapter;
    //private RealmResults<EnLista> fechas;
    private ProgressDialog pDialog;
    private ArrayList<ItemSimple> fechas = new ArrayList<>();
    private ListaClientesCorporativoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clientes_corporativo);
        iniciarLayout();
        iniciarPDialog();
        requestFechasLocal();
        //iniciarRRV();
        //if (EnLista.getUltimoId() == 0) EnLista.cargarData();
    }

    private void iniciarRV() {
        //realm = Realm.getDefaultInstance();
        //fechas = realm.where(EnLista.class).findAll().sort(EnLista.ID, Sort.DESCENDING);
        adapter = new ListaClientesCorporativoAdapter(this, fechas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void requestFechasLocal() {
        showDialog(pDialog);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constantes.FECHAS_LOCAL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")){
                                JSONArray jArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jArray.length(); i++) {
                                    ItemSimple fecha = new ItemSimple();
                                    fecha.setId(jArray.getJSONObject(i).getInt("LIS_LOC_ID"));
                                    fecha.setTitulo(jArray.getJSONObject(i).getString("LIS_LOC_FECHA"));
                                    fechas.add(fecha);
                                }
                                iniciarRV();
                            }else {
                                new AlertDialog.Builder(ListaClientesCorporativoActivity.this)
                                        .setTitle(R.string.app_name)
                                        .setMessage(getString(R.string.usuarios_not_found))
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                onBackPressed();
                                            }
                                        })
                                        .show();
                            }

                            Log.d(TAG, jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString(), e);
                        }
                        hidepDialog(pDialog);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(error.toString(), error);
                        hidepDialog(pDialog);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(Corporativo.getCorporativo().getId_local()));
                return params;
            }
        };
        Configuracion.getInstance().addToRequestQueue(request, TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isSesion() {
        return Corporativo.getCorporativo().isSesion();
    }

    @Override
    public void iniciarLayout() {
        setToolbarSon(toolbar, this, icono_toolbar);
        setFondoActivity(this, fondo);
    }

    @Override
    public void iniciarPDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage(getString(R.string.enviando_peticion));
    }
}
