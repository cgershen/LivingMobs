package com.iimas.donadatosv1_1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DialogoAceraDe extends DialogFragment {

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder =
				new AlertDialog.Builder(getActivity());

		String message = getResources().getString(R.string.acercaDeM);
		String tittle = getResources().getString(R.string.acercaDe);
		
		builder.setMessage(message)
		.setIcon(R.drawable.help_24)
		.setTitle(tittle)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
}
