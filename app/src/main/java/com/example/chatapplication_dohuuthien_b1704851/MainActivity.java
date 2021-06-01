package com.example.chatapplication_dohuuthien_b1704851;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication_dohuuthien_b1704851.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private final String URL_SERVER = "http://10.13.146.33:3000";
    private Socket mSocket; // Chọn Socket (IO.socket.client)
    private ArrayList<String> users, chats;
    private ArrayAdapter<String> adapterUser, adapterChat;

    ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        try {
            mSocket = IO.socket(URL_SERVER);
        } catch (URISyntaxException e) {
            Log.e("TAG", "onCreate: " + e.getMessage());
        }

        users = new ArrayList<>();
        chats = new ArrayList<>();


        adapterChat = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chats);
        adapterUser = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        mainBinding.listUser.setAdapter(adapterUser);
        mainBinding.listChat.setAdapter(adapterChat);

        mainBinding.send.setOnClickListener(v -> {
            if (mainBinding.content.getText().length() > 0){
                mSocket.emit("client-send-chat", mainBinding.content.getText().toString());
                mainBinding.content.setText("");
            }

        });
        mainBinding.add.setOnClickListener(v -> {
            if (mainBinding.content.getText().length() > 0){
                mSocket.emit("client-register-user", mainBinding.content.getText().toString());
            }
        });
        mainBinding.layout.setOnClickListener(v -> hideKeyboard(MainActivity.this));
        mSocket.connect();
        mSocket.on("server-send-data", onRetrieveResult);
        mSocket.on("server-send-user", onListUser);
        mSocket.on("server-send-chat", onListChat);
    }

    private Emitter.Listener onRetrieveData = args -> runOnUiThread(() -> {
        JSONObject object = (JSONObject) args[0];
        try {
            String ten = object.getString("noidung");
            Toast.makeText(MainActivity.this, ten, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });
    private Emitter.Listener onListChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        String noiDung = object.getString("chatComent");
                        chats.add(noiDung);
                        adapterChat.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onListUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        JSONArray array = object.getJSONArray("danhsach");
                        adapterUser.clear();
                        for (int i = 0; i < array.length(); i++) {
                            String username = array.getString(i);
                            adapterUser.add(username);
                        }
                        adapterUser.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onRetrieveResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        //String ten = object.getString("noidung");
                        boolean exits = object.getBoolean("ketqua");
                        if(exits) {
                            Toast.makeText(MainActivity.this, "Tài khoản này đã tôn tại!", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Đã đăng ký thành công", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}