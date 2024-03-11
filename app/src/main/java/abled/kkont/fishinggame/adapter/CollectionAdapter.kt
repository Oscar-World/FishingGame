package abled.kkont.fishinggame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import abled.kkont.fishinggame.data.Fish
import abled.kkont.fishinggame.databinding.ItemCollectionBinding

class CollectionAdapter() : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    var collectionItemList:ArrayList<Fish> = ArrayList()
    lateinit var binding: ItemCollectionBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {

        binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CollectionViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return collectionItemList.size
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {

        holder.onBind(collectionItemList[position])

    }

    class CollectionViewHolder(val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(fish:Fish){

            if(fish.name == ""){ // 도감 정보 없는 경우

                binding.linearLayoutIsNotFish.visibility = View.VISIBLE
                binding.linerLayoutIsFish.visibility = View.GONE

            } else {

                binding.linearLayoutIsNotFish.visibility = View.GONE
                binding.linerLayoutIsFish.visibility = View.VISIBLE
                binding.textViewFishName.text = fish.name
                binding.textViewFishSize.text = "${fish.length}cm"
                binding.imageViewCollectedFish.setImageResource(fish.icon)

            }

        }

    }

}