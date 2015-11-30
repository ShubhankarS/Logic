package com.example.shubhankar.pillboxlogic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    String input = "";
    String output = "";
    byte[] x = new byte[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText entered = (EditText) findViewById(R.id.editText);
        final TextView result = (TextView) findViewById(R.id.textView);
        Button add = (Button) findViewById(R.id.button);
        Button clear = (Button) findViewById(R.id.button2);
        Button scannow = (Button) findViewById(R.id.scan_now);

        result.setText(output);

        x[0] = (byte) 0x1a;
        x[1] = (byte) 0xff;
        x[2] = (byte) 0x2d;
        x[3] = (byte) 0x41;
        x[4] = (byte) 0xb6;
        x[5] = (byte) 0x96;
        x[6] = (byte) 0xd8;
        x[7] = (byte) 0xff;

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp = Integer.parseInt(entered.getText().toString(), 16);
                output = result.getText().toString().concat(" " + String.format("%08d",
                        Integer.parseInt(Integer.toBinaryString(temp))));
                result.setText(output);
                entered.setText("");

                String z = bytesToHex(x);
                Log.d("Converted bytes", z);
            }
        });

        scannow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scan = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(scan);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                output = "";
                input = "";
                entered.setText(input);
                result.setText(output);
            }
        });
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xff;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//    public static String toHexString(byte[] bytes) {
//        char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
//        char[] hexChars = new char[bytes.length * 2];
//        int v;
//        for (int j = 0; j < bytes.length; j++) {
//            v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v / 16];
//            hexChars[j * 2 + 1] = hexArray[v % 16];
//        }
//        return new String(hexChars);
//    }
}
