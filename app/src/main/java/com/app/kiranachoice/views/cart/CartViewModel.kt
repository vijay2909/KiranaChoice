package com.app.kiranachoice.views.cart

import androidx.lifecycle.*
import com.app.kiranachoice.data.database_models.CartItem
import com.app.kiranachoice.data.network_models.CouponModel
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.DELIVERY_CHARGE
import com.app.kiranachoice.utils.DELIVERY_FREE
import com.app.kiranachoice.utils.MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {

    private var discount : Double = 0.0

    suspend fun getCartItems() = withContext(Dispatchers.IO) { dataRepository.getCartItems() }


    init {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("Saved amount: ${dataRepository.getSavedAmount().value}")
        }
    }

    val cartSubTotal = dataRepository.getTotalInvoiceAmount()


    val savedAmount = dataRepository.getSavedAmount()


    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount


    /**
     * calculate delivery fee based upon subTotal amount
     * */
    val deliveryFee: LiveData<String> = Transformations.map(cartSubTotal) { subTotal ->
        if (subTotal.toInt() < MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE) {
            _totalAmount.value = subTotal.toInt().plus(DELIVERY_CHARGE).toString()
            DELIVERY_CHARGE.toString()
        } else {
            _totalAmount.value = subTotal.toString()
            DELIVERY_FREE
        }
    }


    /**
     * this function actually not update the item
     * hence, this function replace current product to exist product
     * */
    fun updateQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            dataRepository.addToCart(cartItem)
        }
    }


    /**
     * Remove product from cart
     * */
    fun removeCartItem(cartItem: CartItem) = viewModelScope.launch {
        dataRepository.removeFromCart(cartItem)
    }


    init {
        dataRepository.getCoupons()
    }


    /**
     * Get Coupon List from network
     * */
    val couponsList = dataRepository.couponList


    val toastForAlreadyAppliedCoupon: LiveData<Boolean> get() = dataRepository.toastForAlreadyAppliedCoupon

    fun eventShowAppliedCouponToastComplete() = dataRepository.eventShowAppliedCouponToastComplete()

    /**
     * on applied coupon button click check first that this coupon could be apply or not
     * if this coupon already applied then [[toastForAlreadyAppliedCoupon]] liveData get value from repo and
     * cartFragment show a toast otherwise this coupon will apply
     * */
    fun couponApplied(couponModel: CouponModel) = dataRepository.couponApplied(couponModel)


    private var _showCoupon = MutableLiveData<Boolean>()
    val showCoupon: LiveData<Boolean> get() = _showCoupon


    val couponDescription: LiveData<String> =
        Transformations.map(dataRepository.couponDiscount) { couponModel ->

            _showCoupon.value = true
            if (couponModel.discountRate != null) {
                val discount =
                    cartSubTotal.value?.toDouble()?.times(couponModel.discountRate!!.toDouble())
                        ?.div(100)
                discount?.let { updateAmount(it) }
                "₹$discount"
            } else {
                val description = couponModel.couponDescription!!.toLowerCase(Locale.getDefault())
                description.substringBefore("on").trim().plus(" Free")
            }
        }


    private fun updateAmount(discount: Double) {
        this.discount = discount
        _totalAmount.value = totalAmount.value.toString().toDouble().minus(discount).toString()
    }


    fun removeCoupon() {
        _showCoupon.value = false
        _totalAmount.value = totalAmount.value.toString().toDouble().plus(discount).toString()
        dataRepository.removeCoupon()
    }

    override fun onCleared() {
        super.onCleared()
        _showCoupon.value = false
    }

}