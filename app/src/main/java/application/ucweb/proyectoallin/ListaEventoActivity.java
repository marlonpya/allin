package application.ucweb.proyectoallin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import application.ucweb.proyectoallin.adapter.EventoAdapter;
import application.ucweb.proyectoallin.adapter.EventoEspecialRealmAdapter;
import application.ucweb.proyectoallin.aplicacion.BaseActivity;
import application.ucweb.proyectoallin.aplicacion.Configuracion;
import application.ucweb.proyectoallin.interfaz.IActividad;
import application.ucweb.proyectoallin.model.Establecimiento;
import application.ucweb.proyectoallin.model.Usuario;
import application.ucweb.proyectoallin.modelparseable.EventoSimple;
import application.ucweb.proyectoallin.util.ConexionBroadcastReceiver;
import application.ucweb.proyectoallin.util.Constantes;
import butterknife.BindView;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.internal.android.ISO8601Utils;

public class ListaEventoActivity extends BaseActivity implements IActividad{
    private static final String TAG = ListaEventoActivity.class.getSimpleName();
    @BindView(R.id.layout_activity_lista_evento) RelativeLayout layout;
    @BindView(R.id.sontoolbar) Toolbar toolbar;
    @BindView(R.id.tvTituloSonToolbar) ImageView icono_toolbar;
    @BindView(R.id.rv_lista_eventos) RecyclerView realmRecyclerView;
    @BindView(R.id.tvDescripcionToolbar)TextView tvTituloListaEvento;
    @BindView(R.id.idiv_layout_lista_evento)ImageView ivFondoListaEvento;
    private EventoAdapter adapter;
    private ArrayList<EventoSimple> lista_eventos = new ArrayList<>();
    private int id;
    private ProgressDialog pDialog;
    private String METHOD_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_evento);
        iniciarLayout();
        iniciarPDialog();
        tvTituloListaEvento.setText(getIntent().getStringExtra(Constantes.S_EVENTO_TOOLBAR));
        if (getIntent().hasExtra(Constantes.I_EVENTO_DIALOG)) {
            if (getIntent().getIntExtra(Constantes.I_EVENTO_DIALOG, -1)==Constantes.FILTRO_X_LOCAL){
                METHOD_URL=Constantes.EVENTOS_X_LOCAL;
                id = getIntent().getIntExtra(Constantes.ID_LOCAL, -1);
                getIntent().removeExtra(Constantes.I_EVENTO_DIALOG);
            }else{
                METHOD_URL=Constantes.EVENTOS;
                id = getIntent().getIntExtra(Constantes.I_EVENTO_DIALOG, -1);
                getIntent().removeExtra(Constantes.I_EVENTO_DIALOG);
            }
            if (ConexionBroadcastReceiver.isConect()) requestEventos();
            else ConexionBroadcastReceiver.showSnack(layout, this);
        }
        else if(getIntent().hasExtra(Constantes.FILTRO_EVENTO_ESPECIAL)){
            METHOD_URL=Constantes.EVENTOS;
            id = getIntent().getIntExtra(Constantes.FILTRO_EVENTO_ESPECIAL, -1);
            if (ConexionBroadcastReceiver.isConect()) requestEventos();
            else ConexionBroadcastReceiver.showSnack(layout, this);

        }
    }

    private void requestEventos() {
        showDialog(pDialog);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                METHOD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                JSONArray jEventos = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jEventos.length(); i++) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("es", "pe"));
                                    EventoSimple evento = new EventoSimple();
                                    evento.setId_server(jEventos.getJSONObject(i).getInt("EVE_ID"));
                                    evento.setNombre(jEventos.getJSONObject(i).getString("EVE_NOMBRE"));
                                    evento.setPrecio(Double.parseDouble(jEventos.getJSONObject(i).getString("EVE_PRECIO")));
                                    evento.setTipo(jEventos.getJSONObject(i).getInt("EVE_TIPO"));
                                    evento.setId_local(jEventos.getJSONObject(i).getInt("LOC_ID"));
                                    evento.setDescripcion(jEventos.getJSONObject(i).getString("EVE_DESCRIPCION"));
                                    evento.setImagen(jEventos.getJSONObject(i).getString("EVE_IMAGEN"));
                                    evento.setMapa(jEventos.getJSONObject(i).getString("EVE_MAPA"));
                                    evento.setRecomendado(jEventos.getJSONObject(i).getInt("EVE_RECOMENDADO")==1);
                                    if (evento.getId_local()!=0){
                                        evento.setNombre_local(jEventos.getJSONObject(i).getString("LOC_NOMBRE"));
                                        evento.setDireccion(jEventos.getJSONObject(i).getString("LOC_DIRECCION"));
                                        evento.setAforo(jEventos.getJSONObject(i).getInt("LOC_AFORO"));
                                        evento.setLatitud(Double.parseDouble(jEventos.getJSONObject(i).getString("LOC_LATITUD")));
                                        evento.setLongitud(Double.parseDouble(jEventos.getJSONObject(i).getString("LOC_LONGITUD")));
                                    }
                                    else {
                                        evento.setNombre_local("");
                                        evento.setDireccion("");
                                        evento.setAforo(0);
                                        evento.setLatitud(Double.parseDouble(jEventos.getJSONObject(i).getString("EVE_LATITUD")));
                                        evento.setLongitud(Double.parseDouble(jEventos.getJSONObject(i).getString("EVE_LONGITUD")));
                                    }
                                    try {
                                        evento.setFecha_inicio(sdf.parse(jEventos.getJSONObject(i).getString("EVE_FEC_INICIO")));
                                        evento.setFecha_fin(sdf.parse(jEventos.getJSONObject(i).getString("EVE_FEC_FIN")));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    lista_eventos.add(evento);
                                }
                                iniciarRV();
                            }else {
                                new AlertDialog.Builder(ListaEventoActivity.this)
                                        .setTitle(R.string.app_name)
                                        .setMessage(getString(R.string.eventos_not_found))
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
                        errorConexion(ListaEventoActivity.this);
                        hidepDialog(pDialog);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Configuracion.getInstance().addToRequestQueue(request, TAG);
    }

    private void iniciarRV() {
        if (getIntent().getIntExtra(Constantes.FILTRO, -1)==Constantes.FILTRO_CALENDARIO) {
            Date fechaSeleccionada = new Date();
            fechaSeleccionada.setTime(getIntent().getLongExtra(Constantes.FECHA, -1));
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "pe"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("es", "pe"));
            try {
                Date fechaFinDia = sdf.parse(f.format(fechaSeleccionada)+ " 23:59:59");
                ArrayList<EventoSimple> lista_eventos_fecha = new ArrayList<>();
                for (int i = 0; i < lista_eventos.size(); i++) {
                    if (lista_eventos.get(i).getFecha_inicio().after(fechaSeleccionada) && lista_eventos.get(i).getFecha_inicio().before(fechaFinDia)){
                        lista_eventos_fecha.add(lista_eventos.get(i));
                    }
                }
                adapter = new EventoAdapter(ListaEventoActivity.this, lista_eventos_fecha);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            getIntent().removeExtra(Constantes.FECHA);
        }
        else{
            adapter = new EventoAdapter(ListaEventoActivity.this, lista_eventos);
        }
        realmRecyclerView.setHasFixedSize(true);
        realmRecyclerView.setLayoutManager(new LinearLayoutManager(ListaEventoActivity.this));
        realmRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean isSesion() {
        return Usuario.getUsuario() != null && Usuario.getUsuario().isSesion();
    }

    @Override
    public void iniciarLayout() {
        setToolbarSon(toolbar, this, icono_toolbar);
        setFondoActivity(this, ivFondoListaEvento);
    }

    @Override
    public void iniciarPDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.enviando_peticion));
        pDialog.setCancelable(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
