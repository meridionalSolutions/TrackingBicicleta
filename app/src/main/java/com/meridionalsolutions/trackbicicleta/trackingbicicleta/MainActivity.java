package com.meridionalsolutions.trackbicicleta.trackingbicicleta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    //  DECLARACION VARIABLES
    TextView estadoBateria, estadoSistema;                      //Variables texto
    Button enciende,apaga;                                      //Variables botones "enciende" y "apaga"
    ImageButton solicitarUbicacion, ingresarMapa, cambiaNum;    //Variables para botones graficos
    ListView historialCoordenadas;                              //Variable de Lista que despliega el historial de coordenadas
    private ArrayList<String> datosCoordenadas;                 //Datos del historial de coordenadas
    ArrayAdapter<String> adapter;                               //Adaptador para pasar DATOS >> LISTA

    String numeroTelefono = "0939987927";//  "0983520915";      //Variable de numero de telefono SIM808 con un numero predetrminado

    public static final String COORDS = "HistorialCoordenadas"; //Etiqueta (key) de los datos del historial de coordenadas para guardar sesion
    SharedPreferences almacenarCoordenadas;                     //Variable para almacenar coordenadas

    Context este = this;                                        //Variable contexto (para facilitar programacion)

    //METODO QUE SE EJECUTA CUANDO EL SISTEMA RECIBE UN MENSAJE DE TEXTO SMS
    IntentFilter filtroIntent;                                                                              //Pasa el Intent (intent es una herramienta para pasar entre activities o eventos)
    private BroadcastReceiver receptorIntent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            estadoSistema = (TextView) findViewById(R.id.estadoSistema);                                    //Enlaza el texto de Estado del Sistema con el recurso gráfico
            String mensajeFromNetwork = intent.getExtras().getString("MESSAGE");                            //Toma los datos del intent (mensaje de texto)
            //Toast.makeText(MainActivity.this, mensajeFromNetwork, Toast.LENGTH_SHORT).show();               //Despliega el mensaje tomado


            //Revisa si el mensaje contiene informacion del encendido del sistema de la bicicleta
            if (getString(R.string.Confirmacion_Encendido).equals(mensajeFromNetwork)) {
                estadoSistema.setText(R.string.Sistema_ON); //Cambia el texto del Estado del Sistema por ENCENDIDO
            }
            //Revisa si el mensaje contiene informacion del apagado del sistema de la bicicleta
            else if (getString(R.string.Confirmacion_Apagado).equals(mensajeFromNetwork)){
                estadoSistema.setText(R.string.Sistema_OFF); //Cambia el texto del Estado del Sistema por APAGADO
            }

            //Revisa si el mensaje contiene informacion de la posicion y bateria de la bicicleta
            else if ("LAT".equals(mensajeFromNetwork != null ? mensajeFromNetwork.substring(0, 3) : null)
                    )
            {
                assert mensajeFromNetwork != null;  //Comprueba que el mensaje no este vacio para continuar
                String nuevoDato = mensajeFromNetwork.substring(0,mensajeFromNetwork.lastIndexOf(";"));  //Toma la subcadena con la info de latitud y longitud
                String bateria = mensajeFromNetwork.substring(mensajeFromNetwork.lastIndexOf("=") + 1, mensajeFromNetwork.length());  //toma la subcadena con la info de bateria
                //Double datoBateria = Double.parseDouble(bateria);      //  Descomentar en caso de procesar informacion de bateria

                String comprobarCoord = nuevoDato.substring(nuevoDato.indexOf("=")+1,nuevoDato.indexOf(";"));

                estadoBateria.setText("Bateria: " + bateria);            //  Establece el estado de la bateria con el valor enviado por la bicicleta

                if (comprobarCoord.length()<5)
                {
                    Toast.makeText(este, "Las coordenadas enviadas por la Bicicleta son INVALIDAS", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    datosCoordenadas.add(0,nuevoDato);                        //Agrega la info de coordenadas a la lista
                    //Evita que existan mas de diez datos en la lista
                    if(datosCoordenadas.size() > 10)
                        datosCoordenadas.remove(datosCoordenadas.size() - 1);
                    adapter.notifyDataSetChanged();//Guardar cambios
                }

            }
            //Revisa si el mensaje contiene informacion de alerta de robo de la bicicleta
            else if ("ALE".equals(mensajeFromNetwork != null ? mensajeFromNetwork.substring(0, 3) : null)){

                assert mensajeFromNetwork != null; //Comprueba que el mensaje no este vacio para continuar
                String nuevoDato = mensajeFromNetwork.substring(mensajeFromNetwork.lastIndexOf("!") + 1,mensajeFromNetwork.lastIndexOf(";"));//Toma la subcadena con la info de latitud y longitud
                String bateria = mensajeFromNetwork.substring(mensajeFromNetwork.lastIndexOf("=") + 1, mensajeFromNetwork.length());//toma la subcadena con la info de bateria
                //Double datoBateria = Double.parseDouble(bateria);      //  Descomentar en caso de procesar informacion de bateria
                estadoBateria.setText("Bateria: "+bateria);            //  Establece el estado de la bateria con el valor enviado por la bicicleta



                datosCoordenadas.add(0,nuevoDato);       //Agrega la info de coordenadas a la lista
                //Evita que existan mas de diez datos en la lista
                if(datosCoordenadas.size() > 10)
                    datosCoordenadas.remove(datosCoordenadas.size() - 1);
                adapter.notifyDataSetChanged();//Guardar cambios
            }

        }
    };

    // INICIO DE LA PRIMERA ACTIVITY o ACTIVITY PRNCIPAL DE LA APLICACION
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);         //Enlaza esta clase con la clase grafica activity_main.xml

        filtroIntent = new IntentFilter();              //Instancia el intent de los SMS
        filtroIntent.addAction("SMS_RECEIVED_ACTION");  //Activa el intent cuando se haya recibido un mensaje SMS


        //ENLACES DE LAS VARIABLES LOGICAS CON LAS VARIABLES GRAFICAS
        estadoBateria = (TextView) findViewById(R.id.estadoBateria);
        enciende = (Button) findViewById(R.id.botonEncender);
        enciende.setOnClickListener(this);                                      //Establece un listener cuando se da clic en el boton
        apaga = (Button) findViewById(R.id.botonApagar);
        apaga.setOnClickListener(this);                                         //Establece un listener cuando se da clic en el boton
        solicitarUbicacion = (ImageButton) findViewById(R.id.botonUbicacion);
        solicitarUbicacion.setOnClickListener(this);                             //Establece un listener cuando se da clic en el boton
        ingresarMapa = (ImageButton) findViewById(R.id.botonMapa);
        ingresarMapa.setOnClickListener(this);                                   //Establece un listener cuando se da clic en el boton
        cambiaNum = (ImageButton) findViewById(R.id.botonNumero);
        cambiaNum.setOnClickListener(this);                                     //Establece un listener cuando se da clic en el boton


        datosCoordenadas = new ArrayList<String>();                     //Crea la lista de datos de coordenadas

        //Datos que se despliegan en caso de no haber datos guardados
        datosCoordenadas.add("LAT= -2.905083;LON= -79.003492");
        datosCoordenadas.add("LAT= 0.352032;LON= -78.117933");
        datosCoordenadas.add("LAT= 0.353109;LON= -78.129877");

        historialCoordenadas = (ListView) findViewById(R.id.historialCoordenadas);      //Enlaza la lista con la variable grafica
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,datosCoordenadas);   //Crea un adaptador para poner la lista de datos dentro de la lista
        historialCoordenadas.setAdapter(adapter);       //Enlaza la lista con el adaptador

        //Determina los eventos al momento de dar click en algun item de la lista
        historialCoordenadas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dato = adapter.getItem(position);                            //Toma el dato del item de la lista
                Procesar_info procesar_info = new Procesar_info(dato).invoke();     //Llama a la clase de tomar el dato de la lista
                Double datoLatitud = procesar_info.getDatoLatitud();                //Separa el dato de latitud del item de la lista
                Double datoLongitud = procesar_info.getDatoLongitud();              //Separa el dato de longitud del item de la lista

                //Toast.makeText(MainActivity.this, dato, Toast.LENGTH_SHORT).show();

                //Despliega el intent para abrir la activity de los mapas
                Intent toMapActivity;
                toMapActivity = new Intent(MainActivity.this,MapsActivity.class);
                //Ingresa los datos de coordenadas para desplegar en la activity del mapa
                toMapActivity.putExtra("LATITUD",datoLatitud);
                toMapActivity.putExtra("LONGITUD",datoLongitud);
                //Abre la activity del mapa
                startActivity(toMapActivity);
                         }
        });
    }


   //Lista de eventos a realizarse según el clic de cada botón
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.botonEncender:                //Cuando se presiona el boton ENCENDER
                //Envia el SMS de PRENDE al sistema de la bicicleta
                SmsManager enciendeSmsManager = SmsManager.getDefault();
                enciendeSmsManager.sendTextMessage(numeroTelefono,null,"PRENDE",null,null);
                Toast.makeText(this, "Mensaje encendido enviado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.botonApagar:                //Cuando se presiona el boton APAGAR
                //Envia el SMS de APAGA al sistema de la bicicleta
                SmsManager apagaSmsManager = SmsManager.getDefault();
                apagaSmsManager.sendTextMessage(numeroTelefono,null,"APAGA",null,null);
                Toast.makeText(this, "Mensaje apagado enviado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.botonUbicacion:               //Cuando se presiona el boton verde debajo de lista
                //Envia el SMS de GPS al sistema de la bicicleta
                SmsManager ubicacionSmsManager = SmsManager.getDefault();
                ubicacionSmsManager.sendTextMessage(numeroTelefono,null,"GPS",null,null);
                Toast.makeText(this, "Mensaje ubicacion enviado", Toast.LENGTH_SHORT).show();
                break;
            case R.id.botonMapa:                    //Cuando se presiona el boton azul debajo de lista
                //Toma el primer dato  de coordenadas de la lista para desplegarlo en el mapa
                String dato = adapter.getItem(0);
                Procesar_info procesar_info = new Procesar_info(dato).invoke();
                Double datoLatitud = procesar_info.getDatoLatitud();
                Double datoLongitud = procesar_info.getDatoLongitud();

                //Despliega la activity del mapa
                Intent desplegarMapa;
                desplegarMapa = new Intent(MainActivity.this,MapsActivity.class);
                //Envia los datos de coordenadas para el mapa
                desplegarMapa.putExtra("LATITUD",datoLatitud);
                desplegarMapa.putExtra("LONGITUD",datoLongitud);
                startActivity(desplegarMapa);           //Abre la activity del mapa
                break;
            case R.id.botonNumero:                  //Cuando se presiona el boton naranja debajo de lista

                //Crea la interfaz grafica del cuadro de dialogo para cambiar el numero de telefono de la bicicleta
                LayoutInflater inflater = LayoutInflater.from(este);
                final View vista_editar_numero = inflater.inflate(R.layout.dialog_editar_num,null);
                final EditText entradaNum = (EditText) vista_editar_numero.findViewById(R.id.etCambioNumero);   //Crea la variable del nuevo numero

                AlertDialog.Builder dialogNumero = new AlertDialog.Builder(este);       //Crea el dialogo
                dialogNumero.setMessage("Ingresa el numero de telefono del nuevo modulo en la bicicleta")
                        .setTitle("CAMBIAR NUMERO")
                        .setView(vista_editar_numero)
                        .setCancelable(false)
                        .setPositiveButton("CAMBIAR", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Toma el dato escrito en el cuadro de texto y lo analiza para saber si es un numero de celular valido
                                String nuevoNum = entradaNum.getText().toString();
                                //Primero mira si el numero tiene al menos 10 cifras
                                if (nuevoNum.length() != 10){
                                    Toast.makeText(este, "NUMERO INVALIDO. Se mantiene numero anterior: "+numeroTelefono , Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    String ceroNueve = nuevoNum.substring(0,2);
                                    //Si el numero tiene 10 cifras, analiza si este empieza con 09, caso contrario anula el cambio
                                    if("09".equals(ceroNueve))
                                    {
                                        numeroTelefono = nuevoNum;
                                        Toast.makeText(este, "Numero cambiado exitosamente: "+numeroTelefono, Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(este, "NUMERO INVALIDO. Se mantiene numero anterior: "+numeroTelefono , Toast.LENGTH_SHORT).show();
                                    }

                                }


                            }
                        })
                        .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Cancela el cambio de numero
                                Toast.makeText(este, "Se mantiene numero anterior: "+ numeroTelefono, Toast.LENGTH_LONG).show();

                            }
                        });
                AlertDialog alertDialog = dialogNumero.create();
                alertDialog.show();
                break;

            default:
                break;

        }
    }
    //Cuando la activity principal inicia o retorna a la funcionalidad
    @Override
    protected void onResume() {
        registerReceiver(receptorIntent,filtroIntent); //Activa la recepcion de SMS
        super.onResume();
        recuperar_Coord();  //recupera el historial de coordenadas

    }



    @Override
    protected void onPause() {
        super.onPause();
    }



    //Cuando se sale de la aplicacion
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receptorIntent);         //Desactiva la recepcion SMS
        guardar_Coord();                            //Guarda el historial de coordenadas
    }

    //Procesa la informacion de cualquier item de la lista para devolver los datos de latitud y longitud
    private class Procesar_info {
        private String dato;
        private Double datoLatitud;
        private Double datoLongitud;

        public Procesar_info(String dato) {
            this.dato = dato;
        }

        public Double getDatoLatitud() {
            return datoLatitud;
        }

        public Double getDatoLongitud() {
            return datoLongitud;
        }

        public Procesar_info invoke() {
            assert dato != null;//Comprueba que el texto de la lista no este vacio

            //Señales para tomar los datos de latitud y longitud
            int igual1=dato.indexOf("=");           //Señal del primer simbolo de '=' que se encuentre en el texto
            int igual2=dato.lastIndexOf("=");       //Señal del segundo simbolo de '=' que se encuentre en el texto
            int pcoma1=dato.indexOf(";");           //Señal del simbolo de ';' que se encuentre en el texto


            String latitud = dato.substring(igual1 + 1, pcoma1);            //Toma la latitud desde el primer '=' hasta que encuentre ';'
            String longitud = dato.substring(igual2 + 1, dato.length());    //Toma la longitud desde el 2do '=' hasta el final
            //Convierte los valores de texto en variables numericas para poder procesarlas y desplegarlas en el mapa
            datoLatitud = Double.parseDouble(latitud);
            datoLongitud = Double.parseDouble(longitud);
            return this;
        }
    }

    //Guarda el historial de coordenadas de la lista
    private void guardar_Coord() {
        almacenarCoordenadas = getSharedPreferences(COORDS, Context.MODE_PRIVATE); // crea la variable SharedPreferences para las coordenadas con el Indice COORDS
        SharedPreferences.Editor editor = almacenarCoordenadas.edit();              //Crea el editor de la SharedPreferences
        //Toast.makeText(this, "guardando datos", Toast.LENGTH_SHORT).show();       //Descomentar si se quiere ver el momento en el que se ejecuta esta clase
        //Guarda cada uno de los datos de la lista
        for (int i=0; i < datosCoordenadas.size(); i++){
            if (adapter.getItem(i) != null){
                String dato = adapter.getItem(i);
                editor.putString("dato"+i,dato);    //Guarda el dato de cada elemento de la lista
                editor.apply();                     //Aplica los cambios
            }
            else {

                break;
            }
        }
        editor.commit();
        if(editor.commit())
        {
            Toast.makeText(this, "guardado exitosamente", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "no se pudo guardar", Toast.LENGTH_SHORT).show();
        }
    }
    //Recuperar los valores del historial de coordenadas cuando se abre nuevamente la aplicacion
    private void recuperar_Coord() {
        almacenarCoordenadas = getSharedPreferences(COORDS, Context.MODE_PRIVATE);    //Toma el dato de SharedPreferences guardado con el indice COORDS
        int cantidadDatos = almacenarCoordenadas.getAll().size();
        Toast.makeText(este, "recuperando historial coordenadas", Toast.LENGTH_SHORT).show();         //Muestra que los datos se estan recuperando
        if (almacenarCoordenadas.contains("dato1"))
        {
            //Ingresa uno a uno los valores guardados previamente en la lista
            for (int i=cantidadDatos; i > 0; i--){
                String dato = almacenarCoordenadas.getString("dato"+i, null);

                if (dato != null){
                    datosCoordenadas.add(0,dato);
                    if(datosCoordenadas.size() > 10)
                        datosCoordenadas.remove(datosCoordenadas.size() - 1);
                    adapter.notifyDataSetChanged();

                }
                else {
                    break;
                }
            }

        }
        else
        {
            Toast.makeText(this, "no hay datos", Toast.LENGTH_SHORT).show();
        }
    }
}
