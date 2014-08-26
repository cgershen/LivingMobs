package com.iimas.donadatosv1_1;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainDDActivity extends ActionBarActivity {

	FragmentManager fragmentManager = getSupportFragmentManager();
	/**
	 * Declarando variables
	 */
	private DBHelper BD; //Objeto de la clase DBHelper (Base de datos)
	TextView TvTipoLoc;			//Textview del tipo de localizacion
	RadioButton RbGps;			//RadioButton de gps
	RadioButton RbNetwork;		//RadioButton de Network
	RadioGroup rGroup;			//RadioGroup
	Button btnIniciar;			//Button iniciar servicio
	Button btnTerminos;
	CheckBox checkTerminos;
	boolean aceptar = false;
	Button btnContinuar;
	LinearLayout boxTermsAndConditions, boxTipoLocalizacion;
	String Terminos = "";

	//Typeface fontOstrich;		//Fuente de la palicacion
	Typeface fontHoboStd;
	public LocationManager mLocationManager;		//Objeto LocationManager
	public static boolean gps=false, network = false;
	
	/**
	 * metodo onCreate()
	 * Se ejecutara cada vez que se inicie la aplicación
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_dd);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		prepararBD();	//Preparando la base de datos para poder usarla
		
		boxTermsAndConditions = (LinearLayout) findViewById(R.id.boxTermsAndConditions);
		boxTipoLocalizacion = (LinearLayout) findViewById(R.id.boxTipoLocalizacion);
		boxTipoLocalizacion.setVisibility(View.GONE);
		checkTerminos = (CheckBox) findViewById(R.id.checkTerminos);
		btnContinuar = (Button) findViewById(R.id.botonContinuar);
		btnTerminos = (Button) findViewById(R.id.botonTerminos);
		
		// Inicializando los TextView
		TvTipoLoc = (TextView) findViewById(R.id.TVTipoLoc);
		TvTipoLoc.setVisibility(View.GONE);
		// Inicializando los RadioButton y radioGroup
		rGroup = (RadioGroup) findViewById(R.id.RGLoc);
		RbGps = (RadioButton) findViewById(R.id.RBGps);
		RbNetwork = (RadioButton) findViewById(R.id.RBNetwork);
		// Inicializando el Button
		btnIniciar = (Button) findViewById(R.id.BtnIniciar);
		btnIniciar.setVisibility(View.GONE);
		btnIniciar.setEnabled(false);
		//Inicializando el LocationManager
		
        if(BD.count()){
        	goToContinue();
        }

		
		//fontOstrich = Typeface.createFromAsset(getAssets(), "ostrich-black.ttf");
		fontHoboStd = Typeface.createFromAsset(getAssets(), "HoboStd.otf");
		asignarFuente();	//Llamada al metodo asignarFuente()
		
		// Reaccionar a eventos del CheckBox
		checkTerminos.setOnClickListener(new CheckBox.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkTerminos.isChecked())aceptar = true;
				else aceptar = false;
			}
		});
		// Fin respecto al checkbox
		// Reaccionar a eventos del boton botonContinuar
		btnContinuar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (aceptar) goToContinue();
				else {imprimir(getResources().getString(R.string.acceptTerms));}
			}
		});// Fin respecto al boton botonContinuar

		
		
		//Accion al presionar el RadioButton GPS
		RbGps.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gps=true; network=false;
				if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					showDialogGPS(getResources().getString(R.string.gpsApagado),
							getResources().getString(R.string.gpsActivated));
				}				
				btnIniciar.setEnabled(true);
			}
		});
		
		
		//Accion al presionar el RadioButton Network
		RbNetwork.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gps=false; network=true;
				btnIniciar.setEnabled(true);
			}
		});
		
		//Accion al presionar el button Iniciar Servicio
		btnIniciar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				  /**
				   * Se inicia el servicio de geolocalizacion
				   */
				startService(new Intent(MainDDActivity.this,ServicioLoc.class));
				btnIniciar.setEnabled(false);	//Bloquea el boton
			}
		});
		
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

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_dd, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menuSalir:
			//CREAMOS OBJETO PARA LLAMAR CUADRO CONFIRMACION PARA SALIR
			DialgoConfirmSalir dialogoConfirmacion = new DialgoConfirmSalir();
			dialogoConfirmacion.show(fragmentManager, "tagAlerta");
			break;
		case R.id.menuAcercaDe:
			//CREAMOS OBJETO PARA LLAMAR CUADRO DIALGO ACERCA DE
			DialogoAceraDe dialogoAcercaDe = new DialogoAceraDe();
			dialogoAcercaDe.show(fragmentManager, "tagAlerta");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Obteniendo los terminos desde un xml
	 * @return Terminos (String)
	 */
	public String getTerminos(){
		String terminos="";
		terminos=getResources().getString(R.string.generalidades)+"\n\n";
		terminos+=getResources().getString(R.string.definiciones)+"\n\n";
		terminos+=getResources().getString(R.string.proteccion)+"\n\n";
		terminos+=getResources().getString(R.string.dd)+"\n\n";
		terminos+=getResources().getString(R.string.ddusodatost);
		terminos+=getResources().getString(R.string.ddusodatosm)+"\n\n";
		terminos+=getResources().getString(R.string.seguridadt);
		terminos+=getResources().getString(R.string.seguridadm)+"\n\n";
		terminos+=getResources().getString(R.string.licencia)+"\n\n";
		terminos+=getResources().getString(R.string.presente)+"\n\n";
		terminos+=getResources().getString(R.string.condiciones)+"\n\n";
		terminos+=getResources().getString(R.string.aplicacion)+"\n\n";
		terminos+=getResources().getString(R.string.usuario)+"\n\n";
		terminos+=getResources().getString(R.string.disponibilidad)+"\n\n";
		terminos+=getResources().getString(R.string.living)+"\n\n";
		terminos+=getResources().getString(R.string.responsable)+"\n\n";
		terminos+=getResources().getString(R.string.requisitos)+"\n\n";
		terminos+=getResources().getString(R.string.reqsoft)+"\n\n";
		terminos+=getResources().getString(R.string.version)+"\n\n";
		terminos+=getResources().getString(R.string.finalizar)+"\n\n";
		terminos+=getResources().getString(R.string.fin)+"\n\n";
		terminos+=getResources().getString(R.string.limitacion)+"\n\n";
		terminos+=getResources().getString(R.string.noresponsable)+"\n\n";
		terminos+=getResources().getString(R.string.easy)+"\n\n";
		terminos+=getResources().getString(R.string.exencion);
		
		return terminos;
	}
	
	
	// Evento para mostrar el cuadro de dialogo de los terminos y condiciones
	public void showTermsAndConditions(View v) {
		if(Terminos.equals("")) Terminos = getTerminos();
		AlertDialog LDialog = new AlertDialog.Builder(v.getContext())
				.setTitle(getResources().getString(R.string.botonAceptarTerminos))
				.setMessage(Terminos)
				.setPositiveButton(android.R.string.ok, null).create();
		LDialog.show();
	}// fin de metodo mostrar terminos y condiciones
	// Metodo para ocultar los componentes principales y muestra los componentes para configurar la localizacion
	public void goToContinue() {
		boxTermsAndConditions.setVisibility(View.GONE);
		btnContinuar.setVisibility(View.GONE);
		
		boxTipoLocalizacion.setVisibility(View.VISIBLE);
		TvTipoLoc.setVisibility(View.VISIBLE);
		btnIniciar.setVisibility(View.VISIBLE);
	}

	public void imprimir(String dato) {
		Toast mensaje = Toast.makeText(this, dato, Toast.LENGTH_SHORT);
		mensaje.show();
	}


	/**
	 * asignarFuente()
	 * 	Aplica la fuente ostrich-black a la interfaz de usuario
	 */
	protected void asignarFuente() {
		TvTipoLoc.setTypeface(fontHoboStd);
		RbGps.setTypeface(fontHoboStd);
		RbNetwork.setTypeface(fontHoboStd);
		btnIniciar.setTypeface(fontHoboStd);
		checkTerminos.setTypeface(fontHoboStd);
		btnContinuar.setTypeface(fontHoboStd);
		btnTerminos.setTypeface(fontHoboStd);
	}
	
	/**
	 * Muestra el dialogo en caso de que el GPS este apagado
	 * 
	 * @param titulo Titulo del dialogo
	 * @param message Mensaje del dialogo
	 */
	public void showDialogGPS(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainDDActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
		builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivity(settingsIntent);
			}
		});
		
		builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				RbGps.setEnabled(true);
				RbNetwork.setEnabled(true);
				btnIniciar.setEnabled(false);
				rGroup.clearCheck();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
	}
	
}