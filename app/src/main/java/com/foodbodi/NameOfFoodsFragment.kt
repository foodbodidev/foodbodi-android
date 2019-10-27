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
import com.foodbodi.Adapters.NamesOfFoodsAdapter
import com.foodbodi.model.Food
import com.foodbodi.model.Restaurant
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
    var restaurant_id = "NwpoauRh3RDKtMNp2EaY";
    private var listener: OnFragmentInteractionListener? = null
    var foods:ArrayList<Food> = ArrayList();
    //Outlet.
    private lateinit var lvForyou:ListView;
    private lateinit var lvMenu: ListView;

    val firestore = FirebaseFirestore.getInstance()

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
        lvForyou = view.findViewById(R.id.lvForYou);
        lvMenu = view.findViewById(R.id.lvMenu);
        this.getNameOfFoodsFromFirbase();
        return view;
    }
    private fun  getNameOfFoodsFromFirbase(){
        firestore.collection("foods").whereEqualTo("restaurant_id",restaurant_id).
            get().addOnSuccessListener { querySnapshot ->

            for (document in querySnapshot.documents) {
                val r = document.toObject(Food::class.java);
                foods.add(r!!)
            }
            if (foods.size > 0){
                val adapter = NamesOfFoodsAdapter(this.requireContext(),foods);
                lvMenu.adapter = adapter;
                adapter.notifyDataSetChanged();
            }
        }
            .addOnFailureListener(OnFailureListener {

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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NameOfFoodsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NameOfFoodsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
