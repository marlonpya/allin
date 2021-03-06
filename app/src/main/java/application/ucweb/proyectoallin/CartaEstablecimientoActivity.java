package application.ucweb.proyectoallin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import application.ucweb.proyectoallin.adapter.ProductoAdapter;
import application.ucweb.proyectoallin.adapter.ProductoRealmAdapter;
import application.ucweb.proyectoallin.aplicacion.BaseActivity;
import application.ucweb.proyectoallin.aplicacion.Configuracion;
import application.ucweb.proyectoallin.interfaz.IActividad;
import application.ucweb.proyectoallin.model.Establecimiento;
import application.ucweb.proyectoallin.model.Producto;
import application.ucweb.proyectoallin.modelparseable.EstablecimientoSimple;
import application.ucweb.proyectoallin.modelparseable.EventoSimple;
import application.ucweb.proyectoallin.modelparseable.ProductoSimple;
import application.ucweb.proyectoallin.util.ConexionBroadcastReceiver;
import application.ucweb.proyectoallin.util.Constantes;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;

public class CartaEstablecimientoActivity extends BaseActivity implements IActividad{
    @BindView(R.id.sontoolbar) Toolbar toolbar;
    @BindView(R.id.tvTituloSonToolbar) ImageView icono_toolbar;
    @BindView(R.id.lista_puntos) RecyclerView rvlista_puntos;
    @BindView(R.id.spCarta)Spinner spCarta;
    @BindView(R.id.tvDescripcionToolbar)TextView tvTituloCartaDiscoteca;
    @BindView(R.id.tvResultadoCartaDiscoteca)TextView tvResultadoCartaDiscoteca;
    @BindView(R.id.tv_cantidad_productos_carrito)TextView tv_cantidad_productos_carrito;
    @BindString(R.string.dialogo_condiciones_compra) String condiciones_compra;
    public static final String TAG = CartaEstablecimientoActivity.class.getSimpleName();
    private ProductoAdapter adapter;
    private ProgressDialog pDialog;
    private ArrayList<ProductoSimple> lista_productos = new ArrayList<>();
    private ArrayList<ProductoSimple> lista_filtrada = new ArrayList<>();
    public static ArrayList<ProductoSimple> lista_carrito = new ArrayList<>();
    private int id=-1;
    private int count=0;
    private int tipoVista=-1;
    private EstablecimientoSimple local;
    private EventoSimple evento;

    private String METHOD_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carta_establecimiento);
        iniciarLayout();
        iniciarPDialog();
        tvTituloCartaDiscoteca.setText(R.string.carta_discoteca);
        //idLocal = getIntent().getIntExtra(Constantes.ID_LOCAL, -1);
        if (getIntent().hasExtra(Constantes.OBJ_S_ESTABLECIMIENTO)){
            local=(EstablecimientoSimple)getIntent().getSerializableExtra(Constantes.OBJ_S_ESTABLECIMIENTO);
            id=local.getId_server();
            METHOD_URL = Constantes.PRODUCTOS_X_LOCAL;
            tipoVista=Constantes.VISTA_DE_LOCAL;
        }
        else if (getIntent().hasExtra(Constantes.OBJ_S_EVENTO)){
            evento=(EventoSimple) getIntent().getSerializableExtra(Constantes.OBJ_S_EVENTO);
            id=evento.getId_server();
            METHOD_URL = Constantes.PRODUCTOS_X_EVENTO;
            tipoVista=Constantes.VISTA_DE_EVENTO;
        }

        dialogo();
        if (id!=-1) {
            if (ConexionBroadcastReceiver.isConect()) {
                requestProductos();
            }//else ConexionBroadcastReceiver.showSnack(id_gallery, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //iniciarRV();
    }

    private void dialogo() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.carta_discoteca))
                .setMessage("*No válido para eventos especiales (VER CALENDARIO).\n*Incluye entrada libre para el cliente.")
                .setIcon(R.drawable.iconoalertafucsia)
                .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick(R.id.iv_ir_a_carrito_puntos)
    public void mostrarCarrito(){
        if (lista_carrito.size()>0) {
            if (tipoVista==Constantes.VISTA_DE_LOCAL){
                startActivity(new Intent(this, ListaCompraActivity.class)
                        .putExtra(Constantes.ARRAY_S_CARRITO, lista_carrito)
                        .putExtra(Constantes.OBJ_S_ESTABLECIMIENTO, local));
            }else if (tipoVista==Constantes.VISTA_DE_EVENTO){
                startActivity(new Intent(this, ListaCompraActivity.class)
                        .putExtra(Constantes.ARRAY_S_CARRITO, lista_carrito)
                        .putExtra(Constantes.OBJ_S_EVENTO, evento));
            }
        }
        else {
            Toast.makeText(this, "Carrito vacío!", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarRV() {
        if (tipoVista==Constantes.VISTA_DE_LOCAL) adapter = new ProductoAdapter(this, lista_filtrada, tv_cantidad_productos_carrito, local);
        else if (tipoVista==Constantes.VISTA_DE_EVENTO) adapter = new ProductoAdapter(this, lista_filtrada, tv_cantidad_productos_carrito, evento);

        rvlista_puntos.setHasFixedSize(true);
        rvlista_puntos.setLayoutManager(new GridLayoutManager(CartaEstablecimientoActivity.this, 2));
        rvlista_puntos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        tvResultadoCartaDiscoteca.setText(count + " resultados");
    }


    @OnItemSelected(R.id.spCarta)
    public void spCartaSeeleccion(int position) {
        int tipo=position-1;
        if (tipo==-1){
            lista_filtrada.clear();
            lista_filtrada.addAll(lista_productos);
        }
        else {
            lista_filtrada.clear();
            for (int i = 0; i < lista_productos.size(); i++) {
                if (lista_productos.get(i).getTipo()==tipo){
                    lista_filtrada.add(lista_productos.get(i));
                }
            }
        }
        count = lista_filtrada.size();
        iniciarRV();
    }

    private void requestProductos() {
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
                                    ProductoSimple producto = new ProductoSimple();
                                    producto.setIdServer(jEventos.getJSONObject(i).getInt("PRO_ID"));
                                    producto.setNombre(jEventos.getJSONObject(i).getString("PRO_NOMBRE"));
                                    producto.setPrecio_normal(BigDecimal.valueOf(Double.parseDouble(jEventos.getJSONObject(i).getString("PRO_PRECIO_NORMAL"))));
                                    producto.setPrecio_allin(BigDecimal.valueOf(Double.parseDouble(jEventos.getJSONObject(i).getString("PRO_PRECIO_ALLIN"))));
                                    //producto.setPrecio_normal(Double.parseDouble(jEventos.getJSONObject(i).getString("PRO_PRECIO_NORMAL")));
                                    //producto.setPrecio_allin(Double.parseDouble(jEventos.getJSONObject(i).getString("PRO_PRECIO_ALLIN")));
                                    producto.setPrecio_puntos(jEventos.getJSONObject(i).getInt("PRO_PRECIO_PUNTOS"));
                                    producto.setStock(jEventos.getJSONObject(i).getInt("PRO_STOCK"));
                                    producto.setIdLocal(jEventos.getJSONObject(i).getInt("LOC_ID"));
                                    producto.setIdEvento(jEventos.getJSONObject(i).getInt("EVE_ID"));
                                    producto.setPromocion(jEventos.getJSONObject(i).getString("PRO_PROMOCION"));
                                    //producto.setNombre_local(jEventos.getJSONObject(i).getString("LOC_NOMBRE"));
                                    //producto.setNombre_evento(jEventos.getJSONObject(i).getString("EVE_NOMBRE"));
                                    if (!jEventos.getJSONObject(i).getString("PRO_CONDICIONES").trim().equals("null") && !jEventos.getJSONObject(i).getString("PRO_CONDICIONES").trim().isEmpty()){
                                        producto.setCondiciones(jEventos.getJSONObject(i).getString("PRO_CONDICIONES"));
                                    }else {
                                        producto.setCondiciones("No hay condiciones");
                                    }
                                    try {
                                        producto.setFecha_inicio(sdf.parse(jEventos.getJSONObject(i).getString("PRO_FEC_INICIO")));
                                        producto.setFecha_fin(sdf.parse(jEventos.getJSONObject(i).getString("PRO_FEC_FIN")));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    producto.setImagen(jEventos.getJSONObject(i).getString("PRO_IMAGEN"));
                                    producto.setTipo(jEventos.getJSONObject(i).getInt("PRO_TIPO"));
                                    producto.setEstado(jEventos.getJSONObject(i).getInt("PRO_ESTADO")==1);
                                    lista_productos.add(producto);
                                }
                                Log.d(TAG, lista_productos.toString());
                                spCartaSeeleccion(0);
                            }else {
                                new AlertDialog.Builder(CartaEstablecimientoActivity.this)
                                        .setTitle(R.string.app_name)
                                        .setMessage(getString(R.string.productos_not_found))
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
                        hidepDialog(pDialog);
                    }
                }
        ){
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

    @Override
    public boolean isSesion() {
        return false;
    }

    public void iniciarLayout() {
        setSpinnerRosa(this, spCarta, R.array.arrayCarta);
        setToolbarSon(toolbar, this, icono_toolbar);
    }

    @Override
    public void iniciarPDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage(getString(R.string.enviando_peticion));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearLists();
    }

    public void clearLists(){
        lista_productos.clear();
        lista_filtrada.clear();
        lista_carrito.clear();
    }

    @Override
    public void onBackPressed() {
        if(lista_carrito.size()>0){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.carta_discoteca))
                    .setMessage(R.string.advertencia_carrito)
                    .setIcon(R.drawable.iconoalertafucsia)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            CartaEstablecimientoActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }else {
            super.onBackPressed();
        }
    }
}
