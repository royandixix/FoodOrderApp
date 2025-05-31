package com.example.foodorderapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var menuList: List<Menu> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadMenuList()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showMenuDialog()
        }
    }

    private fun loadMenuList() {
        ApiClient.apiService.getMenu().enqueue(object : Callback<List<Menu>> {
            override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                if (response.isSuccessful) {
                    menuList = response.body().orEmpty()
                    recyclerView.adapter = MenuAdapter(menuList,
                        onItemClick = { menu -> showMenuDialog(true, menu) },
                        onDeleteClick = { menu -> deleteMenu(menu.id) }
                    )
                } else {
                    showToast("Gagal memuat data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun addMenu(menu: Menu) {
        ApiClient.apiService.addMenu(menu).enqueue(object : Callback<Menu> {
            override fun onResponse(call: Call<Menu>, response: Response<Menu>) {
                if (response.isSuccessful) {
                    showToast("Menu berhasil ditambah")
                    loadMenuList()
                } else {
                    showToast("Gagal menambahkan menu")
                }
            }

            override fun onFailure(call: Call<Menu>, t: Throwable) {
                showToast("Gagal tambah menu: ${t.message}")
            }
        })
    }

    private fun updateMenu(id: Int, menu: Menu) {
        ApiClient.apiService.updateMenu(id, menu).enqueue(object : Callback<Menu> {
            override fun onResponse(call: Call<Menu>, response: Response<Menu>) {
                if (response.isSuccessful) {
                    showToast("Menu berhasil diubah")
                    loadMenuList()
                } else {
                    showToast("Gagal mengubah menu")
                }
            }

            override fun onFailure(call: Call<Menu>, t: Throwable) {
                showToast("Gagal ubah menu: ${t.message}")
            }
        })
    }

    private fun deleteMenu(id: Int) {
        ApiClient.apiService.deleteMenu(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                showToast("Menu berhasil dihapus")
                loadMenuList()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Gagal hapus menu: ${t.message}")
            }
        })
    }

    private fun showMenuDialog(isEdit: Boolean = false, menu: Menu? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_menu, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.etName)
        val priceInput = dialogView.findViewById<EditText>(R.id.etPrice)

        if (isEdit && menu != null) {
            nameInput.setText(menu.name)
            priceInput.setText(formatRupiahInput(menu.price))
        }

        priceInput.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    priceInput.removeTextChangedListener(this)
                    val clean = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    current = if (clean.isNotEmpty()) {
                        val formatted = NumberFormat.getNumberInstance(Locale("in", "ID")).format(clean.toLong())
                        "Rp$formatted"
                    } else ""
                    priceInput.setText(current)
                    priceInput.setSelection(current.length)
                    priceInput.addTextChangedListener(this)
                }
            }
        })

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Ubah Menu" else "Tambah Menu")
            .setView(dialogView)
            .setPositiveButton(if (isEdit) "Simpan" else "Tambah", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val priceStr = priceInput.text.toString().replace("[Rp.\\s]".toRegex(), "")
                val price = priceStr.toIntOrNull()

                if (name.isEmpty()) {
                    showToast("Nama tidak boleh kosong")
                    return@setOnClickListener
                }
                if (price == null) {
                    showToast("Harga tidak valid")
                    return@setOnClickListener
                }

                val newMenu = Menu(
                    id = menu?.id ?: 0,
                    name = name,
                    description = "",
                    price = price
                )

                if (isEdit) {
                    updateMenu(menu!!.id, newMenu)
                } else {
                    addMenu(newMenu)
                }

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun formatRupiahInput(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        return "Rp${formatter.format(amount)}"
    }
}
