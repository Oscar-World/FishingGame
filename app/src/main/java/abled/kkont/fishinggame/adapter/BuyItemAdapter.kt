package abled.kkont.fishinggame.adapter

import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.databinding.ItemRodBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BuyItemAdapter(val clickListener : BuyItemAdapter.ItemClickListener, val context:Context) :
    RecyclerView.Adapter<BuyItemAdapter.BuyItemViewHolder>() {

    private var buyItemArrayList : ArrayList<Int> = arrayListOf(10,15)
    lateinit var binding : ItemRodBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyItemViewHolder {
        binding = ItemRodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyItemViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BuyItemViewHolder, position: Int) {
        holder.onBind(buyItemArrayList[position])
    }

    override fun getItemCount(): Int {
        return buyItemArrayList.count()
    }

    inner class BuyItemViewHolder(private val binding: ItemRodBinding) : RecyclerView.ViewHolder(binding.root){

        fun onBind(rod:Int){
            binding.textViewRodName.text = "+${rod} 낚싯대"
            if(rod==0){
                binding.textViewRodName.setTextColor(ContextCompat.getColor(context,R.color.normal))
            }else if(rod < 10){
                binding.textViewRodName.setTextColor(ContextCompat.getColor(context,R.color.magic))
            }else if(rod < 20){
                binding.textViewRodName.setTextColor(ContextCompat.getColor(context,R.color.rare))
            }else if(rod <30){
                binding.textViewRodName.setTextColor(ContextCompat.getColor(context,R.color.unique))
            }else if(rod == 30){
                binding.textViewRodName.setTextColor(ContextCompat.getColor(context,R.color.legendary))

            }
            binding.linearLayoutItem.setOnClickListener {

                clickListener.onBuyItemClick(it, adapterPosition)

            }
        }
    }

    interface ItemClickListener {

        fun onBuyItemClick(view : View, position : Int)

    } // ItemClickListener

}