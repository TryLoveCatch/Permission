package com.xiaodu.permission;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xiaodu.permission.util2.PermissionActivity;

public class MainActivity extends PermissionActivity {

    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button)findViewById(R.id.btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(new CheckPermListener() {
                    @Override
                    public void superPermission() {
                        Toast.makeText(MainActivity.this, "相机可用", 1).show();
                    }
                },R.string.carmera_msg, Manifest.permission.CAMERA);
            }
        });
    }
}
