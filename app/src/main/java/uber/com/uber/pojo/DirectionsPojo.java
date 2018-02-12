package uber.com.uber.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by SAKSHI on 1/31/2018.
 */

public class DirectionsPojo {



        @SerializedName("geocoded_waypoints")
        @Expose
        private List<GeocodedWayPoint> geocodedWaypoints = null;
        @SerializedName("routes")
        @Expose
        private List<uber.com.uber.pojo.Route> routes = null;
        @SerializedName("status")
        @Expose
        private String status;

        public List<GeocodedWayPoint> getGeocodedWaypoints() {
            return geocodedWaypoints;
        }

        public void setGeocodedWaypoints(List<GeocodedWayPoint> geocodedWaypoints) {
            this.geocodedWaypoints = geocodedWaypoints;
        }

        public List<uber.com.uber.pojo.Route> getRoutes() {
            return routes;
        }

        public void setRoutes(List<uber.com.uber.pojo.Route> routes) {
            this.routes = routes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

