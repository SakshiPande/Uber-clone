package uber.com.uber.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

import uber.com.uber.R;


public class CommonUtils {

    public static boolean isErrorDialogVisible = false;

    public static ProgressDialog showProgressBar(Context context, String str_message, Boolean isCancelable) {
        final ProgressDialog pdialog = new ProgressDialog(context);
        pdialog.setMessage(str_message);
        pdialog.setCancelable(isCancelable);
        pdialog.show();
        return pdialog;

    }

    public static void showAlertDialog(Context context, String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_alert);
        builder.setMessage(message);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isErrorDialogVisible = false;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        isErrorDialogVisible = true;
    }

    public static void showErrorDialog(Context context, String message, String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_alert);
        builder.setMessage(message);

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isErrorDialogVisible = false;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        isErrorDialogVisible = true;
    }
    public static void showErrorDialogLogin(final Context context, String message, String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_alert);
        builder.setMessage(message);

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isErrorDialogVisible = false;
//                Intent intent=new Intent(context, LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        isErrorDialogVisible = true;
    }

    public static AlertDialog.Builder showNoNetworkErrorDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Oops!! Something Went Wrong.");
        builder.setMessage("Check Your Internet Connection and try again.");
        builder.setIcon(R.drawable.ic_alert);
        return builder;
    }


    public static String ucfirst(String input){
        String output = input.substring(0, 1).toUpperCase() + input.substring(1);
        return output;
    }

    public static void showSnackBarNoAction(View view, String message){

        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_SHORT);

        snackbar.show();
    }

    public static long getUserCredentialsValidity(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(calendar.getTimeInMillis()));
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime().getTime();
    }

//
}
