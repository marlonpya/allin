package application.ucweb.proyectoallin.apis.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import application.ucweb.proyectoallin.EncuestaActivity;
import application.ucweb.proyectoallin.PrincipalActivity;
import application.ucweb.proyectoallin.R;
import application.ucweb.proyectoallin.util.Constantes;

/**
 * Created by ucweb03 on 16/01/2017.
 */

public class FcmMessagingService extends FirebaseMessagingService {
    private static final String TAG = FcmMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, remoteMessage.getData() == null ? "ERROR" : remoteMessage.getData().toString());
        if (remoteMessage != null) {
            String ruta_imagen = remoteMessage.getData().get("rutaImagen") == null ? null : remoteMessage.getData().get("rutaImagen");
            if (ruta_imagen!=null){
                notificacion(ruta_imagen);
            }
            String idEncuesta = remoteMessage.getData().get("ID_enc") == null ? null : remoteMessage.getData().get("ID_enc");
            if (idEncuesta!=null){
                notificacionEncuesta(Integer.parseInt(idEncuesta));
            }
        }
    }

    private void notificacion(String ruta_imagen) {
        Intent i = new Intent(this, PrincipalActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.EXTRA_S_RUTA_IMAGEN, ruta_imagen);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("ALL IN")
                .setContentText("¡ Tienes una nueva Promoción !")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }

    private void notificacionEncuesta(int idEncuesta) {
        Intent i = new Intent(this, EncuestaActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ID_ENCUESTA, idEncuesta);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("ALL IN")
                .setContentText("¡ Tienes una nueva Encuesta !")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }
}
