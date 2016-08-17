package com.example.ianribas.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View.OnClickListener showMessageListener = new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, String.format(getString(R.string.message), ((Button) v).getText()), Toast.LENGTH_LONG).show();
            }
        };

        findViewById(R.id.btPopularMovies).setOnClickListener(showMessageListener);
        findViewById(R.id.btStockHawk).setOnClickListener(showMessageListener);
        findViewById(R.id.btBuildBigger).setOnClickListener(showMessageListener);
        findViewById(R.id.btMaterial).setOnClickListener(showMessageListener);
        findViewById(R.id.btGoUbiquitous).setOnClickListener(showMessageListener);
        findViewById(R.id.btCapstone).setOnClickListener(showMessageListener);
    }

}
