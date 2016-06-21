package com.example.reativos.oscar;

import android.app.AlertDialog;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Log;


public class CodeEditor extends ActionBarActivity {
    Button send;
    Button addCommand;
    Button cleanCode;
    String address = null;
    public List<Command> codeMain = new ArrayList<>();
    final Map<String, List<Command>> commandLists = new HashMap<>();
    final Map<String, ArrayAdapter<Command>> adapters = new HashMap<>();
    final Map<String, Character> reactionTypes = new HashMap<>();

    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set up global app data source
         */
        final GlobalAppDataStore dataStore = GlobalAppDataStore.getInstance();
        dataStore.initialize(getApplicationContext());

        Intent newint = getIntent();
        //address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        setContentView(R.layout.activity_code_editor);

        //new ConnectBT().execute(); //Call the class to connect


        // Set up tab view
        final TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();
        resetTabs(host, commandLists);

        // Set up lists
        for(Map.Entry<String, List<Command>> commandList : commandLists.entrySet()) {
            ArrayAdapter<Command> adapter =
                    new ArrayAdapter<Command>(this, android.R.layout.simple_list_item_1,
                            commandList.getValue());

            if (commandList.getKey().equals("MAIN"))
                ((ListView) findViewById(R.id.mainCodeList)).setAdapter(adapter);
            else if (commandList.getKey().equals("REACTION 1"))
                ((ListView) findViewById(R.id.reactionCodeList1)).setAdapter(adapter);
            else if (commandList.getKey().equals("REACTION 2"))
                ((ListView) findViewById(R.id.reactionCodeList2)).setAdapter(adapter);

            adapters.put(commandList.getKey(), adapter);
        }

        // Send message when button is clicked!
        send = (Button) findViewById(R.id.SendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentTabTag = host.getCurrentTabTag();

//                try {
                    // Iterate over list of commands to generate code
                    String code = "";
                    for (Map.Entry<String, List<Command>> commandList : commandLists.entrySet()) {
                        if (commandList.getValue().size() > 0) {
                            code += reactionTypes.get(commandList.getKey());
                            for (Command c : commandLists.get(currentTabTag)) {
                                code += c.serialize();
                            }
                            code += "|";
                        }
                    }
                    code += "/";
                    Log.i("MainActivity", code);
                //    btSocket.getOutputStream().write(code.getBytes());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });

        // Send message when button is clicked!
        addCommand = (Button) findViewById(R.id.addCommand);
        addCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddCommand command = new AddCommand();
                command.show(getFragmentManager(), "Add new command");

                int commandToRead = dataStore.getInt("commandToRead" );

                final String currentTabTag = host.getCurrentTabTag();

                command.setOnDismissListener(new AddCommand.OnDismissListener() {
                    @Override
                    public void onDismiss(AddCommand myDialogFragment) {
                        final AddParam param = new AddParam();
                        param.show(getFragmentManager(), "Add new param");
                        param.setOnDismissListener(new AddParam.OnDismissListener() {
                            @Override
                            public void onDismiss(AddParam myDialogFragment) {
                                String command_type = dataStore.getString("type");
                                int command_param = dataStore.getInt("param");
                                if (command_type != null ) {
                                    Command newCommand = new Command(command_type, command_param);
                                    commandLists.get(currentTabTag).add(newCommand);
                                    adapters.get(currentTabTag).notifyDataSetChanged();
                            }
                        }
                        });
                    }
                });
                //}
            }
        });


        // Send message when button is clicked!
        cleanCode = (Button) findViewById(R.id.cleanCode);
        cleanCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentTabTag = host.getCurrentTabTag();
                commandLists.get(currentTabTag).clear();
                adapters.get(currentTabTag).notifyDataSetChanged();
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Digite um comando!");
            }
        }
        finish(); //return to the first layout

    }

    // Reset all the tabs
    private void resetTabs(TabHost host, Map<String, List<Command>> commandLists) {
        // Set up the tabs
        TabHost.TabSpec spec = host.newTabSpec("MAIN");
        spec.setContent(R.id.mainTab);
        spec.setIndicator("MAIN");
        commandLists.put(spec.getTag(), new ArrayList<Command>());
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("REACTION 1");
        spec.setContent(R.id.reactionTab1);
        spec.setIndicator("REACTION 1");
        commandLists.put(spec.getTag(), new ArrayList<Command>());
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("REACTION 2");
        spec.setContent(R.id.reactionTab2);
        spec.setIndicator("REACTION 2");
        commandLists.put(spec.getTag(), new ArrayList<Command>());
        host.addTab(spec);
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CodeEditor Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.reativos.oscar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CodeEditor Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.reativos.oscar/http/host/path")
        );
        try {
            btSocket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(CodeEditor.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
