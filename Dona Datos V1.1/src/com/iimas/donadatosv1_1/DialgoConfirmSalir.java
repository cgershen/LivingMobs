package com.iimas.donadatosv1_1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DialgoConfirmSalir extends DialogFragment{

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String message = getResources().getString(R.string.salirApp);
		String tittle = getResources().getString(R.string.salir);
		String acept = getResources().getString(R.string.aceptar);
		String cancel = getResources().getString(R.string.cancelar);

		builder.setMessage(message)
		.setIcon(R.drawable.stop_24)
		.setTitle(tittle)
		.setPositiveButton(acept, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					try {
						((MainDDActivity)getActivity()).finish();
					} catch (ClassCastException e) {}
				}
			})
			.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			return builder.create();
	}
	
}
