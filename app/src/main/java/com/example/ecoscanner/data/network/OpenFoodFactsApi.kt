package com.example.ecoscanner.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ---------------- DTOs ----------------

data class OpenFoodFactsResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("code") val code: String?,
    @SerializedName("product") val product: ProductDto?
)

data class ProductDto(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_es") val productNameEs: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_front_url") val imageFrontUrl: String?,
    @SerializedName("origins") val origins: String?,
    @SerializedName("countries") val countries: String?,
    @SerializedName("countries_tags") val countriesTags: List<String>?,
    @SerializedName("brands") val brands: String?,
    // --- Nuevos campos para matching Km 0 ---
    @SerializedName("categories_tags") val categoriesTags: List<String>?,
    @SerializedName("labels_tags") val labelsTags: List<String>?,
    @SerializedName("nutriscore_grade") val nutriscoreGrade: String?,
    @SerializedName("ecoscore_grade") val ecoscoreGrade: String?,
    @SerializedName("code") val code: String?
)

// Respuesta del endpoint de búsqueda por categoría
data class CategorySearchResponse(
    @SerializedName("count") val count: Int?,
    @SerializedName("page_count") val pageCount: Int?,
    @SerializedName("products") val products: List<ProductDto>?
)

// ---------------- Retrofit Service ----------------

interface OpenFoodFactsApi {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse

    // Busca productos en una categoría concreta de un país concreto.
    // Ejemplo: /cgi/search.pl?action=process&tagtype_0=categories&tag_contains_0=contains
    //   &tag_0=cookies&tagtype_1=countries&tag_contains_1=contains&tag_1=spain
    //   &page_size=20&json=1
    @GET("cgi/search.pl?action=process&tagtype_0=categories&tag_contains_0=contains&tagtype_1=countries&tag_contains_1=contains&json=1")
    suspend fun searchByCategoryAndCountry(
        @Query("tag_0") category: String,
        @Query("tag_1") country: String = "spain",
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_by") sortBy: String = "popularity"
    ): CategorySearchResponse
}

object OpenFoodFactsClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}