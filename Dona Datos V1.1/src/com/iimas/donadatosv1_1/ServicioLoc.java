package com.iimas.donadatosv1_1;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.iimas.utils.Util;

public class ServicioLoc extends Service implements Runnable{
	
	/**
	 * Declarando variables
	 */
	String[] datosMovil = new String[4], movimiento = new String[3];
	private DBHelper BD; //Objeto de la clase DBHelper (Base de datos)
	private GetInfo info; //Objeto de la clase GetIngo (Informacion del dispositivo)
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
	private Location currentLocation = null;
	private Thread thread;
	private Double latitud, longitud;
	private boolean gps, network;
	NotificationManager nm;
	Notification notif;
	static String ns = Context.NOTIFICATION_SERVICE;
	int icono_v = R.drawable.ic_launcher;
	int icono_r = R.drawable.logoaudi;
	int TIMEOUT_MILLISEC = 300000; // = 5 minutos


    @Override
    public void onCreate() {
    	Toast.makeText(this,getResources().getString(R.string.service), Toast.LENGTH_SHORT).show();
        super.onCreate();
        prepararBD();	//Preparando la base de datos para poder usarla
        
        if(!BD.count()){
        	dispInf();		//Obtenemos email, marca y modelo de dispositivo
        	BD.InfoDisp(datosMovil);	//Insertamos en SQLite los datos obtenido (email, marca y modelo)
        	
        }
		
      	mLocationListener = new MyLocationListener();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//Inicio el servicio de notificaciones accediendo al servicio
		nm = (NotificationManager) getSystemService(ns);
		
		//Realizadno una notificacion por medio de un metodo
		notificacion(icono_v, "Servicio en ejecucion",
				"El servicio esta en ejecucion",
				"se enviaran datos cuando tengas conexion a internet");
		
		nm.notify(1, notif);
		
    }
    
    
    /**
     * Creando la notificacion
     */
    public void notificacion(int icon, CharSequence textoEstado, CharSequence titulo, CharSequence text) {
    	//Capturando la hora del evento
    	long hora = System.currentTimeMillis();
    	
    	//Definiendo la notificacion, icono, texto y hora
    	notif = new Notification(icon, textoEstado, hora);
    	notif.setLatestEventInfo(getApplicationContext(), titulo, text,PendingIntent.getActivity(this, 0, new Intent(this,MainDDActivity.class), 0));
    	
    	notif.flags = Notification.FLAG_ONGOING_EVENT;
    }
    
    
    /**
	 * Metodo para obtener datos del dispositivo y mandandolas a la base de datos
	 * email, marca, modelo
	 */
    private void dispInf(){
		datosMovil[0] = Build.MANUFACTURER; //Obteniendo marca de dispositivo
		datosMovil[1] = info.getEmail(getBaseContext());	//Obteniendo el email del dispositivo
		datosMovil[2] = Build.MODEL;	//Obteniendo Modelo de dispositivo
		datosMovil[3] = Build.VERSION.RELEASE; //Obtiene Version de Android del dispositivo
    }
    
    
    /**
     * Metodo para preparar la base de dato
     */
    private void prepararBD(){
        BD = new DBHelper(this);
        try{
  			BD.createDataBase(); //creando la base de datos
  			BD.openDataBase(); 		//Abriendo la base de datos
  		}
        
  		catch(IOException e){
  			e.printStackTrace();
  			
  		}
        
    }
    
    
    /**
     * Metodo que guarga la informacion de un movimiento en un arreglo de tipo String
     */
    private void moviendo(){
    	if(gps)
    		movimiento[0] = "gps";					//Tipo de geoloclizacion
    	else if(network)
    		movimiento[0] = "network";					//Tipo de geoloclizacion
    	else
    		movimiento[0] = "Desconocido";					//Tipo de geoloclizacion
    	movimiento[1] = latitud+"";				//latitud
    	movimiento[2] = longitud+"";			//longitud
    }


    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {  
          obtenerSenalGPS();
          return START_STICKY;
    }
    

    @Override
    public void onDestroy() {
		if (mLocationManager != null)
			if (mLocationListener != null)
				mLocationManager.removeUpdates(mLocationListener);
		
        Toast.makeText(this,getResources().getString(R.string.serviceStop),Toast.LENGTH_SHORT).show();
        if(BD!=null)
        	BD.close();    
        super.onDestroy();
    }
    
    
    /**
     * handler
     */
    private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// mLocationManager.removeUpdates(mLocationListener);
			updateLocation(currentLocation);
		}
	};


	/**
	 * metodo para actualizar la localizacion
	 * 
	 * @param currentLocation
	 * @return void
	 */
	public void updateLocation(Location currentLocation) {
		if (currentLocation != null) {
			latitud = Double.parseDouble(currentLocation.getLatitude() + ""); //Obtiene la latitud
			longitud = Double.parseDouble(currentLocation.getLongitude() + ""); //Obtiene la longitud
			moviendo();
			BD.Movimientos(movimiento);
			if(Util.isNetworkConnectionOk(ServicioLoc.this)){
				BD.getRegistros();
			}
			
		}
		
	}

	
	/**
	 * Hilo de la aplicacion para cargar las cordenadas del usuario
	 */
	public void run() {
		if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && MainDDActivity.gps){
			network=false;
			gps=true;
			Looper.prepare();
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIMEOUT_MILLISEC, 0, mLocationListener);
			Looper.loop();
			Looper.myLooper().quit();
		}
		else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && MainDDActivity.network || (!MainDDActivity.network && !MainDDActivity.gps)){
			network=true;
			gps=false;
			Looper.prepare();
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIMEOUT_MILLISEC, 0, mLocationListener);
			Looper.loop();
			Looper.myLooper().quit();
		}

	}
	
	
	/**
	 * Metodo para Obtener la señal del GPS
	 */
	private void obtenerSenalGPS() {
		thread = new Thread(this);
		thread.start();
		
	}

	/**
	 * Metodo para asignar las cordenadas del usuario
	 * */
	private void setCurrentLocation(Location loc) {
		currentLocation = loc;
		
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			//Log.d("finura",loc.getAccuracy()+"");
			if (loc != null) {
				setCurrentLocation(loc);
				handler.sendEmptyMessage(0);
			}
		}

		public void onProviderDisabled(String provider) {

		}

		// @Override
		public void onProviderEnabled(String provider) {
			
		}

		// @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
	}

}