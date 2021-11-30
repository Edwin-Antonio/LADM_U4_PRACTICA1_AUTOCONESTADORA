package mx.tecnm.tepic.ladm_u4_practica1_llamadas

class HiloControl(p: MainActivity) : Thread() {
    private var iniciado = false
    private var puntero = p
    private var pausa = false
    private var contador = 1

    override fun run() {
        super.run()
        iniciado = true
        while (iniciado) {
            sleep(3000)
            puntero.runOnUiThread {
                puntero.leerLlamadas()
            }
        }
    }
}