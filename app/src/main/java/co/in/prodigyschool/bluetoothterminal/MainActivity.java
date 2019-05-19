package co.in.prodigyschool.bluetoothterminal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DeviceCallback;

public class MainActivity extends AppCompatActivity {

    String address, name, dataTBS, nl, cr;
    Bluetooth bluetoothObject;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> mBTArrayAdapter;
    int sentDataNumber=0;


    public void composeEmail(String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","shlokj@gmail.com", null));
        intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "shlokj@gmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bluetooth Terminal app");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void sendEmail () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send a message: ");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        FrameLayout container = new FrameLayout(getApplicationContext());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 20;
        params.rightMargin = 20;
        input.setLayoutParams(params);
        container.addView(input);
//        builder.setMessage("This will direct you to send a mail to the developer");
        builder.setView(container);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String Message = input.getText().toString();
                composeEmail(Message);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }



    public void displayPairedDevices(){
        mBTArrayAdapter.clear();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
            mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_btdevices, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Select your device");
        alertDialog.setMessage("Your device must be paired for it to appear in this list. If this is the first time you are using this Android device with your Bluetooth module, you will need to pair it in settings.");
        alertDialog.setCancelable(false);
        alertDialog.setNeutralButton("Open settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
                displayPairedDevices();
            }
        });
        ListView devicesListView = (ListView) convertView.findViewById(R.id.mDevicesListView);
        devicesListView.setAdapter(mBTArrayAdapter);
        final android.app.AlertDialog dialog = alertDialog.show();
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                address = info.substring(info.length() - 17);
                name = info.substring(0, info.length() - 17);
                dialog.dismiss();
                progressDialog.setMessage("Connecting to "+name+" ("+address+")");
                progressDialog.setTitle("Connecting");
                progressDialog.setCancelable(false);
                progressDialog.show();
                bluetoothObject.connectToAddress(address);
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            displayPairedDevices();
        }
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        ActionBar actionBar = getSupportActionBar();
        bluetoothObject = new Bluetooth(getApplicationContext());
        bluetoothObject.onStart();
        bluetoothObject.enable();
        final TextView sentData = (TextView) findViewById(R.id.sent_data);
        sentData.setMovementMethod(new ScrollingMovementMethod());
        bluetoothObject.setBluetoothCallback(new BluetoothCallback() {
            @Override
            public void onBluetoothTurningOn() { }

            @Override
            public void onBluetoothOn() {
                displayPairedDevices();
            }

            @Override
            public void onBluetoothTurningOff() { }

            @Override
            public void onBluetoothOff() { }

            @Override
            public void onUserDeniedActivation() { }
        });
        bluetoothObject.setDeviceCallback(new DeviceCallback() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
                progressDialog.dismiss();
                sentData.append("Connected to "+name+"\n\n");
            }

            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) { }

            @Override
            public void onMessage(String message) { }
            @Override
            public void onError(String message) { }
            @Override
            public void onConnectError(BluetoothDevice device, String message) { }
        });

        progressDialog = new ProgressDialog(this);
        final Vibrator vibrator = (Vibrator) getSystemService(MainActivity.this.VIBRATOR_SERVICE);
        final Switch newline = (Switch) findViewById(R.id.nl);
        final Switch carreturn = (Switch) findViewById(R.id.cr);
        nl = "";
        cr = "";
        newline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newline.isChecked()) {
                    nl = "\n";
                }
                else {
                    nl = "";
                }
            }
        });

        carreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (carreturn.isChecked()) {
                    cr = "\r";
                }
                else {
                    cr = "";
                }
            }
        });

        Button send = (Button) findViewById(R.id.send_data);
        final EditText data = (EditText) findViewById(R.id.data_tbs);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataTBS = data.getText().toString();
                if (bluetoothObject.isConnected()) {
                    data.setText("");
                    bluetoothObject.send(dataTBS+nl+cr);
//                    Toast.makeText(getApplicationContext(), "Data sent", Toast.LENGTH_SHORT).show();
                    sentDataNumber++;
                    sentData.append(sentDataNumber + ". " + dataTBS);
                    vibrator.vibrate(40);
                }
                else if (!bluetoothObject.isConnected()) {
                    vibrator.vibrate(30);
//                    sentDataNumber++;
//                    sentData.append(sentDataNumber + ". " + dataTBS + "\n");
                    Toast.makeText(getApplicationContext(), "Bluetooth device not connected", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothObject.isConnected()){
            displayPairedDevices();
        }
    }

    private void bluetoothOn(){
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        else{
//            bluetoothObject = new Bluetooth(getApplicationContext());
//            bluetoothObject.onStart();
//            bluetoothObject.enable();
//            mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
//            displayPairedDevices();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        switch (id) {
            case R.id.disconnect:
                bluetoothObject.disconnect();
                break;
            case R.id.chooseDevice:
                displayPairedDevices();
                break;
            case R.id.aboutDeveloper:
                displayAboutDeveloper();
                break;
            case R.id.btsettings:
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
        }
        return true;
    }

    public void displayAboutDeveloper() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage(R.string.about);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Contact", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendEmail();
            }
        });
//        builder.setIcon(R.drawable.ic_launcher);
        builder.show();
    }

}
