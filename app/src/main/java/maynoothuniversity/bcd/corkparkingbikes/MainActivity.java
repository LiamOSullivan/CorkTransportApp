package maynoothuniversity.bcd.corkparkingbikes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

//import android.support.design.widget.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    // Define the boundary for the camera (arbitrary coordinates - can be changed)
    private static final LatLngBounds CORK_CITY = new LatLngBounds.Builder()
            .include(new LatLng(51.917283, -8.557001))
            .include(new LatLng(51.851449, -8.366767))
            .build();

    private MapView mapView;
    private MapboxMap mapboxMap;
    private CheckBox dontShow;
    //private Button refreshButton;

    android.support.design.widget.FloatingActionButton floatingActionButton;

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

    // holds free_spaces for each car park
    public  static String[] splitFreeSpaces = new String [8];
    public  static String freeSpaces;
    private static String saint_finbarr;
    private static String merchant_quay;
    private static String grand_parade;
    private static String carroll_quay;
    private static String city_hall;
    private static String black_ash;
    private static String north_main;
    private static String paul_street;

    // holds 'bikesAvailable' & 'docksAvailable' data
    private static int dataArray[] = new int[62];

    // track connection status
    private static int connectionResultCarPark;
    private static int connectionResultBike;

    public String date;

    // Main create method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token_mapbox));
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        mapView = findViewById(R.id.mapView);

        // layer toggling FABs
        floatingActionMenu = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionMenu.setIconAnimated(false);
        floatingActionMenu.setMenuButtonColorNormal(Color.parseColor("#d84e52"));

        // info page FAB
        floatingActionButton = findViewById(R.id.info_fab);

        DateFormat df = new SimpleDateFormat("h:mma", Locale.ENGLISH);
        date = df.format(Calendar.getInstance().getTime());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.contains("FirstTime")) {
            AlertDialog.Builder firstTime = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.first_time_popup, null);
            dontShow = view.findViewById(R.id.dont_show_again);
            firstTime.setView(view);
            firstTime.setTitle("How To Use This Map");
            firstTime.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(dontShow.isChecked()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("FirstTime",true);
                        editor.apply();
                    }
                }
            });
            firstTime.setCancelable(false);
            firstTime.show();
        }

        // Retrieve data for the app in separate threads
        new GetCarParkData().execute("http://data.corkcity.ie/datastore/dump/6cc1028e-7388-4bc5-95b7-667a59aa76dc"); // parking data
        new GetBikeData().execute(); // bike data

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    // Sets up preferences, markers, and listeners
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        //TODO -> fix compass?
        mapboxMap.getUiSettings().setCompassEnabled(false);
        
        // Set the bounds
        mapboxMap.setLatLngBoundsForCameraTarget(CORK_CITY);
        mapboxMap.setMaxZoomPreference(18);
        mapboxMap.setMinZoomPreference(10);

        // Add the markers for bikes and parking with clustering
        addClusteredGeoJsonSource();

        mapboxMap.addOnMapClickListener(this);

        // Layers Toggling
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
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                //overridePendingTransition(0, 0);
                startActivity(intent);
                //overridePendingTransition(0, 0);
            }
        });

        // Info Activity
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }

    // Handles layer toggling
    private void toggleBikeLayer() {
        for(int i = 0; i <= 3; i++) {
            // toggle circles
            Layer layer = mapboxMap.getLayer("clusterBike-"+i);
            if(layer != null) {
                if (VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE));
                } else {
                    layer.setProperties(visibility(VISIBLE));
                }
            }
        }
        // toggle the individual markers
        Layer layer = mapboxMap.getLayer("unclustered-points-bike");
        if(layer != null) {
            if (VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
            } else {
                layer.setProperties(visibility(VISIBLE));
            }
        }
        // toggle the circle numbers
        Layer layer_nums = mapboxMap.getLayer("countBike");
        if(layer_nums != null) {
            if (VISIBLE.equals(layer_nums.getVisibility().getValue())) {
                layer_nums.setProperties(visibility(NONE));
            } else {
                layer_nums.setProperties(visibility(VISIBLE));
            }
        }
    }
    private void toggleParkingLayer() {
        // toggle the individual markers
        Layer layer = mapboxMap.getLayer("unclustered-points-park");
        if(layer != null) {
            if (VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
            } else {
                layer.setProperties(visibility(VISIBLE));
            }
        }
    }

    // Handles tapping the map and bringing up dialogs that display information
    @Override
    public void onMapClick(@NonNull LatLng point) {
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-park");
        List<Feature> bike_features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-bike");
        List<Feature> cluster = mapboxMap.queryRenderedFeatures(pixel, "clusterBike-" + 1, "clusterBike-" + 2);

        int half = (int)mapboxMap.getHeight()/2;
        int quarter = half/2;

        String orientation;
        Display screenOrientation = getWindowManager().getDefaultDisplay();
        double currentZoom = mapboxMap.getCameraPosition().zoom;
        // note: camera centers at tap position, not feature coordinates

        // Centre + zoom into tapped cluster
        if(cluster.size() > 0) {
            if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
                // Square
                orientation = "Square";
                CameraPosition position = new CameraPosition.Builder()
                        .target(point)
                        .zoom(currentZoom+2)
                        .build();
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
            } else {
                if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                    // Portrait
                    orientation = "Portrait";
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .zoom(currentZoom+2)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                } else {
                    // Landscape
                    orientation = "Landscape";
                    mapboxMap.setPadding(0, 0, 0, 0);
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .zoom(currentZoom+2)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                }
            }
        }

        // For parking markers --------------------------------------------------------------------
        if (features.size() > 0) {
            if (connectionResultCarPark == 200) {
                Feature feature = features.get(0);

                //<editor-fold desc="Camera Move on Marker Tap">
                if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
                    // Square
                    orientation = "Square";
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                } else {
                    if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                        // Portrait
                        orientation = "Portrait";
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    } else {
                        // Landscape
                        orientation = "Landscape";
                        mapboxMap.setPadding(0, half, 0, 0);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }
                //</editor-fold>

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_info, null);

                TextView txtName = mView.findViewById(R.id.name);
                TextView txtFree = mView.findViewById(R.id.freeSpaces);
                TextView txtPrice = mView.findViewById(R.id.price);
                TextView txtOpening = mView.findViewById(R.id.openingTimes);

                String park_name = feature.getProperty("name").toString();
                String price = feature.getProperty("price").toString();
                String opening_time = feature.getProperty("opening_times").toString();

                txtName.setText(park_name.replace('"', ' '));
                txtPrice.setText(price.replace('"', ' '));

                txtOpening.setText(R.string.open);
                txtOpening.append(opening_time.replace('"', ' '));

                double percentage;
                // Assign free_spaces values to relevant dialog box
                //<editor-fold desc="Parking Data Assignment">
                if (park_name.equals("\"Saint Finbarr's\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", saint_finbarr, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(saint_finbarr) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"Merchants Quay\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", merchant_quay, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(merchant_quay) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"Grand Parade\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", grand_parade, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(grand_parade) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"Carrolls Quay\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", carroll_quay, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(carroll_quay) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"City Hall - Eglington Street\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", city_hall, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(city_hall) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"Black Ash Park & Ride\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", black_ash, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(black_ash) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("\"North Main Street\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", north_main, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(north_main) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                    //                if(percentage > 50) {
                    //                    txtFree.setShadowLayer(1.5f, -1, 1, Color.BLACK);
                    //                }
                }
                if (park_name.equals("\"Paul Street\"")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", paul_street, feature.getProperty("spaces").toString())));
                    percentage = (Double.parseDouble(paul_street) / Double.parseDouble(feature.getProperty("spaces").toString())) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                //</editor-fold>
                txtFree.append("\n (" + date + ")");

                builder.setView(mView);
                AlertDialog dialog = builder.create();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim
                }

                WindowManager.LayoutParams wlp;
                if (window != null) {
                    if (orientation.equals("Square") || orientation.equals("Portrait")) {
                        wlp = window.getAttributes();
                        wlp.gravity = Gravity.TOP;
                        //wlp.y = quarter;  <- lowers the dialog by 1/4 of the screen height
                        window.setAttributes(wlp);
                    } else {
                        wlp = window.getAttributes();
                        wlp.gravity = Gravity.TOP;
                        window.setAttributes(wlp);
                    }
                }
                dialog.show();
            } else {
                Toast.makeText(getApplicationContext(), "Data could not be loaded. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }

        // For bike markers -----------------------------------------------------------------------
        else if (bike_features.size() > 0) {
            if(connectionResultBike == 200) {

                Feature feature = bike_features.get(0);

                //<editor-fold desc="Camera Move on Marker Tap">
                if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
                    // Square
                    orientation = "Square";
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                } else {
                    if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                        // Portrait
                        orientation = "Portrait";
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    } else {
                        // Landscape
                        orientation = "Landscape";
                        mapboxMap.setPadding(0, half, 0, 0);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }
                //</editor-fold>

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_info_bike, null);

                TextView txtName = mView.findViewById(R.id.name);
                TextView txtBikes = mView.findViewById(R.id.bikes);

                // note: these values come from an old file
                String bike_station_name = feature.getProperty("data__name").toString();

                txtName.setText(bike_station_name.replace('"', ' '));

                // Assign bikesAvailable and docksAvailable values to relevant dialog box
                //<editor-fold desc="Bike Data Assignment">
                if (bike_station_name.equals("\"Gaol Walk\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[0], dataArray[1])));
                }
                if (bike_station_name.equals("\"Fitzgerald's Park\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[2], dataArray[3])));
                }
                if (bike_station_name.equals("\"Bandfield\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[4], dataArray[5])));
                }
                if (bike_station_name.equals("\"Dyke Parade\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[6], dataArray[7])));
                }
                if (bike_station_name.equals("\"Mercy Hospital\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[8], dataArray[9])));
                }
                if (bike_station_name.equals("\"St. Fin Barre's Bridge\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[10], dataArray[11])));
                }
                if (bike_station_name.equals("\"Pope's Quay\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[12], dataArray[13])));
                }
                if (bike_station_name.equals("\"North Main St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[14], dataArray[15])));
                }
                if (bike_station_name.equals("\"Grattan St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[16], dataArray[17])));
                }
                if (bike_station_name.equals("\"Wandesford Quay\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[18], dataArray[19])));
                }
                if (bike_station_name.equals("\"Bishop St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[20], dataArray[21])));
                }
                if (bike_station_name.equals("\"Camden Quay\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[22], dataArray[23])));
                }
                if (bike_station_name.equals("\"Corn Market St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[24], dataArray[25])));
                }
                if (bike_station_name.equals("\"Lapp's Quay\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[26], dataArray[27])));
                }
                if (bike_station_name.equals("\"St. Patricks St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[28], dataArray[29])));
                }
                if (bike_station_name.equals("\"South Main St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[30], dataArray[31])));
                }
                if (bike_station_name.equals("\"Grand Parade\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[32], dataArray[33])));
                }
                if (bike_station_name.equals("\"Peace Park\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[34], dataArray[35])));
                }
                if (bike_station_name.equals("\"South Gate Bridge\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[36], dataArray[37])));
                }
                if (bike_station_name.equals("\"Coburg St.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[38], dataArray[39])));
                }
                if (bike_station_name.equals("\"Emmet Place\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[40], dataArray[41])));
                }
                if (bike_station_name.equals("\"South Mall\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[42], dataArray[43])));
                }
                if (bike_station_name.equals("\"College of Commerce\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[44], dataArray[45])));
                }
                if (bike_station_name.equals("\"Father Mathew Statue\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[46], dataArray[47])));
                }
                if (bike_station_name.equals("\"Cork School of Music\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[48], dataArray[49])));
                }
                if (bike_station_name.equals("\"Brian Boru Bridge\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[50], dataArray[51])));
                }
                if (bike_station_name.equals("\"Bus Station\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[52], dataArray[53])));
                }
                if (bike_station_name.equals("\"Cork City Hall\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[54], dataArray[55])));
                }
                if (bike_station_name.equals("\"Lower Glanmire Rd.\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[56], dataArray[57])));
                }
                if (bike_station_name.equals("\"Clontarf Street\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[58], dataArray[59])));
                }
                if (bike_station_name.equals("\"Kent Station\"")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[60], dataArray[61])));
                }
                //</editor-fold>
                txtBikes.append("\n (" + date + ")");

                builder.setView(mView);
                AlertDialog dialog = builder.create();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim
                }

                WindowManager.LayoutParams wlp;
                if (window != null) {
                    if (orientation.equals("Square") || orientation.equals("Portrait")) {
                        wlp = window.getAttributes();
                        wlp.gravity = Gravity.TOP;
                        //wlp.y = quarter;
                        window.setAttributes(wlp);
                    } else {
                        wlp = window.getAttributes();
                        wlp.gravity = Gravity.TOP;
                        window.setAttributes(wlp);
                    }
                }
                dialog.show();
            } else {
                Toast.makeText(getApplicationContext(), "Data could not be loaded. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Methods for getting map data
    private static class GetCarParkData extends AsyncTask<String, String, String> {
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

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    connectionResultCarPark = responseCode;
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    String line = "";
                    int i = 0;
                    while ((line = reader.readLine()) != null) {
                        data_csv = line.split(",");
                        try {
                            if (i > 0) {
                                Log.d("Data ", "" + data_csv[4]);
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
                } else {
                    connection.disconnect();
                    Log.d("Car Park data error: ", ""+responseCode);
                    return "";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //data.setText(result);
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            if(result.length() > 0) {
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
            }
        }
    }
    private static class GetBikeData extends AsyncTask<String, Void, String> {
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
                    connectionResultBike = responseCode;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line = "";
                    while((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                        break;
                    }
                    bufferedReader.close();
                    return stringBuffer.toString();
                }
                else {
                    connection.disconnect();
                    Log.d("Bike data error: ", ""+responseCode);
                    return "";
                }
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(result);
                    JSONArray jsonArray = object.getJSONArray("data");
                    int k = 0;
                    for (int i = 0; i < jsonArray.length(); i++, k += 2) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        dataArray[k] = jsonObject.getInt("bikesAvailable");
                        dataArray[k + 1] = jsonObject.getInt("docksAvailable");
                    }
                    for (int j = 0; j < dataArray.length; j++) {
                        Log.d("Data Array", j + ") " + dataArray[j]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Used by GetBikeData
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

    // Adds both types of markers to map and clusters them (except for Parking)
    private void addClusteredGeoJsonSource() {
     // Example Bike Json from 24-05-2017 -> https://api.myjson.com/bins/dlp89
     // Static Car Park Json -> https://api.myjson.com/bins/x52zl
        try {
            mapboxMap.addSource(
                    new GeoJsonSource("cork-parking",
                            new URL("https://api.myjson.com/bins/x52zl"),
                            new GeoJsonOptions()
                                    .withCluster(false)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                    )
            );
            mapboxMap.addSource(
                    new GeoJsonSource("cork-bike",
                            new URL("https://api.myjson.com/bins/dlp89"),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                    )
            );
        } catch (MalformedURLException malformedUrlException) {
            Log.e("dataClusterActivity", "Check the URL " + malformedUrlException.getMessage());
        }

        int[][] layers = new int[][]{
                new int[]{150, Color.parseColor("#dd1c77")},
                new int[]{20, Color.parseColor("#addd8e")},
                new int[]{0, Color.parseColor("#2b8cbe")}
        };

        SymbolLayer unclusteredPark = new SymbolLayer("unclustered-points-park", "cork-parking");
        unclusteredPark.withProperties(
                iconImage("parking-15-colour"),
                iconSize(1.5f),
                visibility(VISIBLE)
        );

//        CircleLayer parkIndicators = new CircleLayer("parkIndicators","cork-parking");
//        parkIndicators.withProperties(
//                circleRadius(20f),
//                circleColor("#feb24c")
//        );

        SymbolLayer unclusteredBike = new SymbolLayer("unclustered-points-bike", "cork-bike");
        unclusteredBike.withProperties(
                iconImage("bicycle-share-15-colour-alt"),
                iconSize(1.5f),
                visibility(VISIBLE)
        );
        mapboxMap.addLayer(unclusteredPark);
//        mapboxMap.addLayerBelow(parkIndicators,"unclustered-points-park");
        mapboxMap.addLayer(unclusteredBike);

        for (int i = 0; i < layers.length; i++) {
            //Add clusters' circles
            CircleLayer circlesPark = new CircleLayer("clusterPark-" + i, "cork-parking");
            circlesPark.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f)
            );
            CircleLayer circlesBike = new CircleLayer("clusterBike-" + i, "cork-bike");
            circlesBike.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f),
                    visibility(VISIBLE)
            );
            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circlesPark.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            circlesBike.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            mapboxMap.addLayer(circlesPark);
            mapboxMap.addLayer(circlesBike);
        }

        //Add the count labels
        SymbolLayer countPark = new SymbolLayer("countPark", "cork-parking");
        countPark.setProperties(
                textField("{point_count}"),
                textSize(12f),
                textColor(Color.WHITE)
        );
        SymbolLayer countBike = new SymbolLayer("countBike", "cork-bike");
        countBike.setProperties(
                textField("{point_count}"),
                textSize(12f),
                textColor(Color.WHITE)
        );
        mapboxMap.addLayer(countPark);
        mapboxMap.addLayer(countBike);
    }

    // Returns blue, yellow, or red based on given percentage
    public String getTextPercentageColour(double percentage) {
        if(percentage <= 20) {
            return "#f03b20"; // red
        }
        if(percentage > 20 && percentage <= 50) {
            return "#feb24c"; // orange

        } else {
            return "#43a2ca"; // blue
        }
    }

    // Lifecycle Methods required for MapBox
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // refresh data on resume?
        // this appears to happen if the app is minimized/closed anyways
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

