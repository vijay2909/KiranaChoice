package com.app.kiranachoice.views.contact_us

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentContactUsBinding
import com.app.kiranachoice.utils.isNotNullOrEmpty
import com.google.android.material.snackbar.Snackbar

class ContactUsFragment : Fragment() {

    private var _bindingContact: FragmentContactUsBinding? = null
    private val binding get() = _bindingContact!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingContact = FragmentContactUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMessageSend.setOnClickListener {
            if (validateDetails()){
                val msg = binding.etMessage.text.toString().trim()
                sendMail(msg)
            }
        }
    }

    private fun validateDetails(): Boolean {
        return (binding.etEmail.isNotNullOrEmpty(getString(R.string.empty_string_error_msg))
                && binding.etMessage.isNotNullOrEmpty(getString(R.string.empty_msg_error_msg)))
    }

    private fun sendMail(msg: String) {
        val to = arrayOf("kiranachoice@gmail.com")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, to)
            putExtra(Intent.EXTRA_TEXT, msg)
        }
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Snackbar.make(
                requireView(),
                "There is no email client installed.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingContact = null
    }

}