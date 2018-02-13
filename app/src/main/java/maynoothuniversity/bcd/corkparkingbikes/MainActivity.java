/*
 * List of helpful links
 * https://www.mapbox.com/install/android/complete/
 * https://www.numetriclabz.com/android-mapbox-sdk-tutorial-to-implement-a-map/
 * https://www.mapbox.com/help/android-dds-circle-layer/
*/
package maynoothuniversity.bcd.corkparkingbikes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
//import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static java.security.AccessController.getContext;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    // Define the boundary for the camera (arbitrary coordinates - can be changed)
    private static final LatLngBounds CORK_CITY = new LatLngBounds.Builder()
            .include(new LatLng(51.917283, -8.557001))
            .include(new LatLng(51.851449, -8.366767))
            .build();

    private MapView mapView;
    private ToggleButton toggleButton;
    private MapboxMap mapboxMap;

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton floatingActionButton1, floatingActionButton2;

    public static String freeSpaces = "";
    public static String[] splitFreeSpaces;
    private String saint_finbarr = "";
    private String merchant_quay = "";
    private String grand_parade = "";
    private String carroll_quay = "";
    private String city_hall = "";
    private String black_ash = "";
    private String north_main = "";
    private String paul_street = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token_mapbox));
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);

//        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
//         Toggle button to switch between LIGHT and DARK theme
//        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(b) { mapView.setStyleUrl(Style.DARK); }
//                else { mapView.setStyleUrl(Style.LIGHT); } // "mapbox://styles/dawnithan/cjct8vxj90uy52smt0fwfgqw6" <- custom light style
//            }
//        });

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);

        // parking data
        new HandleCSV().execute("http://data.corkcity.ie/datastore/dump/6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        // bike data
        // soon(tm)
        //new HandleJSON().execute("https://data.bikeshare.ie/dataapi/resources/station/data/list?key=a5e70f27ae91405f9c21d023f4fb72400f24888687e26d6e75dc47b208c4aa97&schemeId=2")
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        // Set the bounds
        mapboxMap.setLatLngBoundsForCameraTarget(CORK_CITY);

        // Add the markers for bikes and parking with clustering
        addClusteredGeoJsonSource();

        mapboxMap.addOnMapClickListener(this);
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

    @Override
    public void onMapClick(@NonNull LatLng point) {
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-park");
        List<Feature> bike_features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-bike");

        // For parking markers
        if (features.size() > 0) {
            Feature feature = features.get(0);

            Display screenOrientation = getWindowManager().getDefaultDisplay();
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            if(screenOrientation.getWidth() == screenOrientation.getHeight()){
                orientation = Configuration.ORIENTATION_SQUARE;
                CameraPosition position = new CameraPosition.Builder()
                        .target(point)
                        .build();
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
            }
            else {
                if(screenOrientation.getWidth() < screenOrientation.getHeight()){
                    orientation = Configuration.ORIENTATION_PORTRAIT;

                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                }
                else {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                    int half = (int)mapboxMap.getHeight()/2;
                    mapboxMap.setPadding(0, half,0,0);
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                }
            }

            // https://blog.mapbox.com/a-guide-to-the-android-symbollayer-api-5daac7b66f2c <- better than dialog but effort ;_;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_info, null);

            TextView txtName = (TextView) mView.findViewById(R.id.name);
            TextView txtFree = (TextView) mView.findViewById(R.id.freeSpaces);
            TextView txtPrice = (TextView) mView.findViewById(R.id.price);

            String park_name = feature.getProperty("name").toString();
            String price = feature.getProperty("price").toString();

            txtName.setText(park_name.replace('"',' '));
            txtPrice.setText(price.replace('"',' '));

            // Manually assign free_spaces values to relevant dialog box (because I'm bad)
            if(park_name.equals("\"Saint Finbarr's\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", saint_finbarr, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"Merchants Quay\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", merchant_quay, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"Grand Parade\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", grand_parade, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"Carrolls Quay\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", carroll_quay, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"City Hall - Eglington Street\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", city_hall, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"Black Ash Park & Ride\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", black_ash, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"North Main Street\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", north_main, feature.getProperty("spaces").toString())); }
            if(park_name.equals("\"Paul Street\"")) { txtFree.setText(String.format("Currently %s free spaces out of %s", paul_street, feature.getProperty("spaces").toString())); }

            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // close
                        }
                    });

            builder.setView(mView);
            AlertDialog dialog = builder.create();

            Window window = dialog.getWindow();
            window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim

            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);

            dialog.show();
        }

        // For bike markers
        else if (bike_features.size() > 0) {
            Feature feature = bike_features.get(0);

            // Centre camera above selected marker - depending on screen orientation
            Display screenOrientation = getWindowManager().getDefaultDisplay();
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            if(screenOrientation.getWidth() == screenOrientation.getHeight()){
                orientation = Configuration.ORIENTATION_SQUARE;
                CameraPosition position = new CameraPosition.Builder()
                        .target(point)
                        .build();
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
            }
            else {
                if(screenOrientation.getWidth() < screenOrientation.getHeight()){
                    orientation = Configuration.ORIENTATION_PORTRAIT;

                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                }
                else {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                    int half = (int)mapboxMap.getHeight()/2;
                    mapboxMap.setPadding(0, half,0,0);
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_info_bike, null);

            TextView txtName = (TextView) mView.findViewById(R.id.name);
            TextView txtBikes = (TextView) mView.findViewById(R.id.bikes);

            // these are old values from an old file
            String bike_station_name = feature.getProperty("data__name").toString();

            txtName.setText(bike_station_name.replace('"',' '));
            txtBikes.setText(String.format("Currently %s bikes and %s stands available.",
                    feature.getProperty("data__bikesAvailable").toString(),
                    feature.getProperty("data__docksAvailable").toString()
            ));

            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // close
                }
            });

            builder.setView(mView);
            AlertDialog dialog = builder.create();

            Window window = dialog.getWindow();
            window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim

            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);

            dialog.show();
        }
    }

//            if (feature.getProperties() != null) {
//                for (Map.Entry<String, JsonElement> entry : feature.getProperties().entrySet()) {
//                    // Log all the properties
//                    Log.d("Main Activity ", String.format("%s = %s", entry.getKey(), entry.getValue()));
//
//                }
//            }

    protected class HandleCSV extends AsyncTask<String, String, String> {
        String dataParsed = "";
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
                        }
                        else {
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
            //data.setText(result);
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            freeSpaces = result;
            splitFreeSpaces = freeSpaces.split(",");
            for(int i = 0; i < splitFreeSpaces.length; i++) {
                Log.d("Split array output ",""+splitFreeSpaces[i]);
            }
            saint_finbarr = splitFreeSpaces[0];
            merchant_quay = splitFreeSpaces[1];
            grand_parade = splitFreeSpaces[2];
            carroll_quay = splitFreeSpaces[3];
            city_hall = splitFreeSpaces[4];
            black_ash = splitFreeSpaces[5];
            north_main = splitFreeSpaces[6];
            paul_street = splitFreeSpaces[7];
        }
    }

    /*
     * Example Bike Json from 24-05-2017 -> https://api.myjson.com/bins/dlp89
     * Static Car Park Json -> https://api.myjson.com/bins/x52zl
    */
    private void addClusteredGeoJsonSource() {
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
                iconImage("car-15"),
                iconSize(1.5f),
                visibility(VISIBLE)
        );
        SymbolLayer unclusteredBike = new SymbolLayer("unclustered-points-bike", "cork-bike");
        unclusteredBike.withProperties(
                iconImage("bicycle-share-15"),
                iconSize(1.5f),
                visibility(VISIBLE)
        );
        mapboxMap.addLayer(unclusteredPark);
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