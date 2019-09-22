package com.foodbodi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.SearchResultItem
import com.foodbodi.controller.MyAdapter
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class SearchRestaurantActivity : AppCompatActivity() {

    val delay:Long = 1000
    var lastTextChangeMoment:Long? = null;
    var searchText:String? = null;
    var searchResultView:RecyclerView? = null
    private lateinit var viewAdapter: SearchResultAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    val searchAction:Runnable = Runnable {
        if (lastTextChangeMoment != null) {
            if (System.currentTimeMillis() >= (lastTextChangeMoment!! + delay)) {
                this.doSearch()
            }
        }
    }
    val handler:Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_restaurant)

        val searchBox:EditText = findViewById<EditText>(R.id.edit_text_search_box)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                lastTextChangeMoment = Date().time;
                if (searchText != null) {
                    handler.postDelayed(searchAction, delay)
                }
            }

            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(searchAction)
                searchText = text.toString();

            }

        })

        viewManager = LinearLayoutManager(this)
        viewAdapter = SearchResultAdapter(ArrayList(), object : OnSearchItemClick {
            override fun onRestaurant(restaurantId: String?) {
                if (restaurantId != null) {
                    val intent = Intent(this@SearchRestaurantActivity, RestaurantDetailActivity::class.java)
                    intent.putExtra(RestaurantDetailActivity.RESTAURANT_ID, restaurantId)
                    startActivity(intent)
                }
            }

            override fun onFood(restaurantId: String?) {
                if (restaurantId != null) {
                    val intent = Intent(this@SearchRestaurantActivity, RestaurantDetailActivity::class.java)
                    intent.putExtra(RestaurantDetailActivity.RESTAURANT_ID, restaurantId)
                    startActivity(intent)
                }
            }

        })
        searchResultView = findViewById<RecyclerView>(R.id.search_restaurant_result)
        searchResultView.apply {
            this?.setHasFixedSize(true)

            this?.layoutManager = viewManager

            this?.adapter = viewAdapter
        }
    }

    private fun doSearch() {
        Toast.makeText(this, "Searching $searchText", Toast.LENGTH_LONG).show()
        FoodbodiRetrofitHolder.getService().searchRestaurant(FoodbodiRetrofitHolder.getHeaders(this), searchText!!)
            .enqueue(object : Callback<FoodBodiResponse<ArrayList<SearchResultItem>>> {
                override fun onResponse(
                    call: Call<FoodBodiResponse<ArrayList<SearchResultItem>>>,
                    response: Response<FoodBodiResponse<ArrayList<SearchResultItem>>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode)
                    {
                        val items:ArrayList<SearchResultItem> = response.body()?.data!!
                        this@SearchRestaurantActivity.renderSearchResult(items)
                    } else {
                        Toast.makeText(this@SearchRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<FoodBodiResponse<ArrayList<SearchResultItem>>>, t: Throwable) {
                    Toast.makeText(this@SearchRestaurantActivity, t.message, Toast.LENGTH_LONG).show()
                }

            })
    }

    private fun renderSearchResult(items:ArrayList<SearchResultItem>) {
        viewAdapter.update(items)
    }
}

class SearchResultRecycleViewHolder(val container: LinearLayout) : RecyclerView.ViewHolder(container) {

}

class SearchResultAdapter(private var dataSet:ArrayList<SearchResultItem>, val clickListener:OnSearchItemClick) : RecyclerView.Adapter<SearchResultRecycleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultRecycleViewHolder {
        val container:LinearLayout = LayoutInflater.from(parent.context).inflate(R.layout.search_reault_item, parent,false) as LinearLayout
        return SearchResultRecycleViewHolder(container)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: SearchResultRecycleViewHolder, position: Int) {
        val itemData = dataSet.get(position)
        val kind = itemData.data?.kind;
        val photoView = holder.container.findViewById<ImageView>(R.id.search_item_photo);
        holder.container.findViewById<TextView>(R.id.search_item_title).text = itemData.data?.document?.name
        holder.container.findViewById<TextView>(R.id.search_item_kind).text = kind?.toUpperCase()
        if ("restaurants".equals(kind)) {
            holder.container.findViewById<TextView>(R.id.search_item_info).text = itemData.data?.document?.address
            val photoUrls:ArrayList<String>? = itemData.data?.document?.photos
            if (photoUrls!!.size > 0) {
                val photoUrl = itemData.data?.document?.photos?.get(0)
                if (photoUrl != null) {
                    Picasso.get().load(photoUrl).fit().centerCrop().into(photoView)
                }
            }
        } else if ("foods".equals(kind)) {
            val photoUrl = itemData.data?.document?.photo
            if (photoUrl != null) {
                Picasso.get().load(photoUrl).fit().centerCrop().into(photoView)
            }
        }

        holder.container.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if ("restaurants".equals(kind)) {
                    this@SearchResultAdapter.clickListener.onRestaurant(itemData.data?.document_id)
                } else if ("foods".equals(kind)) {
                    this@SearchResultAdapter.clickListener.onFood(itemData.data?.document?.restaurant_id)
                }
            }

        })

    }

    fun update(newDataSet:ArrayList<SearchResultItem>) {
        dataSet = newDataSet
        this.notifyDataSetChanged()
    }

}

interface OnSearchItemClick {
    fun onRestaurant(restaurantId:String?)

    fun onFood(restaurantId:String?)
}