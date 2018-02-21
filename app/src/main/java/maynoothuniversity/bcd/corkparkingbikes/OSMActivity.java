package maynoothuniversity.bcd.corkparkingbikes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
//import android.support.design.widget.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

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

import java.util.Iterator;
import java.util.Locale;

public class OSMActivity extends AppCompatActivity {

    // holds 'bikesAvailable' & 'docksAvailable' data
    private static int dataArray[] = new int[62];

    public  static String[] splitFreeSpaces;
    public  static String freeSpaces = "";
    private static String saint_finbarr;
    private static String merchant_quay;
    private static String grand_parade;
    private static String carroll_quay;
    private static String city_hall;
    private static String black_ash;
    private static String north_main;
    private static String paul_street;

    public static MapView map;

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton floatingActionButton1, floatingActionButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        // required to not get banned from osm servers
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_osm);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        
        floatingActionMenu = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionMenu.setIconAnimated(false);

        map = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.OpenTopo);
        map.setBuiltInZoomControls(false); // Zoom buttons
        map.setMultiTouchControls(true); // Pinch control

        map.getController().setZoom(15);
        map.setMinZoomLevel(11);
        map.getController().setCenter(new GeoPoint(51.8982899, -8.4765593));

        // Get the park & bike data
        new GetParkingData().execute("http://data.corkcity.ie/datastore/dump/6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        new GetBikeData().execute(); // <- uses clustering

        // Listeners for FABs
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleParkingLayer();
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleBikeLayer();
            }
        });
    }

    // Toggle layers (disabled)
    private void toggleParkingLayer() {
        Toast.makeText(this, "Tapped parking", Toast.LENGTH_SHORT).show();
//        Overlay toggle = map.getOverlays().get()
//        if(toggle.isEnabled()) {
//            toggle.setEnabled(false);
//        }
//        else {
//            toggle.setEnabled(true);
//        }
    }
    private void toggleBikeLayer() {
        Toast.makeText(this, "Tapped bike", Toast.LENGTH_SHORT).show();
//        Overlay toggle = map.getOverlays().get()
//        if(toggle.isEnabled()) {
//            toggle.setEnabled(false);
//        }
//        else {
//            toggle.setEnabled(true);
//        }
    }

    // Create markers
    private void setupPark() {
        FolderOverlay parkOverlay = new FolderOverlay();

        Drawable parkMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.parking, null);

        //<editor-fold desc="Car Park Markers">
        Marker finbarr = new Marker(map);
        finbarr.setTitle("Saint Finbarr's Car Park");
        finbarr.setIcon(parkMarker);
        finbarr.setPosition(new GeoPoint(51.896723, -8.482056));
        finbarr.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 350", saint_finbarr));
        parkOverlay.add(finbarr);

        Marker merchant = new Marker(map);
        merchant.setTitle("Merchants Quay Car Park");
        merchant.setIcon(parkMarker);
        merchant.setPosition(new GeoPoint(51.8995765, -8.4686481));
        merchant.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 710", merchant_quay));
        parkOverlay.add(merchant);

        Marker grand = new Marker(map);
        grand.setTitle("Grand Parade Car Park");
        grand.setIcon(parkMarker);
        grand.setPosition(new GeoPoint(51.896562, -8.474557));
        grand.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 352", grand_parade));
        parkOverlay.add(grand);

        Marker carroll = new Marker(map);
        carroll.setTitle("Carrolls Quay Car Park");
        carroll.setIcon(parkMarker);
        carroll.setPosition(new GeoPoint(51.901788, -8.472013));
        carroll.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 376", carroll_quay));
        parkOverlay.add(carroll);

        Marker city = new Marker(map);
        city.setTitle("City Hall - Eglington Street Car Park");
        city.setIcon(parkMarker);
        city.setPosition(new GeoPoint(51.896579, -8.464302));
        city.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 436", city_hall));
        parkOverlay.add(city);

        Marker blackash = new Marker(map);
        blackash.setTitle("Black Ash Park & Ride");
        blackash.setIcon(parkMarker);
        blackash.setPosition(new GeoPoint(51.878279, -8.466956));
        blackash.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 935", black_ash));
        parkOverlay.add(blackash);

        Marker north = new Marker(map);
        north.setTitle("North Main St. Car Park");
        north.setIcon(parkMarker);
        north.setPosition(new GeoPoint(51.901008, -8.477804));
        north.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 330", north_main));
        parkOverlay.add(north);

        Marker paul = new Marker(map);
        paul.setTitle("Paul St. Car Park");
        paul.setIcon(parkMarker);
        paul.setPosition(new GeoPoint(51.900542, -8.475415));
        paul.setSnippet(String.format(Locale.ENGLISH,"Currently has %s spaces out of 749", paul_street));
        parkOverlay.add(paul);
        //</editor-fold>

        map.getOverlays().add(parkOverlay);
        map.invalidate();
    }
    private void setupBike() {
        RadiusMarkerClusterer bikeCluster = new RadiusMarkerClusterer(this);
        Drawable bikeDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.blue_small, null);
        if ((bikeDrawable) != null) {
            Bitmap bikeClusterIcon = ((BitmapDrawable) bikeDrawable).getBitmap();

            bikeCluster.setIcon(bikeClusterIcon);
            bikeCluster.setRadius(200);
            bikeCluster.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);

            map.getOverlays().add(bikeCluster);

            Drawable bikeMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.cycling, null);

            //<editor-fold desc="Bike Markers">
            Marker gaol_walk = new Marker(map);
            gaol_walk.setTitle("Goal Walk");
            gaol_walk.setIcon(bikeMarker);
            gaol_walk.setPosition(new GeoPoint(51.893604, -8.494174));
            gaol_walk.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[0], dataArray[1]));
            bikeCluster.add(gaol_walk);

            Marker fitz_park = new Marker(map);
            fitz_park.setTitle("Fitzgerald's Park");
            fitz_park.setIcon(bikeMarker);
            fitz_park.setPosition(new GeoPoint(51.89555327, -8.49341266));
            fitz_park.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[2], dataArray[3]));
            bikeCluster.add(fitz_park);

            Marker bandfield = new Marker(map);
            bandfield.setTitle("Bandfield");
            bandfield.setIcon(bikeMarker);
            bandfield.setPosition(new GeoPoint(51.89580557, -8.4891363));
            bandfield.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[4], dataArray[5]));
            bikeCluster.add(bandfield);

            Marker dyke_parade = new Marker(map);
            dyke_parade.setTitle("Dyke Parade");
            dyke_parade.setIcon(bikeMarker);
            dyke_parade.setPosition(new GeoPoint(51.89718531, -8.48458467));
            dyke_parade.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[6], dataArray[7]));
            bikeCluster.add(dyke_parade);

            Marker mercy_hos = new Marker(map);
            mercy_hos.setTitle("Mercy Hospital");
            mercy_hos.setIcon(bikeMarker);
            mercy_hos.setPosition(new GeoPoint(51.89911495, -8.48225676));
            mercy_hos.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[8], dataArray[9]));
            bikeCluster.add(mercy_hos);

            Marker fin_bridge = new Marker(map);
            fin_bridge.setTitle("St. Fin Barre's Bridge");
            fin_bridge.setIcon(bikeMarker);
            fin_bridge.setPosition(new GeoPoint(51.89710212, -8.48196155));
            fin_bridge.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[10], dataArray[11]));
            bikeCluster.add(fin_bridge);

            Marker pope_quay = new Marker(map);
            pope_quay.setTitle("Pope's Quay");
            pope_quay.setIcon(bikeMarker);
            pope_quay.setPosition(new GeoPoint(51.901632, -8.477385));
            pope_quay.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[12], dataArray[13]));
            bikeCluster.add(pope_quay);

            Marker north_st = new Marker(map);
            north_st.setTitle("North Main St.");
            north_st.setIcon(bikeMarker);
            north_st.setPosition(new GeoPoint(51.89974733, -8.47844005));
            north_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[14], dataArray[15]));
            bikeCluster.add(north_st);

            Marker grattan_st = new Marker(map);
            grattan_st.setTitle("Grattan St.");
            grattan_st.setIcon(bikeMarker);
            grattan_st.setPosition(new GeoPoint(51.8984737, -8.47977966));
            grattan_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[16], dataArray[17]));
            bikeCluster.add(grattan_st);

            Marker wandesford_quay = new Marker(map);
            wandesford_quay.setTitle("Wandesford Quay");
            wandesford_quay.setIcon(bikeMarker);
            wandesford_quay.setPosition(new GeoPoint(51.896492, -8.48004));
            wandesford_quay.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[18], dataArray[19]));
            bikeCluster.add(wandesford_quay);

            Marker bishop_st = new Marker(map);
            bishop_st.setTitle("Bishop St.");
            bishop_st.setIcon(bikeMarker);
            bishop_st.setPosition(new GeoPoint(51.89468826, -8.4790268));
            bishop_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[20], dataArray[21]));
            bikeCluster.add(bishop_st);

            Marker camden_quay = new Marker(map);
            camden_quay.setTitle("Camden Quay");
            camden_quay.setIcon(bikeMarker);
            camden_quay.setPosition(new GeoPoint(51.901054, -8.473342));
            camden_quay.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[22], dataArray[23]));
            bikeCluster.add(camden_quay);

            Marker corn_market = new Marker(map);
            corn_market.setTitle("Corn Market");
            corn_market.setIcon(bikeMarker);
            corn_market.setPosition(new GeoPoint(51.9, -8.477));
            corn_market.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[24], dataArray[25]));
            bikeCluster.add(corn_market);

            Marker lapp_quay = new Marker(map);
            lapp_quay.setTitle("Lapp's Quay");
            lapp_quay.setIcon(bikeMarker);
            lapp_quay.setPosition(new GeoPoint(51.898144, -8.465735));
            lapp_quay.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[26], dataArray[27]));
            bikeCluster.add(lapp_quay);

            Marker patrick_st = new Marker(map);
            patrick_st.setTitle("St. Patrick's St.");
            patrick_st.setIcon(bikeMarker);
            patrick_st.setPosition(new GeoPoint(51.89850471, -8.47261531));
            patrick_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[28], dataArray[29]));
            bikeCluster.add(patrick_st);

            Marker south_st = new Marker(map);
            south_st.setTitle("South Main St.");
            south_st.setIcon(bikeMarker);
            south_st.setPosition(new GeoPoint(51.89694605306899, -8.47689553164735));
            south_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[30], dataArray[31]));
            bikeCluster.add(south_st);

            Marker grand_parade = new Marker(map);
            grand_parade.setTitle("Grand Parade");
            grand_parade.setIcon(bikeMarker);
            grand_parade.setPosition(new GeoPoint(51.89748023175811, -8.47536977381303));
            grand_parade.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[32], dataArray[33]));
            bikeCluster.add(grand_parade);

            Marker peace_park = new Marker(map);
            peace_park.setTitle("Peace Park");
            peace_park.setIcon(bikeMarker);
            peace_park.setPosition(new GeoPoint(51.89619469926869, -8.47347588279142));
            peace_park.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[34], dataArray[35]));
            bikeCluster.add(peace_park);

            Marker south_gate = new Marker(map);
            south_gate.setTitle("South Gate Bridge");
            south_gate.setIcon(bikeMarker);
            south_gate.setPosition(new GeoPoint(51.89549439125871, -8.47586514429047));
            south_gate.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[36], dataArray[37]));
            bikeCluster.add(south_gate);

            Marker coburg_st = new Marker(map);
            coburg_st.setTitle("Coburg St.");
            coburg_st.setIcon(bikeMarker);
            coburg_st.setPosition(new GeoPoint(51.90155283, -8.47056736));
            coburg_st.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[38], dataArray[39]));
            bikeCluster.add(coburg_st);

            Marker emmet_place = new Marker(map);
            emmet_place.setTitle("Emmet Place");
            emmet_place.setIcon(bikeMarker);
            emmet_place.setPosition(new GeoPoint(51.90020812214529, -8.47270466388061));
            emmet_place.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[40], dataArray[41]));
            bikeCluster.add(emmet_place);

            Marker south_mall = new Marker(map);
            south_mall.setTitle("South Mall");
            south_mall.setIcon(bikeMarker);
            south_mall.setPosition(new GeoPoint(51.89683855516081, -8.46989982762231));
            south_mall.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[42], dataArray[43]));
            bikeCluster.add(south_mall);

            Marker commerce = new Marker(map);
            commerce.setTitle("College of Commerce");
            commerce.setIcon(bikeMarker);
            commerce.setPosition(new GeoPoint(51.8953, -8.469797));
            commerce.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[44], dataArray[45]));
            bikeCluster.add(commerce);

            Marker mat_statue = new Marker(map);
            mat_statue.setTitle("Father Mathew Statue");
            mat_statue.setIcon(bikeMarker);
            mat_statue.setPosition(new GeoPoint(51.89967344, -8.4706278));
            mat_statue.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[46], dataArray[47]));
            bikeCluster.add(mat_statue);

            Marker cork_music = new Marker(map);
            cork_music.setTitle("Goal Walk");
            cork_music.setIcon(bikeMarker);
            cork_music.setPosition(new GeoPoint(51.89631707505499, -8.46809252166047));
            cork_music.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[48], dataArray[49]));
            bikeCluster.add(cork_music);

            Marker brian_boru = new Marker(map);
            brian_boru.setTitle("Brian Boru Bridge");
            brian_boru.setIcon(bikeMarker);
            brian_boru.setPosition(new GeoPoint(51.900405, -8.465153));
            brian_boru.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[50], dataArray[51]));
            bikeCluster.add(brian_boru);

            Marker bus_station = new Marker(map);
            bus_station.setTitle("Bus Station");
            bus_station.setIcon(bikeMarker);
            bus_station.setPosition(new GeoPoint(51.89951532, -8.46695074));
            bus_station.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[52], dataArray[53]));
            bikeCluster.add(bus_station);

            Marker cork_hall = new Marker(map);
            cork_hall.setTitle("Cork City Hall");
            cork_hall.setIcon(bikeMarker);
            cork_hall.setPosition(new GeoPoint(51.897, -8.466));
            cork_hall.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[54], dataArray[55]));
            bikeCluster.add(cork_hall);

            Marker glanmire = new Marker(map);
            glanmire.setTitle("Lower Glanmire Rd.");
            glanmire.setIcon(bikeMarker);
            glanmire.setPosition(new GeoPoint(51.90137057, -8.46411816));
            glanmire.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[56], dataArray[57]));
            bikeCluster.add(glanmire);

            Marker clontarf = new Marker(map);
            clontarf.setTitle("Clontarf Street");
            clontarf.setIcon(bikeMarker);
            clontarf.setPosition(new GeoPoint(51.89848186918711, -8.46562933177544));
            clontarf.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[58], dataArray[59]));
            bikeCluster.add(clontarf);

            Marker kent_station = new Marker(map);
            kent_station.setTitle("Kent Station");
            kent_station.setIcon(bikeMarker);
            kent_station.setPosition(new GeoPoint(51.90196195, -8.45821512));
            kent_station.setSnippet(String.format(Locale.ENGLISH,"Currently has %d bikes and %d stands available", dataArray[60], dataArray[61]));
            bikeCluster.add(kent_station);
            //</editor-fold>

        }
        map.invalidate();
    }

    // Thread tasks
    private class GetParkingData extends AsyncTask<String, String, String> {
        String dataParsed;
        String[] data_csv;
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                //StringBuffer buffer = new StringBuffer();
                String line = "";
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    data_csv = line.split(",");
                    try {
                        if(i > 0) {
                            Log.d("Data ", ""+data_csv[4]);
                            dataParsed = dataParsed + data_csv[4] + ",";
                            i++;
                        } else {
                            i++;
                        }
                    } catch (Exception e) {
                        Log.d("Problem: ", e.toString());
                    }
                }
                return dataParsed;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) { connection.disconnect(); }
                try {
                    if (reader != null) { reader.close(); }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            freeSpaces = result;
            splitFreeSpaces = freeSpaces.split(",");
            for (String splitFreeSpace : splitFreeSpaces) {
                Log.d("Split array output ", "" + splitFreeSpace);
            }
            saint_finbarr = splitFreeSpaces[0].substring(4); // remove the null appended to the first no.
            merchant_quay = splitFreeSpaces[1];
            grand_parade = splitFreeSpaces[2];
            carroll_quay = splitFreeSpaces[3];
            city_hall = splitFreeSpaces[4];
            black_ash = splitFreeSpaces[5];
            north_main = splitFreeSpaces[6];
            paul_street = splitFreeSpaces[7];

            // now make the parking markers
            setupPark();
        }
    }
    private class GetBikeData extends AsyncTask<String, Void, String> {
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
//                    JSONObject object = new JSONObject(stringBuffer.toString());
//                    JSONArray jsonArray  = object.getJSONArray("data");
//                    for(int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        parsed = jsonObject.getString("name") + "\n" + "Bikes available: " +
//                                 jsonObject.getInt("bikesAvailable") + "\n" + "Stands available: " +
//                                 jsonObject.getInt("docksAvailable") + "\n";
//                        total = total + parsed + "\n";
//                    }
//                    return total;
                    bufferedReader.close();
                    return stringBuffer.toString();
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

            JSONObject object = null;
            try {
                object = new JSONObject(result);
                JSONArray jsonArray  = object.getJSONArray("data");
                int k = 0;
                for(int i = 0; i < jsonArray.length(); i++, k+=2) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    dataArray[k] = jsonObject.getInt("bikesAvailable");
                    dataArray[k+1] = jsonObject.getInt("docksAvailable");
                }
                for (int j = 0; j < dataArray.length; j++) {
                    Log.d("Data Array", j+") " + dataArray[j]);
                }
                // now make the bike markers
                setupBike();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @NonNull
    public static String getPostDataString(JSONObject params) throws Exception {
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

    // Toolbar Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.attribution_id:
                AlertDialog.Builder builder = new AlertDialog.Builder(OSMActivity.this);
                View view = getLayoutInflater().inflate(R.layout.attribution_layout, null);
                builder.setView(view);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.mapbox_id:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }
}
