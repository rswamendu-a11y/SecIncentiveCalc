package com.secincentive

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.secincentive.databinding.ActivityMainBinding
import com.secincentive.ui.CalculatorViewModel
import com.secincentive.ui.IncentiveAdapter
import com.secincentive.ui.UiRowItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CalculatorViewModel

    // Adapters
    private lateinit var phoneAdapter: IncentiveAdapter
    private lateinit var wearableAdapter: IncentiveAdapter
    private lateinit var tabletAdapter: IncentiveAdapter
    private lateinit var pcAdapter: IncentiveAdapter
    private lateinit var careAdapter: IncentiveAdapter
    private lateinit var bundleAdapter: IncentiveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]

        setupAdapters()
        setupInputs()
        observeViewModel()
    }

    private fun setupAdapters() {
        // Callback logic
        val onUpdate: (UiRowItem) -> Unit = { item ->
            viewModel.updateItem(item)
        }

        phoneAdapter = IncentiveAdapter(onUpdate)
        binding.rvSmartphones.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = phoneAdapter
        }

        wearableAdapter = IncentiveAdapter(onUpdate)
        binding.rvWearables.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = wearableAdapter
        }

        tabletAdapter = IncentiveAdapter(onUpdate)
        binding.rvTablets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tabletAdapter
        }

        pcAdapter = IncentiveAdapter(onUpdate)
        binding.rvPc.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pcAdapter
        }

        careAdapter = IncentiveAdapter(onUpdate)
        binding.rvCarePlus.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = careAdapter
        }

        bundleAdapter = IncentiveAdapter(onUpdate)
        binding.rvBundles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bundleAdapter
        }
    }

    private fun setupInputs() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val target = binding.inputTargetVolume.text.toString().toDoubleOrNull() ?: 0.0
                val achieved = binding.inputAchievedVolume.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateTargetSettings(target, achieved)
            }
        }
        binding.inputTargetVolume.addTextChangedListener(watcher)
        binding.inputAchievedVolume.addTextChangedListener(watcher)
    }

    private fun observeViewModel() {
        val scope = CoroutineScope(Dispatchers.Main)

        // Lists
        scope.launch { viewModel.smartphoneList.collectLatest { phoneAdapter.submitList(it) } }
        scope.launch { viewModel.wearableList.collectLatest { wearableAdapter.submitList(it) } }
        scope.launch { viewModel.tabletList.collectLatest { tabletAdapter.submitList(it) } }
        scope.launch { viewModel.pcList.collectLatest { pcAdapter.submitList(it) } }
        scope.launch { viewModel.careList.collectLatest { careAdapter.submitList(it) } }
        scope.launch { viewModel.bundleList.collectLatest { bundleAdapter.submitList(it) } }

        // Calculation Result
        scope.launch {
            viewModel.uiState.collectLatest { result ->
                // Subtotals
                binding.tvSmartphoneSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.smartphoneIncentive)}"
                binding.tvWearableSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.wearableIncentive)}"
                binding.tvTabletSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.tabletIncentive)}"
                binding.tvPcSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.pcIncentive)}"
                binding.tvCareSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.careIncentive)}"
                binding.tvBundleSubtotal.text = "Subtotal: ₹ ${String.format("%.0f", result.bundleIncentive)}"

                // Grand Totals
                // Gross = Sum of all raw incentives (for display) or just Grand Total?
                // Let's use Grand Total for Net Payable
                binding.tvGrossTotal.text = "Gross Total: ₹ ${String.format("%.0f", result.grandTotal)}" // Simplified mapping
                binding.tvNetPayable.text = "Net Payable: ₹ ${String.format("%.0f", result.grandTotal)}"

                // Visual Feedback
                if (result.warnings.isNotEmpty()) {
                    binding.tvNetPayable.setTextColor(Color.RED)
                    // Show only first warning as toast to avoid spam
                    // Toast.makeText(this@MainActivity, result.warnings.first(), Toast.LENGTH_SHORT).show()
                    // Toast spam is annoying on every keystroke. Better: Set error on InputLayout?
                    // For now, just color change as requested.
                } else {
                    binding.tvNetPayable.setTextColor(Color.parseColor("#2E7D32")) // Green
                }
            }
        }
    }
}
