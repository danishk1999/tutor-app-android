package com.example.myapplication.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.Activity.DetailActivity
import com.example.myapplication.Domain.TutorModel
import com.example.myapplication.databinding.ViewholderTopTutorBinding

class TopTutorAdapter(val items: MutableList<TutorModel>):RecyclerView.Adapter<TopTutorAdapter.Viewholder>() {
    private var context:Context?=null


    class Viewholder(val binding: ViewholderTopTutorBinding):
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopTutorAdapter.Viewholder {
        context=parent.context
        val binding= ViewholderTopTutorBinding.inflate(LayoutInflater.from(context),parent,false)
        return Viewholder(binding)

    }

    override fun onBindViewHolder(holder: TopTutorAdapter.Viewholder, position: Int) {
        holder.binding.nameTxt.text = items[position].Name
        holder.binding.specialTxt.text=items[position].Special
        holder.binding.scoreTxt.text=items[position].Rating.toString()
        holder.binding.yearTxt.text=items[position].Experience.toString()+" Year"

        Glide.with(holder.itemView.context)
            .load(items[position].Picture)
            .apply{ RequestOptions().transform(CenterCrop()) }
            .into(holder.binding.tutorimg)

        holder.itemView.setOnClickListener{
            val intent= Intent(context,DetailActivity::class.java)
            intent.putExtra("object",items[position])
            context!!.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}