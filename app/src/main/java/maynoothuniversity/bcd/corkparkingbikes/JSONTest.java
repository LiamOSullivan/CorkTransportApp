package maynoothuniversity.bcd.corkparkingbikes;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONTest extends AppCompatActivity {

    Button click;
    public TextView data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsontest);

        click = (Button) findViewById(R.id.button);
        data = (TextView) findViewById(R.id.fetched_data);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendPostRequest().execute();
            }
        });
    }

    private class SendPostRequest extends AsyncTask<String, Void, String> {
        String parsed = "";
        String total = "";
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://data.bikeshare.ie/dataapi/resources/station/data/list");

                JSONObject postDataParams = new JSONObject();
                try {
                    postDataParams.put("key", "a5e70f27ae91405f9c21d023f4fb72400f24888687e26d6e75dc47b208c4aa97");
                    postDataParams.put("schemeId", "2");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(getPostDataString(postDataParams));

                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line = "";
                    while((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                        break;
                    }

                    JSONObject object = new JSONObject(stringBuffer.toString());
//                    JSONArray jsonArray  = object.getJSONArray("data");
//                    for(int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        parsed = jsonObject.getString("name") + "\n" + "Bikes available: " +
//                                 jsonObject.getInt("bikesAvailable") + "\n" + "Stands available: " +
//                                 jsonObject.getInt("docksAvailable") + "\n";
//                        total = total + parsed + "\n";
//                    }
                    bufferedReader.close();
                    return total;
                    //return stringBuffer.toString();
                }
                else {
                    return "false:" + responseCode;
                }
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            data.setText(result);
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> iterator = params.keys();

        while(iterator.hasNext()) {
            String key = iterator.next();
            Object value = params.get(key);

            if(first) {
                first = false;
            }
            else {
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
