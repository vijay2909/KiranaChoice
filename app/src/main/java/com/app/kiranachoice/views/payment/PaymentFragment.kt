package com.app.kiranachoice.views.payment

import android.app.Application
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.App
import com.app.kiranachoice.MainActivity
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.DialogBookingConfirmedBinding
import com.app.kiranachoice.databinding.FragmentPaymentBinding
import com.app.kiranachoice.utils.DELIVERY_FREE
import com.app.kiranachoice.utils.Mailer
import com.app.kiranachoice.utils.toPriceAmount
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class PaymentFragment : Fragment() {

    private var bindingPayment: FragmentPaymentBinding? = null
    private val binding get() = bindingPayment!!
    private lateinit var viewModel: PaymentViewModel


    private lateinit var manager: NotificationManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = PaymentViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(PaymentViewModel::class.java)

        bindingPayment = FragmentPaymentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.paymentViewModel = viewModel

        manager = requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        return binding.root
    }


    private val args: PaymentFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allProducts.observe(viewLifecycleOwner, {
            viewModel.cartItems = it
            setProductListTextWithPrice()
        })

        binding.btnPlaceOrder.setOnClickListener {
            binding.btnPlaceOrder.isEnabled = false
            binding.progressBar.root.visibility = View.VISIBLE
            viewModel.saveUserOrder(args.deliveryAddress)
        }

        /**
         * this runs when order will saved
         * Then -> send confirmation mail
         * Then -> show order successful dialog
         * */
        viewModel.orderSaved.observe(viewLifecycleOwner, {
            if (it) {
                sendConfirmationMail()
                viewModel.orderSaveFinished()
            }
        })
    }


    private fun sendConfirmationMail() {
        val amountWithDeliveryCharge = if (viewModel.deliveryCharge.value == DELIVERY_FREE) {
            viewModel.totalProductsAmount.value
        } else {
            val amount = viewModel.totalProductsAmount.value.toString().filter { it.isDigit() }
                .removeSuffix("00")
            amount.toInt().plus(viewModel.deliveryCharge.value?.toInt()!!)
                .toString().toPriceAmount()
        }

        Mailer.sendMail(
            requireContext(),
            "vijaysaini2925@gmail.com",
            viewModel.user?.name.toString(),
            viewModel.orderId.toString(),
            viewModel.orderPlacedDate,
            viewModel.totalProductsAmount.value.toString(),
            getString(R.string.rupee).plus(viewModel.deliveryCharge.value.toString()),
            amountWithDeliveryCharge.toString()
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showBookingConfirmDialog(); showNotification() }
    }


    private fun showBookingConfirmDialog() {
        val bookingConfirmView =
            DialogBookingConfirmedBinding.inflate(LayoutInflater.from(requireContext()))

        binding.progressBar.root.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(bookingConfirmView.root)
            .setCancelable(false)
            .show()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bookingConfirmView.btnOk.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_paymentFragment_to_homeFragment)
            viewModel.removeCartItems()
            dialog.dismiss()
        }
    }

    private fun showNotification() {

        val pendingIntent = NavDeepLinkBuilder(requireContext())
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.myOrdersFragment)
//            .setArguments(bundle)
            .createPendingIntent()


        val builder = NotificationCompat.Builder(requireContext(), App.CHANNEL_ORDER_BOOKED_ID)
        builder.setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle(getString(R.string.order_booked))
            .setContentText("Thank you for order ${viewModel.user?.name?.substringBefore(" ")}")
            .setContentIntent(pendingIntent)
            .setColor(resources.getColor(R.color.colorPrimaryDark, null))
            .setOnlyAlertOnce(true)
            .priority = NotificationCompat.PRIORITY_HIGH


        manager.notify(1, builder.build())
    }


    private fun setProductListTextWithPrice() {
        bindingPayment?.productLayout?.removeAllViews()
        viewModel.cartItems?.forEach { cartItem ->
            val relativeLayout = RelativeLayout(requireContext())

            val textProductName = TextView(requireContext())
            textProductName.setTextColor(Color.BLACK)
            textProductName.id = View.generateViewId()
            textProductName.textSize = 15f
            textProductName.text = cartItem.productTitle

            val textSellingPriceAndQuantity = TextView(requireContext())
            textSellingPriceAndQuantity.textSize = 12f
            textSellingPriceAndQuantity.text =
                getString(R.string.quantity).plus(" ${cartItem.quantity}")
                    .plus(" ${getString(R.string.multiply)}")
                    .plus(" ${getString(R.string.rupee)}")
                    .plus(" ${cartItem.productPrice}")

            val textProductPrice = TextView(requireContext())
            val totalPrice =
                cartItem.quantity.toInt().times(cartItem.productPrice.toInt())
            textProductPrice.id = View.generateViewId()
            textProductPrice.setTextColor(Color.BLACK)
            textProductPrice.textSize = 15f
            textProductPrice.text = getString(R.string.rupee).plus(" $totalPrice")

            val param0 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param0.setMargins(0, 0, 0, 24)

            val param1 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param1.setMargins(0, 0, 16, 0)
            param1.addRule(RelativeLayout.ALIGN_PARENT_START)
            param1.addRule(RelativeLayout.START_OF, textProductPrice.id)

            val param2 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param2.setMargins(32, 0, 0, 0)
            param2.addRule(RelativeLayout.BELOW, textProductName.id)

            val param3 = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            param3.addRule(RelativeLayout.ALIGN_PARENT_END)


            relativeLayout.layoutParams = param0
            textProductName.layoutParams = param1
            textSellingPriceAndQuantity.layoutParams = param2
            textProductPrice.layoutParams = param3


            relativeLayout.addView(textProductName)
            relativeLayout.addView(textSellingPriceAndQuantity)
            relativeLayout.addView(textProductPrice)

            bindingPayment?.productLayout?.addView(relativeLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingPayment = null
    }

}

@Suppress("UNCHECKED_CAST")
class PaymentViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PaymentViewModel(application) as T
    }
}