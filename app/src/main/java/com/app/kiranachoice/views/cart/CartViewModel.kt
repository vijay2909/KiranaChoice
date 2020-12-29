package com.app.kiranachoice.views.cart

import androidx.lifecycle.*
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.CouponModel
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.DELIVERY_CHARGE
import com.app.kiranachoice.utils.DELIVERY_FREE
import com.app.kiranachoice.utils.MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE
import kotlinx.coroutines.launch
import java.util.*

class CartViewModel(val dataRepository: DataRepository) : ViewModel() {


    val allCartItems: LiveData<List<CartItem>> = dataRepository.allCartItems


    private var _savedAmount = MutableLiveData("0")
    val savedAmount: LiveData<String> get() = _savedAmount


    /**
     * calculate subTotal and saved amount
     * */
    val subTotalAmount: LiveData<String> = Transformations.map(allCartItems) { cartItems ->
        var subTotal = 0
        var savedAmount = 0
        cartItems.forEach { cartItem ->
            subTotal += cartItem.quantity.toInt().times(cartItem.productPrice.toInt())

            savedAmount += if (cartItem.productMRP.toInt() > cartItem.productPrice.toInt()) {
                (cartItem.productMRP.toInt()
                    .minus(cartItem.productPrice.toInt())).times(cartItem.quantity.toInt())
            } else {
                0
            }
        }
        _savedAmount.postValue(savedAmount.toString())
        subTotal.toString()
    }


    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount


    /**
     * calculate delivery fee based upon subTotal amount
     * */
    val deliveryFee: LiveData<String> = Transformations.map(subTotalAmount) { subTotal ->
        if (subTotal.toInt() < MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE) {
            _totalAmount.value = subTotal.toInt().plus(DELIVERY_CHARGE).toString()
            DELIVERY_CHARGE.toString()
        } else {
            _totalAmount.value = subTotal
            DELIVERY_FREE
        }
    }


    /**
     * this function actually not update the item
     * hence, this function replace current product to exist product
     * */
    fun updateQuantity(cartItem: CartItem, quantity: Int) {
        viewModelScope.launch {
            val item = CartItem(
                productKey = cartItem.productKey,
                productSKU = cartItem.productSKU,
                productTitle = cartItem.productTitle,
                productImageUrl = cartItem.productImageUrl,
                productMRP = cartItem.productMRP,
                productPrice = cartItem.productPrice,
                packagingSize = cartItem.packagingSize,
                quantity = quantity.toString()
            )
            dataRepository.insert(item)
        }
    }


    /**
     * Remove product from cart
     * */
    fun removeCartItem(cartItem: CartItem) = viewModelScope.launch {
        dataRepository.delete(cartItem)
    }


    init {
        dataRepository.getCoupons()
    }


    /**
     * Get Coupon List from network
     * */
    val couponsList = dataRepository.couponList


    val toastForAlreadyAppliedCoupon: LiveData<Boolean> get() = dataRepository.toastForAlreadyAppliedCoupon

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
                    subTotalAmount.value?.toDouble()?.times(couponModel.discountRate!!.toDouble())
                        ?.div(100)
                discount?.let { updateAmount(it) }
                "₹$discount"
            } else {
                val description = couponModel.couponDescription!!.toLowerCase(Locale.getDefault())
                description.substringBefore("on").trim().plus(" Free")
            }
        }


    private fun updateAmount(discount: Double) {
        _totalAmount.value = totalAmount.value.toString().toDouble().minus(discount).toString()
    }


    fun removeCoupon() {
        _showCoupon.value = false
        viewModelScope.launch { dataRepository.removeCoupon() }
    }

}