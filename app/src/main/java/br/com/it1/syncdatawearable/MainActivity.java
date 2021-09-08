package br.com.it1.syncdatawearable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    private TextView textView;
    Button talkButton;
    EditText editText;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView =  findViewById(R.id.text);
        talkButton =  findViewById(R.id.talkClick);
        editText = findViewById(R.id.mensagem);

        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                textView.setText("Enviou mensagem n." + sentMessageNumber++ + ": " + message);
                new SendMessage(Commom.MESSAGE_PATH, message).start();
            }
        });

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

    }
    public class Receiver extends BroadcastReceiver {
        @Override

        public void onReceive(Context context, Intent intent) {

            textView.setText("Recebido: " + intent.getExtras().get("message").toString());
        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

        SendMessage(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        Integer result = Tasks.await(sendMessageTask);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}