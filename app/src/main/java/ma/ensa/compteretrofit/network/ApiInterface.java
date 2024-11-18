package ma.ensa.compteretrofit.network;


import java.util.List;

import ma.ensa.compteretrofit.models.Compte;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("banque/comptes")
    Call<List<Compte>> getAllComptes(@Header("Accept") String acceptHeader);


    @PUT("banque/comptes/{id}")
    Call<Compte> updateCompte(@Path("id") Long id, @Body Compte compte);

    @DELETE("banque/comptes/{id}")
    Call<Void> deleteCompte(@Path("id") Long id);
    @POST("banque/comptes")
    Call<Compte> addCompte(@Body Compte compte);

}
