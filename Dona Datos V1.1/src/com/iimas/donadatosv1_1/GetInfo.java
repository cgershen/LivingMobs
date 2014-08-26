package com.iimas.donadatosv1_1;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class GetInfo {

	/**
	 * Metodo que obtiene la cuenta de google del dispositivo
	 * @param accountManager
	 * @return
	 */
    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
          account = accounts[0];      
        } else {
          account = null;
        }
        return account;
      }
    
    /**
     * Metodo para obtener el email del dipositivo
     * @param context
     * @return
     */
    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context); 
        Account account = getAccount(accountManager);

        if (account == null) {
          return "No hay cuentas.";
        } else {
          return account.name;
        }
    }
}
