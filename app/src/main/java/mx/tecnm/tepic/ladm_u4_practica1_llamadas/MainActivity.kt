package mx.tecnm.tepic.ladm_u4_practica1_llamadas

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var seguridad = false
    val permiso = 5
    val permisoMensaje = 60
    var hiloControl : HiloControl?=null
    var dbRemota = FirebaseFirestore.getInstance()
    var intento = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        security()

        hiloControl = HiloControl(this)
        hiloControl?.start()

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG,
                    android.Manifest.permission.READ_CONTACTS),
                    permiso)

        }else{
            leerLlamadas()
        }
        btn_seguridad.setOnClickListener {
            seguridad = !seguridad
            security()
        }
        btn_listaBlanca.setOnClickListener {
            var window = Intent(this, MainActivity2::class.java)
            startActivity(window)
        }
        btn_listaNegra.setOnClickListener {
            var window = Intent(this, MainActivity3::class.java)
            startActivity(window)
        }
    }

    private fun security() {
        if (seguridad){
            txt_seguridad.setText("Seguridad del dispositivo: Activo")
        }else{
            txt_seguridad.setText("Seguridad del dispositivo: Apagado")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==permiso){
            leerLlamadas()
        }
    }

    fun leerLlamadas() {
        var resultado = ""
        /*Tipos de llamadas
        * 1 -> Llamada Entrante
        * 2 -> Llamada Saliente
        * 3 -> Llamada Perdida
        * 4 -> Cooreo de voz
        * 5 -> Llamada Rechazada
        * 6 -> Numeros bloqueados
        * */

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), permiso)
        }
        //Uri.parse("content://call_log/calls");
        var cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,null,null,null,null)
        if(cursor!!.moveToLast()){
            var posColumnaNumero = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            var posColumnaTipo = cursor.getColumnIndex(CallLog.Calls.TYPE)
            var posColumnaFecha = cursor.getColumnIndex(CallLog.Calls.DATE)
            var callType =""

            do{
                when(cursor.getString(posColumnaTipo).toInt()){
                    1 -> {
                        //sendSms(cursor.getString(posColumnaNumero),"No incistas porfavor")
                        callType = "Llamada Entrante"
                    }
                    2 -> callType = "Llamada Saliente"
                    3 -> {
                        if(seguridad){
                            buscarBD(cursor, posColumnaNumero, posColumnaFecha)
                        }
                        callType = "Llamada Perdida"
                    }
                    4 -> callType = "Cooreo de voz"
                    5 -> callType = "Llamada Rechazada"
                    6 -> callType = "Numero bloqueado"
                    else -> callType = "Numero Desconocido"
                }
                val fechaLlamada = cursor.getString(posColumnaFecha)
                resultado += "Numero: "+cursor.getString(posColumnaNumero)+
                        "\nTipo de Llamada: "+callType+"\nFecha: "+ Date(fechaLlamada.toLong()) +"\n-------------------------\n"
            }while (cursor.moveToNext())
        }else{
            resultado = "NO HAY LLAMADAS"
        }
        lista.setText(resultado)
    }

    private fun buscarBD(cursor: Cursor, posColumnaNumero: Int, posColumnaFecha: Int) {
        //Cursor = objeto que tiene el resulado de un select
        val drivTable = DataBase(this, "CALL1", null, 1).readableDatabase
        //SELECT * FROM LISTAB
        val cursorNegro = drivTable.query("LISTAN", arrayOf("*"), null, null, null, null, null)
        if (cursorNegro.moveToFirst()) {
            //sendSms(cursor.getString(posColumnaNumero),numeroDB) - Hasta aquí todo bien
            do {
                var numeroDB = cursorNegro.getString(1)
                if (numeroDB.equals(cursor.getString(posColumnaNumero), true)) {
                    //sendSms(cursor.getString(posColumnaNumero),"Numero bloqueado por el programador")
                    var datosInsertar = hashMapOf(
                            "nombre" to cursorNegro.getString(0),
                            "numero" to cursorNegro.getString(1),
                            "mensaje" to "NO DEVOLVERE TU LLAMADA, POR FAVOR NO INSISTAS",
                            "fecha" to Date(cursor.getString(posColumnaFecha).toLong()))
                    dbRemota.collection("llam_NoDeseada")
                            .add(datosInsertar as Any)
                            .addOnSuccessListener {
                                //sendSms(cursor.getString(posColumnaNumero),"Numero bloqueado por el programador")
                            }
                            .addOnFailureListener {
                                //Toast.makeText(this,"NO SE ENVIO A LA BASE REMOTA",Toast.LENGTH_LONG).show()
                            }
                    sendSms(cursor.getString(posColumnaNumero), "NO DEVOLVERE TU LLAMADA, POR FAVOR NO INSISTAS")
                }
            } while (cursorNegro.moveToNext())
        }
        drivTable.close()
        //Cursor = objeto que tiene el resulado de un select
        val drivTableB = DataBase(this, "CALL1", null, 1).readableDatabase
        //SELECT * FROM LISTAB
        val cursorBlanco = drivTableB.query("LISTAB", arrayOf("*"), null, null, null, null, null)
        if (cursorBlanco.moveToFirst()) {

            do {
                var numeroDB2 = cursorBlanco.getString(1)
                //sendSms(cursor.getString(posColumnaNumero), cursor.getString(posColumnaNumero) +" - "+ numeroDB2)
                if (numeroDB2.equals(cursor.getString(posColumnaNumero), true)) {
                    sendSms(cursor.getString(posColumnaNumero), "Ponganos 70 en la ultima unidad a nuestra bina profe por navidad, y ya irnos de vacaciones porfavor jajaja")
                }
            } while (cursorBlanco.moveToNext())
        }
        drivTableB.close()
    }

    private fun sendSms(str: String, mensaje: String) {
        if(ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            //Solicitar un permiso en caso de que se encuntre denegado
            // Activity, Arreglo de Cuantos permisos va a otorgar, El valor que tendra el permiso
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.SEND_SMS), permisoMensaje )
        }else {
            SmsManager.getDefault().sendTextMessage(str, // Dirección/numero del telefono a enviar
                    null,
                    mensaje, //Texto del mensaje a enviar
                    null,
                    null)
            Toast.makeText(this,"SE ENVIO EL MENSAJE", Toast.LENGTH_LONG).show()
        }
    }
}