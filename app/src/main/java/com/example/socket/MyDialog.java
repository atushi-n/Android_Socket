package com.example.socket;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class MyDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Dialogの属性とかを設定するための Builder を生成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.setting_dialog, null);
        builder.setView(layout);

        //初期値の読み込み
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        String defaultAddr = sharedPref.getString("addr", "empty");
        int defaultPort = sharedPref.getInt("port", -1);


        EditText dAddrEditText = layout.findViewById(R.id.dAddrEditText);
        EditText dPortEditText = layout.findViewById(R.id.dPortEditText);


        //初期値が存在すればセットする
        if ((defaultPort != -1)) {
            dPortEditText.setText(((Integer) defaultPort).toString());
        }
        if (!defaultAddr.equals("empty")) {
            dAddrEditText.setText(defaultAddr);
        }


        //buliderにいろいろセットする
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("addr", dAddrEditText.getText().toString());
                editor.putInt("port", Integer.parseInt(dPortEditText.getText().toString()));
                editor.apply();
            }
        });

        builder.setNegativeButton("やめる", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        this.setCancelable(false);
        return builder.create();
    }
}
