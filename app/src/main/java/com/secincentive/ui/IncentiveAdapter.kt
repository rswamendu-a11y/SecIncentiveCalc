package com.secincentive.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.secincentive.databinding.ItemIncentiveRowBinding

class IncentiveAdapter(
    private val onRowUpdated: (UiRowItem) -> Unit
) : ListAdapter<UiRowItem, IncentiveAdapter.IncentiveViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncentiveViewHolder {
        val binding = ItemIncentiveRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IncentiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncentiveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IncentiveViewHolder(private val binding: ItemIncentiveRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UiRowItem) {
            binding.label.text = item.label
            binding.rateInfo.text = item.rateInfo

            // Handle Checkbox
            binding.checkboxOption.visibility = if (item.hasCheckbox) View.VISIBLE else View.GONE
            // Remove listener before setting state to avoid loops
            binding.checkboxOption.setOnCheckedChangeListener(null)
            binding.checkboxOption.isChecked = item.isChecked

            // Handle EditText
            // Remove text watcher to avoid loops during binding
            binding.inputQty.tag = null // Tag used to prevent re-entry if needed, or simple removing listener logic
            // Ideally we remove specific watcher, but cleaner is to just not add duplicates
            // We'll clear listeners by recreating or robust handling.
            // Since we can't easily "remove" an anonymous TextWatcher, let's use a tag approach or simple replacement if view recycled.
            // Simplified approach: Clear previous logic?
            // Better: Set text without triggering listener logic?
            // Standard way: removeTextChangedListener. But we need reference.
            // Let's implement a custom simplified way or just accept overhead of recreating listener if simple.
            // Actually, ListAdapter with DiffUtil + setText often triggers watchers.
            // Workaround: tag the watcher.

            // Reset fields
            if (binding.inputQty.text.toString() != item.quantity.toString()) {
                 // Only update if different to avoid cursor jumps
                 if (item.quantity == 0 && binding.inputQty.text.toString().isEmpty()) {
                     // Do nothing, visual matches logic (0 usually shown as empty or 0)
                     binding.inputQty.setText("0")
                 } else {
                     binding.inputQty.setText(item.quantity.toString())
                 }
            }

            // Set Listeners
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val qty = s?.toString()?.toIntOrNull() ?: 0
                    if (item.quantity != qty) {
                        item.quantity = qty
                        // Update UI subtotal for this row immediately?
                        // Or wait for diff?
                        // Let's update the calculated total view immediately for UX
                        updateTotal(item)
                        // Callback
                        onRowUpdated(item)
                    }
                }
            }
            // Clear old watchers?
            // In a simple generic adapter without custom view view classes, hard to clear specific previous watcher.
            // But we can set the OnFocusChangeListener to add/remove watcher or simple tag hack.
            // Hack: Tag holds the watcher.
            val oldWatcher = binding.inputQty.getTag(com.secincentive.R.id.input_qty) as? TextWatcher
            if (oldWatcher != null) {
                binding.inputQty.removeTextChangedListener(oldWatcher)
            }
            binding.inputQty.addTextChangedListener(watcher)
            binding.inputQty.setTag(com.secincentive.R.id.input_qty, watcher)


            binding.checkboxOption.setOnCheckedChangeListener { _, isChecked ->
                if (item.isChecked != isChecked) {
                    item.isChecked = isChecked
                    onRowUpdated(item)
                }
            }

            updateTotal(item)
        }

        private fun updateTotal(item: UiRowItem) {
            val total = item.quantity * item.unitIncentive
            binding.calculatedTotal.text = "₹ ${String.format("%.0f", total)}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UiRowItem>() {
        override fun areItemsTheSame(oldItem: UiRowItem, newItem: UiRowItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: UiRowItem, newItem: UiRowItem): Boolean {
            return oldItem == newItem
        }
    }
}
