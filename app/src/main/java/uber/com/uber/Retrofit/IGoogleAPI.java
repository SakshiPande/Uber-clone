package uber.com.uber.Retrofit;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import uber.com.uber.pojo.DirectionsPojo;

/**
 * Created by SAKSHI on 1/18/2018.
 */

public interface IGoogleAPI {

    @GET("/maps/api/directions/json?key=AIzaSyCxyQhG3x0QNi7zFPDYjGnxqs0cIkHnAS8")
    Call<DirectionsPojo> getPath(@Query("origin") String origin,@Query("mode") String mode,@Query("transit_routing_prefrence") String routing_prefrence,
                                 @Query("destination") String destination);

}
