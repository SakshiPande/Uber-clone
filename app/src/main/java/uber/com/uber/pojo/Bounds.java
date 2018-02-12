package uber.com.uber.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SAKSHI on 1/31/2018.
 */

public class Bounds {


        @SerializedName("northeast")
        @Expose
        private NorthEast northeast;
        @SerializedName("southwest")
        @Expose
        private SouthWest southwest;

        public NorthEast getNortheast() {
            return northeast;
        }

        public void setNortheast(NorthEast northeast) {
            this.northeast = northeast;
        }

        public SouthWest getSouthwest() {
            return southwest;
        }

        public void setSouthwest(SouthWest southwest) {
            this.southwest = southwest;
        }



}
