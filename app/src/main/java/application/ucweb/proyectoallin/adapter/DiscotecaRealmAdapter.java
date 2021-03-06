package application.ucweb.proyectoallin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import application.ucweb.proyectoallin.EventoActivity;
import application.ucweb.proyectoallin.R;
import application.ucweb.proyectoallin.aplicacion.BaseActivity;
import application.ucweb.proyectoallin.model.Establecimiento;
import application.ucweb.proyectoallin.util.Constantes;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by ucweb02 on 22/09/2016.
 */
public class DiscotecaRealmAdapter extends RealmBasedRecyclerViewAdapter<Establecimiento, DiscotecaRealmAdapter.ViewHolder> {

    public DiscotecaRealmAdapter(
            Context context,
            RealmResults<Establecimiento> realmResults,
            boolean automaticUpdate,
            boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public DiscotecaRealmAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_establecimiento, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(DiscotecaRealmAdapter.ViewHolder viewHolder, int i) {
        final Establecimiento item = realmResults.get(i);
        BaseActivity.setImageConGlideCircular(getContext(), viewHolder.contorno, R.drawable.circulo_con_lineas);
        BaseActivity.setImageConGlideCircular(getContext(), viewHolder.imagen, item.getImagen());
        viewHolder.nombre.setText(item.getNombre());
        viewHolder.fecha.setText("Fecha: "+item.getFecha_inicio());
        viewHolder.hora.setText("Hora: " + item.getFecha_fin());
        viewHolder.boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EventoActivity.class);
                intent.putExtra(Constantes.K_S_TITULO_TOOLBAR, item.getNombre());
                intent.putExtra(Constantes.K_L_ID_EVENTO, item.getId());
                getContext().startActivity(intent);
            }
        });
    }

    public static class ViewHolder extends RealmViewHolder {
        @BindView(R.id.row_iv_establecimiento_contorno)ImageView contorno;
        @BindView(R.id.row_iv_establecimiento_imagen)ImageView imagen;
        @BindView(R.id.row_tv_establecimiento_nombre)TextView nombre;
        @BindView(R.id.row_tv_establecimiento_fecha)TextView fecha;
        @BindView(R.id.row_tv_establecimiento_hora)TextView hora;
        @BindView(R.id.row_btnEstablecimiento) LinearLayout boton;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
