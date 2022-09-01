package com.eraqi.chatsdk.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eraqi.chatlib.Stomp
import com.eraqi.chatsdk.LoadingButton
import com.eraqi.chatsdk.R
import com.eraqi.chatsdk.databinding.FragmentAllUsersBinding
import com.eraqi.chatsdk.presentation.AllUsersFragmentViewModel
import com.eraqi.chatsdk.ui.adapters.AllUsersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify

class AllUsersFragment: Fragment() {
    lateinit var binding: FragmentAllUsersBinding
    lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllUsersBinding.inflate(layoutInflater)
        return binding.root
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            Stomp.initSDK("ws://192.168.1.14:8080/ws/websocket")
            Stomp.connect()
            Stomp.subscribe("/topic/chat/${arguments?.getString("phone")}")
        }
            recyclerView = binding.rvUsers
            val userViewModel = ViewModelProvider(this)[AllUsersFragmentViewModel::class.java]
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            val adapter = AllUsersAdapter(requireContext(), arrayListOf(), itemClick)
            recyclerView.adapter = adapter

            lifecycleScope.launch {

                userViewModel.getUsersFlow().collect {
                    withContext(Dispatchers.Main){
                        adapter.addItems(it)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            userViewModel.getUsers(arguments?.getString("phone")!!)
            requireActivity().onBackPressedDispatcher.addCallback{
                findNavController().navigate(R.id.action_allUsersFragment_to_loginFragment)
            }
        }


 val itemClick: (String)->Unit = { phone: String->
     run {
         println(phone)
         findNavController().navigate(
             R.id.action_allUsersFragment_to_chatFragment,
             bundleOf("my_phone" to phone)
         )
     }
 }
}