package com.example.corey.smelly;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.corey.smelly.ElectronicNose;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
  // private static final int PROGRESS = 0x1;
  //  private ProgressBar mProgress;
    private int mProgressStatus = 0;
    private TextView text;
    public ImageView image;
    public TextView fruitname;

  //  private Handler mHandler = new Handler();//handler for mock progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    //    mProgress = (ProgressBar) findViewById(R.id.progressBar);
    //    mProgress.setMax(1000000000);
        text = (TextView)findViewById(R.id.textView);
        text.setVisibility(View.INVISIBLE);


// for mock progress bar
       /* new Thread(new Runnable() {
            public void run() {
                mProgress.setProgress(10);

                while (mProgressStatus < 1000000000) {
                    mProgressStatus = smelling() ;
                    text.setVisibility(View.VISIBLE);
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);

                        }
                    });
                }
            }
        }).start();*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {




                final ElectronicNose enose = new ElectronicNose("98:76:B6:00:6A:BA");


                int con = enose.connect();

                String v = enose.getVersionString();

                Log.d(TAG, "Got version string : [" + v + "]");

              //  before we collect the data, lets configure the timing
                enose.setBaselineTime(2000);
                enose.setSampleTime(3000);
                enose.setPurgeTime(30000);
                enose.setSettleTime(30000);

                //a list of strings containing all the data points we are interested in.
             ArrayList<String> td = enose.collectData();


                if (con == -1) {
                   Log.d(TAG, "Failed to connect");
                    return;
                }



                Log.d(TAG, String.format("Collected %d samples", td.size()));

                enose.disconnect();



                //Now lets try to connect to the server

                CloudNose cn = new CloudNose(td);
                 final Thread t = new Thread(cn);
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final String test = cn.getResult();

                runOnUiThread(new Runnable() {

                    public void run() {


                        setContentView(R.layout.serverresponse);
// Retreiving the images of fruits,based on the input collected

                        image = (ImageView) findViewById(R.id.imageView);
                        fruitname = (TextView) findViewById(R.id.textView2);

                         if(test.contains("class=1 (Banana)"))
                         {
                          image.setImageResource(R.drawable.banana);
                          fruitname.setText("Banana");

                         }
                         else if(test.contains("class=2 (Clear Raspberry)"))
                         {
                             image.setImageResource(R.drawable.raspberry);
                             fruitname.setText("Raspberry");

                         }

                         else if(test.contains("class=3 (Coffee)"))
                         {
                             image.setImageResource(R.drawable.coffee);
                             fruitname.setText("Coffee");

                         }
                         else if(test.contains("class=4 (Grape)"))
                         {
                             image.setImageResource(R.drawable.grapes);
                             fruitname.setText("Grapes");

                         }
                        else
                         {
                             fruitname.setText("Unrecognized");
                         }
                    }

                });
            }
        });

    }
    // mock progress bar
   /* public  int smelling(){

        for(int i=0;i<= 1000000000 ;i++) {

            mProgress.setProgress(i*10);

       }

        return 1000000000;

    }*/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
