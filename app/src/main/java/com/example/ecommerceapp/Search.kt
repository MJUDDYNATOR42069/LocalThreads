package com.example.ecommerceapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ecommerceapp.databinding.ActivitySearchBinding
import com.example.ecommerceapp.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class Search : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var productAdapter: ProductAdapter

    private val db = FirebaseFirestore.getInstance()

    private var allProducts = mutableListOf<Product>()
    private var filteredProducts = mutableListOf<Product>()

    private var minPrice = 0f
    private var maxPrice = 2000f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBackButton()
        setupFilterDrawer()
        setupSearch()
        setupSorting()
        setupPriceSlider()
        loadProducts()


    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(filteredProducts)
        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(this@Search, 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupFilterDrawer() {
        binding.btnFilter.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.filterDrawer)
        }

        binding.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(binding.filterDrawer)
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
            binding.drawerLayout.closeDrawer(binding.filterDrawer)
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun setupSearch() {
        binding.Search.addTextChangedListener {
            applyFilters()
        }
    }

    private fun setupSorting() {
        binding.spinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    sortProducts()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupPriceSlider() {
        binding.priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            minPrice = values[0]
            maxPrice = values[1]
            binding.tvPriceRange.text = "R${minPrice.toInt()} - R${maxPrice.toInt()}"
        }
    }

    private fun loadProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                allProducts.clear()
                for (document in documents) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        category = document.getString("category") ?: "",
                        size = document.getString("size") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        colour = document.getString("colour") ?: "",
                        brand = document.getString("brand") ?: "",
                        condition = document.getString("condition") ?: "",
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    allProducts.add(product)
                }
                applyFilters()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load products", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        val searchText = binding.Search.text.toString().trim().lowercase()
        val selectedCategories = mutableListOf<String>()
        if (binding.cbTshirts.isChecked) selectedCategories.add("T-Shirts")
        if (binding.cbShoes.isChecked) selectedCategories.add("Shoes")
        if (binding.cbDresses.isChecked) selectedCategories.add("Dresses")
        if (binding.cbAccessories.isChecked) selectedCategories.add("Accessories")

        val selectedSizes = mutableListOf<String>()
        if (binding.cbSizeS.isChecked) selectedSizes.add("S")
        if (binding.cbSizeM.isChecked) selectedSizes.add("M")
        if (binding.cbSizeL.isChecked) selectedSizes.add("L")
        if (binding.cbSizeXL.isChecked) selectedSizes.add("XL")

        val selectedBrands = mutableListOf<String>()
        if (binding.cbBrandLocal.isChecked) selectedBrands.add("LocalThreads")
        if (binding.cbBrandZama.isChecked) selectedBrands.add("Zama Designs")
        if (binding.cbBrandMusa.isChecked) selectedBrands.add("Musa Apparel")
        if (binding.cbBrandMasa.isChecked) selectedBrands.add("Masa Designs")

        val selectedCondition = when {
            binding.rbNew.isChecked -> "New"
            binding.rbSecondHand.isChecked -> "Second-hand"
            else -> ""
        }

        filteredProducts = allProducts.filter { product ->
            val matchesSearch = searchText.isEmpty() ||
                    product.name.lowercase().contains(searchText) ||
                    product.category.lowercase().contains(searchText) ||
                    product.brand.lowercase().contains(searchText)

            val matchesCategory = selectedCategories.isEmpty() || selectedCategories.contains(product.category)
            val matchesSize = selectedSizes.isEmpty() || selectedSizes.contains(product.size)
            val matchesPrice = product.price >= minPrice && product.price <= maxPrice
            val matchesBrand = selectedBrands.isEmpty() || selectedBrands.contains(product.brand)
            val matchesCondition = selectedCondition.isEmpty() || product.condition.equals(selectedCondition, ignoreCase = true)

            matchesSearch && matchesCategory && matchesSize && matchesPrice && matchesBrand && matchesCondition
        }.toMutableList()

        sortProducts()
    }

    private fun sortProducts() {
        val sortedProducts = when (binding.spinnerSort.selectedItemPosition) {
            1 -> filteredProducts.sortedBy { it.price }
            2 -> filteredProducts.sortedByDescending { it.price }
            3 -> filteredProducts.sortedBy { it.name.lowercase() }
            4 -> filteredProducts.sortedByDescending { it.name.lowercase() }
            else -> filteredProducts
        }

        filteredProducts.clear()
        filteredProducts.addAll(sortedProducts)
        productAdapter.updateProducts(filteredProducts)
    }

    private fun clearFilters() {
        binding.cbTshirts.isChecked = false
        binding.cbShoes.isChecked = false
        binding.cbDresses.isChecked = false
        binding.cbAccessories.isChecked = false

        binding.cbSizeS.isChecked = false
        binding.cbSizeM.isChecked = false
        binding.cbSizeL.isChecked = false
        binding.cbSizeXL.isChecked = false

        binding.cbBrandLocal.isChecked = false
        binding.cbBrandZama.isChecked = false
        binding.cbBrandMusa.isChecked = false
        binding.cbBrandMasa.isChecked = false

        binding.rbNew.isChecked = false
        binding.rbSecondHand.isChecked = false

        binding.priceSlider.values = listOf(0f, 2000f)
        minPrice = 0f
        maxPrice = 2000f
        binding.tvPriceRange.text = "R0 - R2000"
        binding.Search.text.clear()
        binding.drawerLayout.closeDrawer(binding.filterDrawer)
        applyFilters()
    }
}
