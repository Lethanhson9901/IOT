package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private static String URI = "tcp://broker.hivemq.com:1883";
    private static String USER_NAME = "sonlt173346";
    private static String PASSWORD = "sonlt173346";
    private static String TOPIC_PUB = "ESP8266/LED/status";
    private static String TEMP_TOPIC_SUB = "ESP8266/LED/status";
    private static String HUMID_TOPIC_SUB = "ESP8266/LED/status";
    private static String TOPIC_SUB = "ESP8266/LED/status";
    private static String TAG = "tag";
    private MqttAndroidClient client;


    private ImageView imageViewLamp1;
    private Button btnOnLamp1;
    private Button btnOffLamp1;

    protected static final int RESULT_SPEECH = 1;
    private ImageButton btnSpeak;
    private TextView tvText;
    private TextToSpeech mTTS;
    public int state=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = findViewById(R.id.tvTest);
        btnSpeak = findViewById(R.id.btnSpeak);

        connectToMqtt();
        intit();

        btnOffLamp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewLamp1.setBackgroundColor(Color.GRAY);
                String payload = "OFF";
                publish(TOPIC_PUB, false, payload);
                state=0;
            }
        });
        btnOnLamp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewLamp1.setBackgroundColor(Color.GREEN);
                String payload = "ON";
                publish(TOPIC_PUB, false, payload);
                state=1;
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                try{
                    startActivityForResult(intent, RESULT_SPEECH);
                    tvText.setText("");
                }
                catch (ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if (resultCode == RESULT_OK && data != null){
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tvText.setText(text.get(0));
                    if (text.contains("turn on the light")){
                        if (state==1){
                            String resText = "Opps, light is already on!";
                            return;
                        }
                        String resText = "OK, light on!";
                        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status == TextToSpeech.SUCCESS){
                                    int result = mTTS.setLanguage(Locale.ENGLISH);
                                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                                        Log.e("TTS", "Language not found");
                                    }
                                    imageViewLamp1.setBackgroundColor(Color.GREEN);
                                    String payload = "ON";
                                    publish(TOPIC_PUB, false, payload);
                                    mTTS.speak(resText, TextToSpeech.QUEUE_FLUSH, null);

                                }
                                else {
                                    Log.e("TTS", "Initialization failed");
                                }

                            }

                        });
                        state=1;
                    }

                    if (text.contains("turn off the light")){
                        String resText = "OK, light off!";
                        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status == TextToSpeech.SUCCESS){
                                    int result = mTTS.setLanguage(Locale.ENGLISH);
                                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                                        Log.e("TTS", "Language not found");
                                    }
                                    imageViewLamp1.setBackgroundColor(Color.GRAY);
                                    String payload = "OFF";
                                    publish(TOPIC_PUB, false, payload);
                                    mTTS.speak(resText, TextToSpeech.QUEUE_FLUSH, null);
                                }
                                else {
                                    Log.e("TTS", "Initialization failed");
                                }

                            }

                        });

                    }
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        if (mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();

    }

    private void intit() {

        imageViewLamp1 = findViewById(R.id.ImgLamp1);
        btnOffLamp1 = findViewById(R.id.btnLamp1OFF);
        btnOnLamp1 = findViewById(R.id.btnLamp1ON);
    }


    private void connectToMqtt() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), URI, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USER_NAME);
        options.setPassword(PASSWORD.toCharArray());
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    subscribesToMqtt(TOPIC_SUB, 0);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribesToMqtt(String topicSub, int qos) {
        try {
            client.subscribe(topicSub, qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, topicSub + ":  " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish(String topicPub, boolean retain, String payload) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(retain);
            client.publish(topicPub, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

}  // end class