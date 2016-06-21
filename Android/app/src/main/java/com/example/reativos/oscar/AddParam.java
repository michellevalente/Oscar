package com.example.reativos.oscar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.text.InputType;
import android.widget.EditText;

/*
 * Created by michellevalente on 19/06/16.
 */
public class AddParam extends DialogFragment {

    public interface OnDismissListener {
        void onDismiss(AddParam myDialogFragment);
    }
    //create Pointer and setter to it
    private OnDismissListener onDismissListener;
    public void setOnDismissListener(OnDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }

    //Call it on the dialogFragment onDissmiss
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final GlobalAppDataStore dataStore = GlobalAppDataStore.getInstance();
        final String[] temp = new String[2];
        final String type = dataStore.getString("type");

//        ArrayList<String> params = new ArrayList<>();

        if (type.equals("stop") || type.equals("loop")) {
            dataStore.setInt("param", -1);
            dismiss();
            return builder.create();
        }
        else if (type.equals("ledr") || type.equals("ledb"))
        {
            temp[0] = "LED On";
            temp[1] = "LED Off";

        }
        else if (type.equals("move") )
        {
            temp[0] = "Forward";
            temp[1] = "Backward";
        }
        else if (type.equals("turn") )
        {
            temp[0] = "Right";
            temp[1] = "Left";
        }
        else if (type.equals("buzz") || type.equals("wait"))
        {
            builder.setTitle("Type the time (ms): ");
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
            builder.setView(input);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    dataStore.setInt("param", Integer.valueOf(input.getText().toString()));
                    dismiss();
                }
            });
            return builder.create();
        }

        builder.setTitle("Choose an option");
        builder.setItems(temp, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                if(type.equals("ledr") || type.equals("ledb") || type.equals("move")
                        || type.equals("turn"))
                {
                    if(which == 0)
                        dataStore.setInt("param", 1);
                    else if(which == 1)
                        dataStore.setInt("param", 0);
                }
                if(type.equals("buzz") || type.equals("wait"))
                {
                    if(which == 0)
                        dataStore.setInt("param", 500);
                    else if(which == 1)
                        dataStore.setInt("param", 1000);
                }

            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}