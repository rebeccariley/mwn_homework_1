package rebeccariley.mwnhomework1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class funky_time extends AppCompatActivity {

    boolean term = false;
    String time_zone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funky_time);
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

    public void connect(View blank) {
        Button connect = (Button) findViewById(R.id.connect_button);
        if (connect.getText().toString().equals("CONNECT")) {
            EditText time_zone = (EditText) findViewById(R.id.time_zone_field);
            time_zone.setVisibility(View.VISIBLE);
            Button submit = (Button) findViewById(R.id.submit_button);
            submit.setVisibility(View.VISIBLE);
        }
        else {
            term = true;
            TextView clock = (TextView) findViewById(R.id.funky_time_text);
            clock.setText("");
            connect.setText("CONNECT");
            connect.setBackgroundColor(0xa435e07e);
        }
    }

    public void submit_button(View blank) {
        EditText time_zone_text = (EditText) findViewById(R.id.time_zone_field);
        time_zone = time_zone_text.getText().toString();
        time_zone_text.setVisibility(View.INVISIBLE);
        Button submit = (Button) findViewById(R.id.submit_button);
        submit.setVisibility(View.INVISIBLE);
        new Thread(new ServerThread()).start();
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            // Connect to server
            InputStream in = null;
            OutputStream out = null;
            try {
                Socket socket = new Socket("funkytime.hopto.org", 1313);
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }
            catch (UnknownHostException e) {
                runOnUiThread(new UnknownHostExceptMessage());
            }
            catch (IOException e) {
                runOnUiThread(new IOExceptMessage());
            }

            // change connect button to disconnect
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button connect = (Button) findViewById(R.id.connect_button);
                    connect.setText("DISCONNECT");
                    connect.setBackgroundColor(0xA4B32207);
                }
            });

            final StringBuilder sb = new StringBuilder();

            // display time on screen
            try {
                // "enter time zone" message
                while ( in.read() != '\n') {}

                out.write(time_zone.toUpperCase().getBytes());
                out.write('\n');

                // display loop
                // while loop escapes when term flag set to false by disconnect button
                int i, sentinel = 0;
                while ( (i = in.read()) != -1 && !term) {
                    sb.append((char)i);
                    if ((char)i == '\n' && (char)sentinel == '\n') {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView fnky_tm = (TextView) findViewById(R.id.funky_time_text);
                                fnky_tm.setText(sb.toString());
                            }
                        });
                        sb.delete(0, sb.length());
                    }
                    sentinel = i;
                }
            }
            catch (IOException e) {
                runOnUiThread(new IOExceptMessage());
            }

            term = false;
        }
    }

    class UnknownHostExceptMessage implements Runnable {
        @Override
        public void run() {
            TextView err_message = (TextView) findViewById(R.id.error_message);
            err_message.setText("UnknownHostException");
        }
    }

    class IOExceptMessage implements Runnable {
        @Override
        public void run() {
            TextView err_message = (TextView) findViewById(R.id.error_message);
            err_message.setText("IOException");
        }
    }
}
