package uber.com.uber.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SAKSHI on 1/31/2018.
 */

public class StartLocation {



        @SerializedName("lat")
        @Expose
        private Float lat;
        @SerializedName("lng")
        @Expose
        private Float lng;

        public Float getLat() {
            return lat;
        }

        public void setLat(Float lat) {
            this.lat = lat;
        }

        public Float getLng() {
            return lng;
        }

        public void setLng(Float lng) {
            this.lng = lng;
        }

    }

