package com.curso.stores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.curso.stores.databinding.ItemStoreBinding

class StoreAdapter (private var stores: MutableList<StoreEntity>, private var listener: OnClickListener):
RecyclerView.Adapter<StoreAdapter.ViewHolder>(){

    private lateinit var mContext : Context //Variable global e indica que es un miembro de la clase


    /*Este metodo sirve para inflar la vista en XML que ha sido disenada */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false)

        return ViewHolder(view) //Regresamos una instancia de la Clase Interna ViewHolder
    }

    //Metodo que realiza el dibujado de cada uno de los elementos de la lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores.get(position)

        with(holder){
        setListener(store)
        binding.tvName.text = store.name
        binding.cbFavorite.isChecked = store.isFavorite

            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgPhoto)
        }

    }


    //Metodo que devuelve el tamaño o indica cuantos elementos hay en el adapter
    override fun getItemCount(): Int = stores.size

    fun add(storeEntity: StoreEntity) {
        if (!stores.contains(storeEntity)) {
            stores.add(storeEntity)
            notifyItemInserted(stores.size - 1) //Verifica la ultima posicion pues siempre se anaden al final de la cola
        }
    }

    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores
        notifyDataSetChanged()
    }

    fun update(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)//Regresa el indice de la primera ocurrencia del elemento especificado en la lista
        if(index != -1){
            stores.set(index, storeEntity) //Remplaza el elemente especificado por el elemento storeEntity
            notifyItemChanged(index)//Se refrezca aquel registro que solo halla sido afectado
        }
    }

    fun delete(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index) //Elimina el elementos con el indice
            notifyItemRemoved(index)//Se refrezca aquel registro que solo halla sido afectado
        }
    }


    /*Clase Interna*/
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemStoreBinding.bind(view)

        fun setListener (storeEntity: StoreEntity){
            binding.root.setOnClickListener { listener.onClick(storeEntity.id) }
            binding.cbFavorite.setOnClickListener { listener.onFavoriteStore(storeEntity) }

            binding.root.setOnLongClickListener {
                listener.onDeleteStore(storeEntity)
                true
            }
        }

    } //Fin de la clase interna

}