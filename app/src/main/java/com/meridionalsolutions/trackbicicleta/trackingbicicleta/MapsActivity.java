package com.meridionalsolutions.trackbicicleta.trackingbicicleta;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;             //Crea la variable de googleMaps
    MarkerOptions opciones;             //Crea una variable para guardar las opciones del marcador
    Marker marcador;                    //Crea una variable de marcador

    Double latitud;                     //Dato latitud para desplegar en el mapa
    Double longitud;                    //Dato longitud para desplegar en el mapa
    String dato;                        //Dato tomado del mensaje SMS

    IntentFilter filtroIntent;
    private BroadcastReceiver receptorIntent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String mensajeFromNetwork = intent.getExtras().getString("MESSAGE");
            //Toast.makeText(MapsActivity.this, mensajeFromNetwork, Toast.LENGTH_SHORT).show();

            if ("LAT".equals(mensajeFromNetwork != null ? mensajeFromNetwork.substring(0, 3) : null)
                    ) {
                assert mensajeFromNetwork != null;
                dato = mensajeFromNetwork.substring(0, mensajeFromNetwork.lastIndexOf(";"));
                int igual1 = dato.indexOf("=");
                int igual2 = dato.lastIndexOf("=");
                int pcoma1 = dato.indexOf(";");


                String datoLatitud = dato.substring(igual1 + 1, pcoma1);
                String datoLongitud = dato.substring(igual2 + 1, dato.length());
                latitud = Double.parseDouble(datoLatitud);
                longitud = Double.parseDouble(datoLongitud);

            } else if ("ALE".equals(mensajeFromNetwork != null ? mensajeFromNetwork.substring(0, 3) : null)) {

                assert mensajeFromNetwork != null;
                dato = mensajeFromNetwork.substring(mensajeFromNetwork.lastIndexOf("!") + 1, mensajeFromNetwork.lastIndexOf(";"));
                int igual1 = dato.indexOf("=");
                int igual2 = dato.lastIndexOf("=");
                int pcoma1 = dato.indexOf(";");


                String datoLatitud = dato.substring(igual1 + 1, pcoma1);
                String datoLongitud = dato.substring(igual2 + 1, dato.length());

                latitud = Double.parseDouble(datoLatitud);
                longitud = Double.parseDouble(datoLongitud);

            }
            actualizarMapa(mMap);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);  //Enlaza esta clase con la clase grafica activity_maps.xml



        filtroIntent = new IntentFilter();              //Establece el intent de recepcion de SMS
        filtroIntent.addAction("SMS_RECEIVED_ACTION");  //Inicia el intent cuando se recibe un SMS

        //Se crea el marcador con el icono de bicicleta y el titulo cuando se da clic al marcador
        opciones = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bicicleta_oscura)).title("Aqui esta la bici");

        //Crea una variable para preguntar por la conexion del mapa
        int mapStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //Si la conexion a internet resulta exitosa entonces se crea el fragment con el MAPA
        if (mapStatus == ConnectionResult.SUCCESS){

            // Crea el mapa y notifica cuando el mapa esta listo para usarse
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        //Si no se da la conexion se notifica al usuario del error
        else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(mapStatus,(Activity)getApplicationContext(),10);
            dialog.show();
        }

        //Recibe los datos de latitud y longitud enviados por la activity principal
        Intent fromMain = getIntent();
        Bundle datosCoord = fromMain.getExtras();

        if (datosCoord!=null){
            latitud = (Double) datosCoord.get("LATITUD");
            longitud = (Double) datosCoord.get("LONGITUD");

        }
    }   


    /**
     * Maneja el mapa una vez está disponible y listo para usarse
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        actualizarMapa(mMap);                                //Llama al metodo que ingresa las coordenadas al mapa
        registerReceiver(receptorIntent,filtroIntent);       //Activa la recepcion de SMS

    }

    //Ingresa los datos de coordenadas en el mapa y configura las opciones de visualizacion
    private void actualizarMapa(final GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);        //Configura el tipo de mapa Hibrido: Satelital pero con nombres en las calles

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);                //Despliega los botones + y - del zoom
        uiSettings.setMapToolbarEnabled(true);

        //Coloca el marcador en las coordenadas recibidas y mueve la camara hacia el marcador
        LatLng ubiBici = new LatLng(latitud, longitud);
        marcador = googleMap.addMarker(opciones.position(ubiBici));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ubiBici));

        float zoomlevel = 18;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubiBici,zoomlevel));

        //Elimina todos los marcadores al tener presionado en cualquier zona del mapa
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(MapsActivity.this, "Marcadores Eliminados", Toast.LENGTH_SHORT).show();
                googleMap.clear();
            }
        });
    }

    @Override
        protected void onResume() {
            registerReceiver(receptorIntent,filtroIntent);      //Activa la recepcion SMS
            super.onResume();
        }

        @Override
        protected void onPause() {
            super.onPause();



        }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receptorIntent);                     //Desactiva la recepcion SMS

    }
}
