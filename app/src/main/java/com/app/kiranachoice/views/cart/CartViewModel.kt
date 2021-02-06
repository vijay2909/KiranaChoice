package com.app.kiranachoice.views.cart

import androidx.lifecycle.*
import com.app.kiranachoice.data.CouponModel
import com.app.kiranachoice.data.db.ProductItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.DELIVERY_CHARGE
import com.app.kiranachoice.utils.DELIVERY_FREE
import com.app.kiranachoice.utils.MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {


    val allCartItems: LiveData<List<ProductItem>> = dataRepository.allCartItems


    private var _savedAmount = MutableLiveData("0")
    val savedAmount: LiveData<String> get() = _savedAmount


    /**
     * calculate subTotal and saved amount
     * */
    val subTotalAmount: LiveData<String> = Transformations.map(allCartItems) { products ->
        var subTotal = 0
        var savedAmount = 0
        products.forEach { product ->
            subTotal += product.orderQuantity.times(product.packagingSize[product.packagingIndex].price!!.toInt())

            val mrp = product.packagingSize[product.packagingIndex].mrp!!.toInt()
            val price = product.packagingSize[product.packagingIndex].price!!.toInt()

            savedAmount += if (mrp > price) {
                (mrp.minus(price)).times(product.orderQuantity)
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
    fun updateQuantity(product: Product) {
        viewModelScope.launch {
            dataRepository.addToCart(product.key)
        }
    }


    /**
     * Remove product from cart
     * */
    fun removeCartItem(product: Product) = viewModelScope.launch {
        dataRepository.removeFromCart(product.key)
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