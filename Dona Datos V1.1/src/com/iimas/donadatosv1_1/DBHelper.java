package com.iimas.donadatosv1_1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.StrictMode;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	/**
	 * Declarando variables
	 */
	//Ruta de la base de datos
	@SuppressLint("SdCardPath") private static String DB_PATH="/data/data/com.iimas.donadatosv1_1/";
	private static String DB_NAME="movimientos.db";
	public SQLiteDatabase myDataBase;
	private final Context myContext;
	public static final String[] campos=new String[]{"tipoGeo","lat","lon","enviado"};
	public static final String[] camposInfo=new String[]{"enviado","marca","Email","modelo", "version"};
	public static String[] Info=new String[5];
	public static final String url1="http://livingmobs.codigo.labplc.mx/movil/android/JSON/dispositivo.php";
	public static final String url2="http://livingmobs.codigo.labplc.mx/movil/android/JSON/datosGeo.php";
	public JSONObject Json = new JSONObject(), jsonInfo = new JSONObject();		//Obejto JSON
	public JSONArray tipoGeo = new JSONArray();
	public JSONArray latitud = new JSONArray();
	public JSONArray longitud = new JSONArray();
	public JSONArray fecha = new JSONArray();
	int TIMEOUT_MILLISEC = 10000; // = 10 seconds
	ArrayList<String> id = new ArrayList<String>();
	
	/**
	 * Constructor
	 * Crea el objeto que nos permitira controla la apertura de la base de datos
	 * 
	 */
	public DBHelper(Context contexto) {
		super(contexto, DB_NAME, null, 1);
		this.myContext = contexto;
		
	}
	

	public synchronized void close() {
		if (myDataBase != null){
			myDataBase.close();
			
		}
		super.close();
		
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Metodo que abre la base de datos
	 */
	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		
	}
	
	
	/**
	 * contamos cuantos registros existen en la tabla InfoDisp para evitar duplicarlos
	 * @return
	 */
	public boolean count(){
		openDataBase();		//Abriendo la base de datos
		//Cursor c=BD.myDataBase.query("InfoDisp", BD.camposInfo, null, null, null, null, null);
		Cursor c=myDataBase.rawQuery("select id from InfoDisp", null);
		return c.moveToFirst();
	}


	/**
	 * Crea una base de datos vacia en el sistema y la reescribe 
	 * con nuestro fichero de base de datos
	 */
	public void createDataBase() throws IOException{
		boolean dbExist=checkDataBase();
		if(dbExist){
			//Si Existe no hacemos nada!
			
		}
		else {
			/**
			 * Llamando a este metodo se crea la base de datos vacia en la ruta
			 * por defecto del sistema de nuestra aplicacion por lo que pdoremos
			 * sobreescribirla con nuestra base de datos.
			 */
			this.getReadableDatabase();
			
			try {
				copyDataBase();
				
			} catch (IOException e){
				throw new Error("Error copiando database");
				
			}
			
		}
		
	}
	
	
	/**
	 * Metodo  que verifica si ya se creo la base de datos
	 * @return
	 */
	private boolean checkDataBase(){
		SQLiteDatabase checkDB=null; boolean check;
		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
			
		} catch (SQLException e){
			//Base de datos no creada todavia
			
		}
		check = checkDB != null ? true : false;
		return check;
		
	}
	
	
	/**
	 * Copea la base de datos al dispositivo	
	 * @throws IOException
	 */
	private void copyDataBase() throws IOException{
		/**
		 * Ruta a la base de datos vacia recien creada
		 */
		OutputStream myOutputDB = new FileOutputStream("" + DB_PATH + DB_NAME);
		/**
		 * Abrimos el fichero de base de datos como entrada
		 */
		InputStream myInputDB = myContext.getAssets().open(DB_NAME);
		/**
		 * Transferimos los bytes desde el fichero de entrada al de salida
		 */
		byte[] buffer = new byte[1024];
		int lenght;
		while((lenght = myInputDB.read(buffer))>0){
			myOutputDB.write(buffer,0,lenght);
			
		}
		
		/**
		 * Liberamos los stream
		 */
		myOutputDB.flush();
		myOutputDB.close();
		myInputDB.close();
		
	}


	/**
	  * Insertamos la informacion del dispositivo
	  * @param info
	  */
	public void InfoDisp(String[] info){
		String Result="";
		openDataBase();		//Abriendo la base de datos
		ContentValues nuevoRegistro = new ContentValues();
		nuevoRegistro.put(camposInfo[0],0);			//Enviado
		nuevoRegistro.put(camposInfo[1],info[0]);	//Marca del dispositivo
		nuevoRegistro.put(camposInfo[2],info[1]);	//Email del dispositivo
		nuevoRegistro.put(camposInfo[3],info[2]);	//Modelo del dispositivo
		nuevoRegistro.put(camposInfo[4],info[3]);	//Version de android
		myDataBase.insert("InfoDisp", null, nuevoRegistro);	//Insertando el registro en la base de datos
		
		try{
			jsonInfo.put(camposInfo[1],info[0]);	//Marca del dispositivo
			jsonInfo.put(camposInfo[2],info[1]);	//Email del dispositivo
			jsonInfo.put(camposInfo[3],info[2]);	//Modelo del dispositivo
			jsonInfo.put(camposInfo[4],info[3]);	//Version de android
			Result = post(url1, jsonInfo);
			System.out.println(jsonInfo.toString());
			System.out.println(Result);
		} catch (JSONException e) {	
			System.out.println("Error envio de datos moviles\n" + e);
		}
		
		if(Result.contains("1") || Result.contains("Existe")){
			//Actualizar un registro
			System.out.println("Informacion del dispositivo insertada con exito");
			myDataBase.execSQL("UPDATE InfoDisp SET enviado=1 WHERE id=1");
			
		}
		else{
			System.out.println("Informacion no insertada verifiqe su conexion a internet");
			
		}
		myDataBase.close(); //Cerrando la base de datos	
	}
	

	/**
	 * insertamos en la base de datos la latitud y longitud obtenida
	 * @param mov
	 */
	public void Movimientos(String[] mov){
		openDataBase();
		ContentValues nuevoRegistro = new ContentValues();
		nuevoRegistro.put(campos[0],mov[0]);	//Tipo de geolocalizacion
		nuevoRegistro.put(campos[1],mov[1]);	//latitud
		nuevoRegistro.put(campos[2],mov[2]);	//Longitud
		nuevoRegistro.put(campos[3],0);			//Enviado
		myDataBase.insert("moviendo", null, nuevoRegistro);	//Insertando el registro en la base de datos
		myDataBase.close();
		
	}
	
	
	/**
	 * metodo que manda los datos que aun no se han mandado al servidor y si el usuraio
	 * aun no esta registrado intenta regitrarlo.
	 */
	public void getRegistros(){
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT id,tipoGeo,lat,lon,fecha FROM moviendo WHERE enviado=0", null);
		Cursor cur = myDataBase.rawQuery("SELECT marca,email,modelo,version,enviado FROM InfoDisp WHERE id=1", null);
		boolean informacion=false;
		String inserto="", insert=""; 

		if(cur.moveToFirst()){
			do{
				Info[0]=cur.getString(0);	//Marca del dispositivo
				Info[1]=cur.getString(1);	//Email del dispositivo
				Info[2]=cur.getString(2);	//Modelo del dispositivo
				Info[3]=cur.getString(3);	//Version de android
				Info[4]=cur.getString(4);	//enviado de android
				if(Info[4].equals("0"))	//Hay informacion para insertar
					informacion=true;
			}while(cur.moveToNext());	
		}
		
		if(informacion){//hay informacion para insertar
			inserto=post(url1, jsonInfo);
			if(inserto.equals("1") || inserto.equals("Existe")){
				//Actualizar un registro
				System.out.println("Informacion del dispositivo insertada con exito");
				myDataBase.execSQL("UPDATE InfoDisp SET enviado=1 WHERE id="+1);
				
			}
			
			System.out.println("Info Dispositivo:"+inserto);
			
		}
		
		if(c.moveToFirst()){
			//recorriendo el cursos para obtener los registros
			do{
				id.add(c.getString(0));
				tipoGeo.put(c.getString(1));
				latitud.put(c.getString(2));
				longitud.put(c.getString(3));
				fecha.put(c.getString(4));
			} while(c.moveToNext());
		}
		
		try{
			Json.put("tipoG", tipoGeo);
			Json.put("lat", latitud);
			Json.put("lon", longitud);
			Json.put("fecha", fecha);
			Json.put("correo", Info[1]);
			System.out.println(Info[1]);
			System.out.println(Json.toString());
			insert = post(url2, Json);
			
		} catch (JSONException e) {	
			System.out.println("Error envio de datos moviles\n" + e);
			
		}
				
		System.out.println("insert datos Geo:"+insert);
		insert = insert.trim();
		if(insert.equals("1")){
			//Actualizar un registro
			for(int i=0; i<id.size(); i++){
				myDataBase.execSQL("UPDATE moviendo SET enviado=1 WHERE id="+id.get(i));
				System.out.println("Modificado con Exito");
			}
		}
		NewJson();
		id.clear();
		
	}
	
	
	/**
	 * Vaciando los Json
	 */
	public void NewJson(){
		Json = new JSONObject();
		tipoGeo = new JSONArray();
		latitud = new JSONArray();
		longitud = new JSONArray();
		fecha = new JSONArray();
		
	}
	
	
	/**
	 * Metodo que envia los datos al servidor via JSON
	 * @param url
	 * @return result
	 */
	public String post(String url, JSONObject json){
		String result="";
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		
		try{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost(url);
		
			request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
			request.setHeader("JSON", json.toString());
			
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				InputStream instream = entity.getContent();

				result = RestClient.convertStreamToString(instream);
				Log.i("Read from server", result);
				System.out.println(result);
			}

		} catch (Throwable t){
			System.out.println("Algo salio mal "+ t.toString());
		}
		
		return result;
	}

}
