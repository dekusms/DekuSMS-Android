package com.example.swob_deku.Services;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.swob_deku.DefaultCheckActivity;
import com.example.swob_deku.R;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.DefaultExceptionHandler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RMQConnectionService extends Service {

    ConnectionFactory factory = new ConnectionFactory();
    Connection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        factory.setUsername("AC68fac19d933395056cbd9647fd5529d2");
        factory.setPassword("aa301a03be0961409aaf5355fde7d5f205e0668a1c4c0ec848209ed241c767c2");
//        factory.setVirtualHost("/");
        factory.setVirtualHost("AC68fac19d933395056cbd9647fd5529d2");
        factory.setHost("staging.smswithoutborders.com");
        factory.setPort(5672);
        factory.setConnectionTimeout(10000);
        factory.setExceptionHandler(new DefaultExceptionHandler());
   }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(this.getClass().getName(), "Starting rmq connection...");
                try {
                    connection = factory.newConnection("deku-test");
                } catch (IOException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        // return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "Ending connection...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }
}