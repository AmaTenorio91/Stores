package com.curso.stores

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.curso.stores.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), OnClickListener, MainAux {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)//Configuramos nuestro binding

        /*mBinding.btnSave.setOnClickListener {
            val store = StoreEntity(name = mBinding.etName.text.toString().trim())
            Thread {
            StoreApplication.getStoreDb().storeDao().addStore(store)
        }.start()
            mAdapter.add(store) //Con esta linea se anade y se ve reflejado nuestro componente agregado al adaptador
        }*/

        mBinding.fab.setOnClickListener { launchEditStoreFragment()}

        setUpRecylerView ()
    } //Fin del OnCreate


    private fun launchEditStoreFragment(args: Bundle? = null) {
        val fragment = EditStoreFragment() //Creamos una instancia del fragmento

        if(args != null) fragment.arguments = args

        val fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.containerMain, fragment) //Se coloca el contenerdor donde se alojara el fragmento
        fragmentTransaction.addToBackStack(null) //Con esta linea podemos regresar a nuestra actividad
        fragmentTransaction.commit()
        //mBinding.fab.hide()
        hideFab()//Por defecto es falso por lo que esta escondido el boton fab
    }

    private fun setUpRecylerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, resources.getInteger(R.integer.main_columns))
        getAllStores()

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    private fun getAllStores(){
        //Lineas para ejecutar de forma asincrona las consultas y cuando este lista se va setear al adaptador
        doAsync {
            val stores = StoreApplication.getStoreDb().storeDao().getAllStores()
            uiThread {
                mAdapter.setStores(stores)
            }
        }
    }

    /*
    * OnClickListener
    * */
    override fun onClick(storeId: Long) {
        val args =  Bundle()
        args.putLong(getString(R.string.arg_id), storeId) //Traemos
        launchEditStoreFragment(args)
    }

    override fun onFavoriteStore(storeEntity: StoreEntity) {
        storeEntity.isFavorite = !storeEntity.isFavorite //Valor actualizado
            doAsync {
                StoreApplication.getStoreDb().storeDao().updateStore(storeEntity)
                uiThread {
                    mAdapter.update(storeEntity)
                }
            }
    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
    /* Se pasa por una serie de opciones antes de eliminar, que sera primero eliminar, llamar, y finalmente
    * mostrar el sitio Web de la tienda que estamos seleccionando */
        val items = resources.getStringArray(R.array.array_options_item)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_option_title)
            .setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->
                when(i){
                    0 -> confirmDelete(storeEntity)
                    1 -> dial(storeEntity.phone)
                    2 -> goToWebsite(storeEntity.website)
                }
            }).show()
    }

    private fun confirmDelete(storeEntity: StoreEntity){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm, DialogInterface.OnClickListener { dialogInterface, i ->
                doAsync {
                    StoreApplication.getStoreDb().storeDao().deleteStore(storeEntity)
                    uiThread {
                        mAdapter.delete(storeEntity)
                    }
                } //Fin de la ejecucion en segundo plano
            })
            .setNegativeButton(R.string.dialog_delet_cancel, null)
            .show()
    }

    private fun dial(phone: String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel: $phone")
        }
        startIntent(callIntent)
    }

    private fun goToWebsite(website: String) {
        if (website.isEmpty()) {
        Toast.makeText(this, R.string.main_error_no_website, Toast.LENGTH_SHORT).show()
        } else if(URLUtil.isValidUrl(website)) {
            val websiteIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(website)
            }
            startIntent(websiteIntent)
        } else {
         Toast.makeText(this, R.string.main_error_url_invalid, Toast.LENGTH_SHORT).show()
        }
            }


    private fun startIntent (intent: Intent){
        if (intent.resolveActivity(packageManager) != null) //Verifica que exista una actividad o aplicacion que pueda resolver este intent
            startActivity(intent)
        else
            Toast.makeText(this, R.string.main_error_no_resolve, Toast.LENGTH_SHORT).show()
    }

    /*
    * MainAux
    * */
    override fun hideFab(isVisible: Boolean) {
        if(isVisible) mBinding.fab.show() else mBinding.fab.hide()
    }

    override fun addStore(storeEntity: StoreEntity) {
        mAdapter.add(storeEntity)
    }

    override fun updateStore(storeEntity: StoreEntity) {
        mAdapter.update(storeEntity)
    }
}

