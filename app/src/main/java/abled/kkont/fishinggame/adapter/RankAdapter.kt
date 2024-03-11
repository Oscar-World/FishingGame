package abled.kkont.fishinggame.adapter

import abled.kkont.fishinggame.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import abled.kkont.fishinggame.data.Rank
import abled.kkont.fishinggame.databinding.ItemRankBinding
import android.view.View


class RankAdapter: RecyclerView.Adapter<RankAdapter.RankViewHolder>() {

    var rankList = ArrayList<Rank>()
    lateinit var binding: ItemRankBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {

        binding = ItemRankBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return RankViewHolder(binding)

    } // onCreateViewHolder()


    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {

        holder.onBind(rankList[position])

    } // onBindViewHolder()


    override fun getItemCount(): Int {

        return rankList.size

    } // getItemCount()


    class RankViewHolder(private val binding: ItemRankBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(rank: Rank){

            var icon = 0

            when (adapterPosition) {

                0 -> {
                    binding.linearLayoutRankItem.setBackgroundResource(R.drawable.background_gold)
                    binding.textViewRank.visibility = View.INVISIBLE
                }
                1 -> {
                    binding.linearLayoutRankItem.setBackgroundResource(R.drawable.background_silver)
                    binding.textViewRank.visibility = View.INVISIBLE
                }
                2 -> {
                    binding.linearLayoutRankItem.setBackgroundResource(R.drawable.background_bronze)
                    binding.textViewRank.visibility = View.INVISIBLE
                }
                else -> {
                    binding.linearLayoutRankItem.setBackgroundResource(0)
                    binding.textViewRank.visibility = View.VISIBLE
                }

            }

            when (rank.type) {

                "호수" -> {

                    icon = R.drawable.bidaning_uh
                    binding.imageViewTargetImage.setBackgroundResource(0)
                    binding.textViewRankContent.text = "${rank.content}cm"

                }

                "강" -> {

                    icon = R.drawable.gamulchi
                    binding.imageViewTargetImage.setBackgroundResource(0)
                    binding.textViewRankContent.text = "${rank.content}cm"

                }

                "바닷가" -> {

                    icon = R.drawable.dageumbari
                    binding.imageViewTargetImage.setBackgroundResource(0)
                    binding.textViewRankContent.text = "${rank.content}cm"

                }

                "태평양" -> {

                    icon = R.drawable.gorae
                    binding.imageViewTargetImage.setBackgroundResource(0)
                    binding.textViewRankContent.text = "${rank.content}cm"

                }

                "기부왕" -> {

                    icon = R.drawable.coin
                    binding.imageViewTargetImage.setBackgroundResource(0)
                    binding.textViewRankContent.text = "${rank.content}꼰"

                }

                "낚시대" -> {

                    icon = R.drawable.rod_wood

                    // 낚시대 색 지정
                    when(Integer.parseInt(rank.content)){

                        0-> binding.imageViewTargetImage.setBackgroundResource(R.drawable.dialog_background_normal)
                        in 1..9 -> binding.imageViewTargetImage.setBackgroundResource(R.drawable.dialog_background_magic)
                        in 10..19 -> binding.imageViewTargetImage.setBackgroundResource(R.drawable.dialog_background_rare)
                        in 20..29 -> binding.imageViewTargetImage.setBackgroundResource(R.drawable.dialog_background_unique)
                        else -> binding.imageViewTargetImage.setBackgroundResource(R.drawable.dialog_background_legendary)

                    }

                    binding.textViewRankContent.text = "+${rank.content}"

                }

            }

            binding.imageViewTargetImage.setImageResource(icon)
            binding.textViewRank.text = (adapterPosition+1).toString()
            binding.textViewNickname.text = rank.nickname



        } // onBind()

    } // RankViewHolder

}