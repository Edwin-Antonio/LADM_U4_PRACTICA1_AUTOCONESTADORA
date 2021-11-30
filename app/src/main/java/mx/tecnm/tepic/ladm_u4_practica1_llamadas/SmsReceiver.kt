package mx.tecnm.tepic.ladm_u4_practica1_llamadas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        val data = p1.extras //Los extras son parametros de envio de un emisor a un receptor
        // Si la data no esta vacia
        if (data != null) {
            var sms = data.get("pdus") as Array<Any>// pdus
            for (indice in sms.indices){
                //Formato
                var format = data.getString("format") //3gpp o 3gpp2

                //Obtener el mensaje y el remitente
                var smsMessaje = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    SmsMessage.createFromPdu(sms[indice] as ByteArray, format)
                }else{
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var phonOrigin = smsMessaje.originatingAddress
                var smsContent = smsMessaje.messageBody.toString()

                //Para mandar los datos de un Kotlin class es recomendable hacer una tabla en SQLite
                try {
                    var baseDatos = DataBase(p0,"entrantes",null,1)
                    var insert = baseDatos.writableDatabase
                    insert.execSQL("INSERT INTO ENTRANTES VALUES('${phonOrigin}','${smsContent}')") //Se usan comillas simples porque son tipo VARCHAR

                    baseDatos.close()
                }catch (error: SQLiteException){ Toast.makeText(p0,error.toString(), Toast.LENGTH_LONG).show()}

                Toast.makeText(p0,"ENTRO CONTENIDO: ${smsContent}", Toast.LENGTH_LONG).show()
            }
        }
    }
}