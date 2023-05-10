package com.mssv.testiot;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final OkHttpClient client = new OkHttpClient();

    MQTTHelper mqttHelper;
    TextView txtTemperature, txtHumidity;
    LabeledSwitch btnLed ;
    LabeledSwitch btnFan ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtHumidity = findViewById(R.id.txtHumidity);
        btnLed = findViewById(R.id.btnLED);
        btnFan = findViewById(R.id.btnFAN);

        try {
            run("https://io.adafruit.com/api/v2/nguyentrongtri/groups?x-aio-key=aio_GPRg164RbJg4jjrlOJ81dC7VAi8D");
            Log.d("Successsss","api sucesss");
        } catch (IOException e) {
            Log.d("Faillll", e.toString());
            e.printStackTrace();
        }

        btnFan.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn){
                    sendDataMQTT("nguyentrongtri/feeds/nutnhan1", "1");
                }else{
                    sendDataMQTT("nguyentrongtri/feeds/nutnhan1", "0");
                }
            }
        });

        btnLed.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn){
                    sendDataMQTT("nguyentrongtri/feeds/nutnhan2", "1");
                }else{
                    sendDataMQTT("nguyentrongtri/feeds/nutnhan2", "0");
                }
            }
        });

        startMQTT();
    }

    public void run(String url) throws IOException {
        Log.d("test", "run api");
        String result = "" ;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {



                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseData = response.body().string();
                            JSONArray json = new JSONArray(responseData);
                            JSONObject obj = json.getJSONObject(0);

                            JSONArray feedData = obj.getJSONArray("feeds");

                            for (int i = 0 ; i < feedData.length(); i++) {
                                JSONObject feedItem = feedData.getJSONObject(i);
                                String name = feedItem.getString("name");
                                String data = feedItem.getString("last_value");
                                if(name.equals("cambien1")){
                                    txtTemperature.setText(data + "°C");
                                }else if(name.equals("cambien2")){
                                    txtHumidity.setText(data + "%");
                                }else if(name.equals("nutnhan1")){
                                    if(data.equals("0")){
                                        btnFan.setOn(false);
                                    }else{
                                        btnFan.setOn(true);
                                    }
                                }else if(name.equals("nutnhan2")){
                                    if(data.equals("0")){
                                        btnLed.setOn(false);
                                    }else{
                                        btnLed.setOn(true);
                                    }
                                }

                            }

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }


    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
        }
    }
    public void startMQTT(){
        Log.d("Start", "start mqtt");
        mqttHelper = new MQTTHelper(this,"aaaaa");
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if(topic.contains("cambien1")){
                    txtTemperature.setText(message.toString() + "°C");
                }else if(topic.contains("cambien2")){
                    txtHumidity.setText(message.toString() + "%");
                }else if(topic.contains("nutnhan1")){
                    if(message.toString().equals("1")){
                        btnFan.setOn(true);
                    }else{
                        btnFan.setOn(false);
                    }
                }else if(topic.contains("nutnhan2")){
                    if(message.toString().equals("1")){
                        btnLed.setOn(true);
                    }else{
                        btnLed.setOn(false);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}