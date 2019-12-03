package com.foodbodi

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.CommentRequest
import com.foodbodi.utils.ProgressHUD
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChatFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var lvChat:ListView;
    private lateinit var txtEnterText:EditText;
    private lateinit var btnSend:Button;
    private var listChat = ArrayList<CommentRequest>()
    val firestore = FirebaseFirestore.getInstance()
    private var unitID: String? = null

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

        var view: View = inflater.inflate(R.layout.fragment_chat, container, false);
        lvChat = view.findViewById(R.id.lvChat);
        btnSend = view.findViewById(R.id.btnSend);
        txtEnterText = view.findViewById(R.id.txtMessage);
        firestore.collection("comments").whereEqualTo("restaurant_id",unitID).
            get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val commentData = document.toObject(CommentRequest::class.java);
                if (commentData != null) {
                    listChat.add(commentData);
                }
            }
            var notesAdapter = NotesAdapter(this.requireContext(), listChat)
            lvChat.adapter = notesAdapter;
            notesAdapter.notifyDataSetChanged();
        }
            .addOnFailureListener(OnFailureListener {
                ProgressHUD.instance.hideLoading()
            })
        btnSend.setOnClickListener { view ->
            if (txtEnterText.text.toString().length > 0){
                ProgressHUD.instance.showLoading(this.requireActivity())
                var comment:CommentRequest = CommentRequest();
                comment.restaurant_id = unitID;
                comment.message = txtEnterText.text.toString();
                FoodbodiRetrofitHolder.getService().
                    addCommentRestaurant(FoodbodiRetrofitHolder.getHeaders(this.requireContext()),comment).enqueue(object :
                    Callback<FoodBodiResponse<CommentRequest>> {

                    override fun onFailure(call: Call<FoodBodiResponse<CommentRequest>>, t: Throwable) {
                        ProgressHUD.instance.hideLoading()
                        Toast.makeText(context,"Sorry! we can't add your comment",Toast.LENGTH_LONG).show();
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<CommentRequest>>,
                        response: Response<FoodBodiResponse<CommentRequest>>
                    ) {
                        ProgressHUD.instance.hideLoading()
                        if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                            txtEnterText.setText("");
                            var message = response.body()?.data()?.message;
                            var resId = response.body()?.data?.restaurant_id;
                            listChat.add(comment);

                            var notesAdapter = NotesAdapter(context, listChat)
                            lvChat.adapter = notesAdapter;
                            notesAdapter.notifyDataSetChanged();

                        }else{
                            Toast.makeText(context,"Sorry! we can't add your comment",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(this.context,"Text is empty",Toast.LENGTH_LONG).show();
            }


        }
        return view;
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
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(unitID: String): ChatFragment {
            val fragment = ChatFragment()
            fragment.unitID = unitID
            return fragment
        }
    }

    inner class NotesAdapter : BaseAdapter {

        private var notesList = ArrayList<CommentRequest>()
        private var context: Context? = null

        constructor(context: Context?, notesList: ArrayList<CommentRequest>) : super() {
            this.notesList = notesList
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val view: View?
            val vh: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.list_comment_restaurant, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
                Log.i("JSA", "set Tag for ViewHolder, position: " + position)
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }
            vh.tvContent.text = notesList[position].message.toString()
            return view
        }

        override fun getItem(position: Int): Any {
            return notesList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return notesList.size
        }
    }

    private class ViewHolder(view: View?) {
        val tvContent: TextView
        init {
            this.tvContent = view?.findViewById(R.id.tvContent) as TextView
        }
    }
}

