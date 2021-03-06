package com.example.reativos.oscar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;

/**
 * Created by michellevalente on 19/06/16.
 */
public class AddCommand extends DialogFragment {
    AlertDialog.Builder builder;

    public interface OnDismissListener {
        void onDismiss(AddCommand myDialogFragment);
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
        builder = new AlertDialog.Builder(getActivity());

        final String[] type_commands = {
                "loop",
                "ledr",
                "ledb",
                "move",
                "turn",
                "buzz",
                "wait",
                "stop"
        };

        builder.setTitle("Choose the type of command");
        builder.setItems(type_commands, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int which) {
                GlobalAppDataStore dataStore = GlobalAppDataStore.getInstance();
                dataStore.setString("type", type_commands[which]);

            }
        });

                // Create the AlertDialog object and return it
        return builder.create();
    }
}
