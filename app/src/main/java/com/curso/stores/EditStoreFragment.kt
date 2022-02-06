package com.curso.stores

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.curso.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class EditStoreFragment : Fragment() {

    lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean =  false
    private var mStoreEntity: StoreEntity? =  null  //Puede ser o no ser nulla

    //Vinculamos la vista XML con nuestro codigo
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    /*Se ha creado por completo, es el momento ideal para comenzar a manipular todos los elementos*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Lineas para pasar los argumentos desde un activity a un fragment
        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if(id != null && id != 0L){
            mIsEditMode = true //Indicamos con el valor a true que estamos en modo edicion
            getStore(id) //Funcion que recibe el id como parametro y conseguir los datos de la tienda seleccionada
        }else {
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setUpActionBar()
        setupTextFields()

    }

    private fun setUpActionBar() {
        mActivity = activity as? MainActivity //Conseguimos la actividad en la cual esta alojada este fragmento y la vamos a
        //castear como MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)//Configuramos un boton de retroceso
        mActivity?.supportActionBar?.title = if(mIsEditMode) getString(R.string.edit_store_title_edit) //Configuramos un titulo para esta pantalla
                                            else  getString(R.string.edit_store_title_add)
        //Linea para tener acceso al menu
        setHasOptionsMenu(true)
    }

    private fun setupTextFields() {
        with(mBinding){
            //Con el addTextChangedListener, se ejecuta lo que hallamos escrito despues de que el texto halla sido cambiado
            etName.addTextChangedListener{ validateFields(tilName) }
            etPhone.addTextChangedListener{ validateFields(tilPhone) }
            etPhotoUrl.addTextChangedListener{
                validateFields(tilPhotoUrl)
                loadImage(it.toString().trim())
            }
        }
    }

    private fun loadImage(url: String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imgPhoto)
    }

    private fun getStore(id: Long) {
        doAsync {
            mStoreEntity = StoreApplication.getStoreDb().storeDao().getStoreById(id) //Almacenamos los datos en la variable mStoreEntity
            uiThread {
                if(mStoreEntity != null) setUiStore(mStoreEntity!!)
            }
        }
    }

    private fun setUiStore(storeEntity: StoreEntity) {
        with(mBinding){
            etName.setText(storeEntity.name)//Asignamos al campo etName el nombre actual de la tienda seleccionada
            //etPhone.setText(storeEntity.phone)
            etPhone.text = storeEntity.phone.editable() //Utilizando la extencion queda de esa manera
            //etWebsite.setText(storeEntity.website)
            etWebsite.text = storeEntity.website.editable()
            //etPhotoUrl.setText(storeEntity.photoUrl)
            etPhotoUrl.text = storeEntity.photoUrl.editable()


            Glide.with(requireActivity())
                .load(storeEntity.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imgPhoto)
        }
    }

    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)


    //Agregamos el menu creado a la vista del fragmento a travez de un inflater
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                //Con el doble signo de exclamacion decimos que la variable no va ser nulla
               if(mStoreEntity != null && validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone, mBinding.tilName)) {
                   /*val store = StoreEntity(name = mBinding.etName.text.toString().trim(),
                       phone = mBinding.etPhone.text.toString().trim(),
                       website = mBinding.etWebsite.text.toString().trim(),
                       photoUrl = mBinding.etPhotoUrl.text.toString().trim())*/


     //Con la misma variable global funcione en ambos casos, ya que si es una edicion este agarra la que halla
     //consultado de la base de datos de lo contrario sera cero
                   with (mStoreEntity!!){
                       name = mBinding.etName.text.toString().trim()
                       phone = mBinding.etPhone.text.toString().trim()
                       website = mBinding.etWebsite.text.toString().trim()
                       photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                   }
                   doAsync {
                       if(mIsEditMode) StoreApplication.getStoreDb().storeDao().updateStore(mStoreEntity!!)
                       else  mStoreEntity!!.id = StoreApplication.getStoreDb().storeDao().addStore(mStoreEntity!!) //Linea para guardar
                       uiThread {
                           hideKeyboard() //Metodo para ocultar el teclado virtual de la aplicacion
                           if(mIsEditMode){
                                mActivity?.updateStore(mStoreEntity!!)
                               Snackbar.make(mBinding.root, R.string.edit_store_message_update_succes, Snackbar.LENGTH_LONG).show()
                           } else {
                               mActivity?.addStore(mStoreEntity!!) //Agregamos un objeto store a nuestro adapter con interfaces
                               Snackbar.make(
                                   mBinding.root,
                                   getString(R.string.edit_store_message_succes),
                                   Snackbar.LENGTH_LONG
                               ).show()
                               mActivity?.onBackPressed()
                           }
                       }
                   }
               } //Fin del condicional if
                true
            } //Fin de la opcion save
            else -> super.onOptionsItemSelected(item)  //Por defecto when debe tener una sentencia
        }
    }

    private fun validateFields (vararg textFields: TextInputLayout) : Boolean {
        var isValid = true

        for(textField in textFields){
            if(textField.editText?.text.toString().trim().isEmpty()){
                textField.error = getString(R.string.helper_required)
                textField.editText?.requestFocus()
                isValid = false
            } else textField.error = null
        }

        return isValid
    }

    private fun validateFields(): Boolean {
        var isValid = true //Por default pensamos que el ususario lleno el formulario correctamente

        if(mBinding.etPhotoUrl.text.toString().trim().isEmpty()){
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            mBinding.etPhotoUrl.requestFocus()
            isValid = false //Quiere decir que el usuario no lleno un campo del formulario
        }

        if(mBinding.etPhone.text.toString().trim().isEmpty()){
            mBinding.tilPhone.error = getString(R.string.helper_required)
            mBinding.etPhone.requestFocus()
            isValid = false //Quiere decir que el usuario no lleno un campo del formulario
        }

        if(mBinding.etName.text.toString().trim().isEmpty()){
            mBinding.tilName.error = getString(R.string.helper_required)
            mBinding.etName.requestFocus()
            isValid = false //Quiere decir que el usuario no lleno un campo del formulario
        }
        return isValid
    }

    /*
    *Se esconde el teclado virtual usando la clase InputMethodManager, llamando al metodo hideSoftInputFromWindow,
    * enviando el token de la ventana que contiene la vista enfocada
    * */
    private fun hideKeyboard(){
        val view : View? = mActivity?.currentFocus //Se comprueba que ninguna vista tiene el foco
        if(view != null){
            val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /*
    * Metodo que se ejecuta justo antes de onDestroy, es en donde se desvincula nuestra vista y es el momento ideal
    * porque antes de llamar al super todavia tenemos control de nuestra vista
    * */
    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)//Se quita el boton de retroceso
        mActivity?.supportActionBar?.title = getString(R.string.app_name)//Permanece el titulo de la app
        mActivity?.hideFab(true)
        setHasOptionsMenu(false) //No hay acceso al menu del actionBar
        super.onDestroy()
    }
}