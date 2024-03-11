package abled.kkont.fishinggame.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import abled.kkont.fishinggame.data.Fish
import abled.kkont.fishinggame.databinding.ItemInventoryBinding
import android.view.View

class InventoryAdapter(val clickListener : ItemClickListener) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    var inventoryItemList : ArrayList<Fish> = ArrayList()
    lateinit var binding : ItemInventoryBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {

        binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return InventoryViewHolder(binding, clickListener)

    } // onCreateViewHolder()


    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {

        holder.onBind(inventoryItemList[position])

    } // onBindViewHolder()


    override fun getItemCount(): Int {

        return inventoryItemList.size

    } // getItemCount()


    fun setItem(inventoryItemList : ArrayList<Fish>) {

        this.inventoryItemList = inventoryItemList
        notifyDataSetChanged()

    } // setItem()


    class InventoryViewHolder(val binding: ItemInventoryBinding, val clickListener : ItemClickListener) : RecyclerView.ViewHolder(binding.root){


        fun onBind(fish: Fish){

            binding.imageViewItem.setImageResource(fish.icon)
            binding.textViewItem.text = fish.name

            binding.linearLayoutItem.setOnClickListener {

                clickListener.onClick(it, adapterPosition)

            }

        } // onBind()

    } // InventoryViewHolder


    interface ItemClickListener {

        fun onClick(view : View, position : Int)

    } // ItemClickListener

}