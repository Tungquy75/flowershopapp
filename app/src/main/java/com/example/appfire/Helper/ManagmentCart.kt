package com.example.appfire.Helper

import android.content.Context
import android.widget.Toast
import com.example.appfire.model.Items

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertItem(item: Items) {
        val listFood = getListCart()
        val itemModel = item.model.firstOrNull() ?: ""

        // Tìm sản phẩm cùng tên và cùng model
        val existingIndex = listFood.indexOfFirst { existingItem ->
            if (itemModel.isEmpty()) {
                existingItem.title == item.title
            } else {
                existingItem.title == item.title && (existingItem.model.firstOrNull() ?: "") == itemModel
            }
        }

        if (existingIndex != -1) {
            // Kiểm tra nếu cộng dồn vượt quá 10 cho sản phẩm này
            val newQuantity = listFood[existingIndex].numberInCart + item.numberInCart
            if (newQuantity > 10) {
                Toast.makeText(context, "Mỗi sản phẩm chỉ được mua tối đa 10 cái", Toast.LENGTH_SHORT).show()
                return
            }
            listFood[existingIndex].numberInCart += item.numberInCart
        } else {
            // Kiểm tra nếu thêm mới vượt quá 10
            if (item.numberInCart > 10) {
                Toast.makeText(context, "Mỗi sản phẩm chỉ được mua tối đa 10 cái", Toast.LENGTH_SHORT).show()
                return
            }
            listFood.add(item)
        }

        tinyDB.putListObject("CartList", listFood)
        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<Items> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    fun minusItem(listFood: ArrayList<Items>, position: Int, listener: ChangeNumberItemsListener) {
        if (listFood[position].numberInCart == 1) {
            listFood.removeAt(position)
        } else {
            listFood[position].numberInCart--
        }
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun plusItem(listFood: ArrayList<Items>, position: Int, listener: ChangeNumberItemsListener) {
        // Kiểm tra nếu tăng lên vượt quá 10 cho sản phẩm này
        if (listFood[position].numberInCart + 1 > 10) {
            Toast.makeText(context, "Mỗi sản phẩm chỉ được mua tối đa 10 cái", Toast.LENGTH_SHORT).show()
            return
        }

        listFood[position].numberInCart++
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listFood = getListCart()
        var fee = 0.0
        for (item in listFood) {
            fee += item.price * item.numberInCart
        }
        return fee
    }
}