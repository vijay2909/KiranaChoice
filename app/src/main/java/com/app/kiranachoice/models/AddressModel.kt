package com.app.kiranachoice.models

data class AddressesContainer(val address : List<AddressModel>)

data class AddressModel(
    var key: String? = null,
    var flat_num_or_building_name: String? = null,
    var area: String? = null,
    var city: String? = null,
    var pincode: String? = null,
    var state: String? = null
)