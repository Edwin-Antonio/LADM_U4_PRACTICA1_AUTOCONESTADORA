package mx.tecnm.tepic.ladm_u4_practica1_llamadas

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main3.*

class MainActivity3 : AppCompatActivity() {
    var dbRemota = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        read()

        btn_regresar_ln.setOnClickListener {
            finish()
        }
        btn_agregar_ln.setOnClickListener {
            if(insert()){
                Toast.makeText(this,"Se inserto con éxito", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"Error al insertar", Toast.LENGTH_LONG).show()
            }
            read()
            limpiarCasillas()
        }
    }

    fun insert() : Boolean{
        val drivTable = DataBase(this,"CALL1",null,1).writableDatabase
        var data = ContentValues()
        data.put("nombre",edt_name_ln.text.toString())
        data.put("celular",edt_number_ln.text.toString())
        val result = drivTable.insert("LISTAN",null,data)
        drivTable.close()
        var datosInsertar = hashMapOf(
            "nombre" to edt_name_ln.text.toString(),
            "numero" to edt_number_ln.text.toString())
        dbRemota.collection("lista_negra")
            .add(datosInsertar as Any)
            .addOnSuccessListener {
                //sendSms(cursor.getString(posColumnaNumero),"Numero bloqueado por el programador")
            }
            .addOnFailureListener {
                //Toast.makeText(this,"NO SE ENVIO A LA BASE REMOTA",Toast.LENGTH_LONG).show()
            }
        //Insert ID > 0 numero de renglon insertado = SI SE PUDO
        // si regresa -1 LONG (Entero largo) = NO SE PUDO
        if (result == -1L) return false
        return true
    }

    fun read(){
        //Cursor = objeto que tiene el resulado de un select
        val drivTable = DataBase(this,"CALL1",null,1).readableDatabase

        //SELECT * FROM LISTAB
        val cursor = drivTable.query("LISTAN", arrayOf("*"), null,null,null,null,null)

        //Se mueve a la primer posición del select y si hay un dato returna un true
        if (cursor.moveToFirst()){
            var datas = ""
            do {
                datas += "Nombre: "+cursor.getString(0)+"\nNumero: "+cursor.getString(1)+"\n--------------\n"
                lista_negra.setText(datas)
            }while (cursor.moveToNext())
        }else{
            lista_negra.setText("No hay datos para mostrar")
        }
        drivTable.close()
    }
    fun limpiarCasillas(){
        edt_name_ln.setText("")
        edt_number_ln.setText("")
    }
}