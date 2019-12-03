package com.foodbodi

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.Adapters.NamesOfFoodsAdapter
import com.foodbodi.controller.Fragments.GetTodayCaloriesData
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.Food
import com.foodbodi.model.Restaurant
import com.foodbodi.utils.Action
import com.foodbodi.utils.ProgressHUD
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [NameOfFoodsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [NameOfFoodsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class NameOfFoodsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    var foods:ArrayList<Food> = ArrayList();
    //Outlet.
    private lateinit var rvForyou:RecyclerView;


    val firestore = FirebaseFirestore.getInstance()
    private var unitID: String? = null

    // Here I want to get unitID


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        var view:View = inflater.inflate(R.layout.fragment_name_of_foods, container, false);
        rvForyou = view.findViewById(R.id.rvForYou);
        rvForyou.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = NamesOfFoodsAdapter(foods);
        }
        this@NameOfFoodsFragment.getNameOfFoodsFromFirbase();
        return view;
    }
    private fun  getNameOfFoodsFromFirbase(){
        ProgressHUD.instance.showLoading(getActivity())
        firestore.collection("foods").whereEqualTo("restaurant_id",unitID).
            get().addOnSuccessListener { querySnapshot ->
            ProgressHUD.instance.hideLoading()
            var foryou:Food = Food();
            foryou.name = "For you";
            foryou.restaurant_id = "";
            foryou.amount = 0;
            foryou.calo = 0.0;
            foods.add(0,foryou);

            val limit = CurrentUserProvider.get().getRemainCaloToEat();
            for (document in querySnapshot.documents) {
                val r = document.toObject(Food::class.java);
                if (r != null) {
                    if (r.calo?.compareTo(limit) == -1) {
                        foods.add(r)
                    }
                }
            }

            var menu:Food = Food();
            menu.name = "Name of foods";
            menu.restaurant_id = "";
            menu.amount = 0;
            menu.calo = 0.0;
            foods.add(menu);
            for (document in querySnapshot.documents) {
                val r = document.toObject(Food::class.java);
                foods.add(r!!)
            }

            if (foods.size > 0){
                val adapter = NamesOfFoodsAdapter(foods);
                rvForyou.adapter = adapter;
                (rvForyou.adapter as NamesOfFoodsAdapter).reloadData(foods);
            }
        }
            .addOnFailureListener(OnFailureListener {
                ProgressHUD.instance.hideLoading()
            })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(unitID: String): NameOfFoodsFragment {
            val fragment = NameOfFoodsFragment();
            fragment.unitID = unitID
            return fragment
        }
    }
}
