package com.d4ck.app.dondestoy;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class MapViewActivity extends ActionBarActivity {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final int MENU_ZOOMIN_ID = Menu.FIRST;
    private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
    private static final int MENU_TILE_SOURCE_ID = MENU_ZOOMOUT_ID + 1;
    private static final int MENU_ANIMATION_ID = MENU_TILE_SOURCE_ID + 1;
    private static final int MENU_MINIMAP_ID = MENU_ANIMATION_ID + 1;
    ArrayList<OverlayItem> overlayItemArray;
    // ===========================================================
    // Fields
    // ===========================================================
    private LocationManager myLocationManager;
    private MapView mapView;
    private MapController mapController;
    private ScaleBarOverlay mScaleBarOverlay;
    private SimpleLocationOverlay mMyLocationOverlay;
    private ResourceProxy resourceProxy;
    private MinimapOverlay miniMapOverlay;
    private LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            updateLoc(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);

        CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

        mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setUseDataConnection(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setAnimationCacheEnabled(true);
        mapView.setClickable(true);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);

        mapController = (MapController) this.mapView.getController();
        mapController.setZoom(20);

        //--- Create Overlay
        resourceProxy = new ResourceProxyImpl(getApplicationContext());

        mMyLocationOverlay = new SimpleLocationOverlay(this, resourceProxy);
        mapView.getOverlays().add(mMyLocationOverlay);

        MinimapOverlay miniMapOverlay = new MinimapOverlay(this, mapView.getTileRequestCompleteHandler());
        miniMapOverlay.setZoomDifference(5);
        miniMapOverlay.setHeight(200);
        miniMapOverlay.setWidth(200);
        mapView.getOverlays().add(miniMapOverlay);

        myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    private void updateLoc(Location loc) {
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        mapController.setCenter(locGeoPoint);
        setOverlayLoc(loc);
        mapView.invalidate();
    }

    public void onLocationLost() {
        // We'll do nothing here.
    }

    private void setOverlayLoc(Location overlayloc) {
        this.mMyLocationOverlay.setLocation(new GeoPoint(overlayloc));
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        myLocationManager.removeUpdates(myLocationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu) {
        pMenu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
        pMenu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");

        final SubMenu subMenu = pMenu.addSubMenu(0, MENU_TILE_SOURCE_ID, Menu.NONE,
                "Choose Tile Source");
        {
            for (final ITileSource tileSource : TileSourceFactory.getTileSources()) {
                subMenu.add(0, 1000 + tileSource.ordinal(), Menu.NONE,
                        tileSource.localizedName(resourceProxy));
            }
        }

        pMenu.add(0, MENU_ANIMATION_ID, Menu.NONE, "Run Animation");
        pMenu.add(0, MENU_MINIMAP_ID, Menu.NONE, "Toggle Minimap");

        return true;
    }

    public boolean onMenuItemSelected(final int featureId, MenuItemCompat item) {
        switch (((MenuItem) item).getItemId()) {
            case MENU_ZOOMIN_ID:
                this.mapController.zoomIn();
                return true;

            case MENU_ZOOMOUT_ID:
                this.mapController.zoomOut();
                return true;

            case MENU_TILE_SOURCE_ID:
                this.mapView.invalidate();
                return true;

            case MENU_MINIMAP_ID:
                miniMapOverlay.setEnabled(!miniMapOverlay.isEnabled());
                this.mapView.invalidate();
                return true;

            case MENU_ANIMATION_ID:
                // this.mOsmv.getController().animateTo(52370816, 9735936,
                // MapControllerOld.AnimationType.MIDDLEPEAKSPEED,
                // MapControllerOld.ANIMATION_SMOOTHNESS_HIGH,
                // MapControllerOld.ANIMATION_DURATION_DEFAULT); // Hannover
                // Stop the Animation after 500ms (just to show that it works)
                // new Handler().postDelayed(new Runnable(){
                // @Override
                // public void run() {
                // SampleExtensive.this.mOsmv.getController().stopAnimation(false);
                // }
                // }, 500);
                return true;

            default:
                ITileSource tileSource = TileSourceFactory.getTileSource(((MenuItem) item).getItemId() - 1000);
                mapView.setTileSource(tileSource);
                miniMapOverlay.setTileSource(tileSource);
        }
        return false;
    }

}