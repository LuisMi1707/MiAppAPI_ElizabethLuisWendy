package com.example.miappapi_elizabethluiswendy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miappapi_elizabethluiswendy.api.ActualizarRequest
import com.example.miappapi_elizabethluiswendy.api.RetrofitClient
import com.example.miappapi_elizabethluiswendy.model.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var tvMensaje: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvProductos: RecyclerView
    private lateinit var etIdBuscar: EditText
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText

    private lateinit var etIdActualizar: EditText
    private lateinit var etIdEliminar: EditText


    private lateinit var adapter: ProductoAdapter
    private val listaProductos = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular vistas
        tvMensaje = findViewById(R.id.tvMensaje)
        progressBar = findViewById(R.id.progressBar)
        rvProductos = findViewById(R.id.rvProductos)
        etIdBuscar = findViewById(R.id.etIdBuscar)
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecio)
        etIdActualizar = findViewById(R.id.etIdActualizar)
        etIdEliminar = findViewById(R.id.etIdEliminar)

        val btnMostrarTodos = findViewById<Button>(R.id.btnMostrarTodos)
        val btnBuscarPorId = findViewById<Button>(R.id.btnBuscarPorId)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        // Configurar RecyclerView
        adapter = ProductoAdapter(listaProductos)
        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = adapter

        // BOTÓN MOSTRAR TODOS
        btnMostrarTodos.setOnClickListener {
            mostrarTodos()
        }

        // BOTÓN BUSCAR POR ID
        btnBuscarPorId.setOnClickListener {
            val id = etIdBuscar.text.toString()
            if (id.isNotEmpty()) {
                buscarPorId(id.toInt())
            } else {
                Toast.makeText(this, "Ingresa un ID", Toast.LENGTH_SHORT).show()
            }
        }

        // BOTÓN ACTUALIZAR
        btnActualizar.setOnClickListener {
            val idTexto = etIdActualizar.text.toString()
            val nombre = etNombre.text.toString()
            val descripcion = etDescripcion.text.toString()
            val precio = etPrecio.text.toString()

            if (idTexto.isEmpty()) {
                Toast.makeText(this, "Ingresa el ID del producto a actualizar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty() || precio.isEmpty()) {
                Toast.makeText(this, "Completa nombre y precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actualizarProducto(idTexto.toInt(), nombre, descripcion, precio.toDouble())
        }
        // BOTÓN ELIMINAR - CORREGIDO
        btnEliminar.setOnClickListener {
            val idTexto = etIdEliminar.text.toString()
            if (idTexto.isNotEmpty()) {
                val id = idTexto.toInt()
                eliminarProducto(id)
            } else {
                Toast.makeText(this, "Ingresa un ID para eliminar", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar productos al iniciar
        mostrarTodos()
    }

    // MOSTRAR TODOS LOS PRODUCTOS
    private fun mostrarTodos() {
        mostrarCarga(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.obtenerTodos()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val productos = response.body()?.data ?: emptyList()
                        listaProductos.clear()
                        listaProductos.addAll(productos)
                        adapter.notifyDataSetChanged()
                        tvMensaje.text = " Mostrados ${productos.size} productos"
                    } else {
                        tvMensaje.text = " Error: ${response.code()}"
                        Log.e("API_ERROR", "Código: ${response.code()}")
                    }
                    mostrarCarga(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvMensaje.text = " Error de conexión: ${e.message}"
                    Log.e("API_ERROR", "Excepción", e)
                    mostrarCarga(false)
                }
            }
        }
    }

    // BUSCAR POR ID
    private fun buscarPorId(id: Int) {
        mostrarCarga(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.obtenerPorId(id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val prod = response.body()?.data
                        if (prod != null) {
                            // LIMPIAR LA LISTA Y MOSTRAR SOLO ESTE PRODUCTO
                            listaProductos.clear()
                            listaProductos.add(prod)
                            adapter.notifyDataSetChanged()
                            tvMensaje.text = " Mostrando solo ID: ${prod.id} | ${prod.nombre} | $${prod.precio}"
                        } else {
                            listaProductos.clear()
                            adapter.notifyDataSetChanged()
                            tvMensaje.text = " Producto no encontrado"
                        }
                    } else {
                        listaProductos.clear()
                        adapter.notifyDataSetChanged()
                        tvMensaje.text = " Error ${response.code()}: Producto no encontrado"
                    }
                    mostrarCarga(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listaProductos.clear()
                    adapter.notifyDataSetChanged()
                    tvMensaje.text = " Error: ${e.message}"
                    Log.e("API_ERROR", "Excepción buscar", e)
                    mostrarCarga(false)
                }
            }
        }
    }
    // ACTUALIZAR PRODUCTO
    private fun actualizarProducto(id: Int, nombre: String, descripcion: String, precio: Double) {
        mostrarCarga(true)
        tvMensaje.text = "Actualizando ID: $id..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val datos = ActualizarRequest(nombre, descripcion, precio)
                val response = RetrofitClient.instance.actualizar(id, datos)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        tvMensaje.text = " ${response.body()?.message}"
                        Toast.makeText(this@MainActivity, "Producto ID $id actualizado", Toast.LENGTH_SHORT).show()

                        // Limpiar campos
                        etIdActualizar.text.clear()
                        etNombre.text.clear()
                        etDescripcion.text.clear()
                        etPrecio.text.clear()

                        mostrarTodos() // Refrescar lista
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                        tvMensaje.text = " Error ${response.code()}: $errorBody"
                    }
                    mostrarCarga(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvMensaje.text = " Error: ${e.message}"
                    Log.e("API_ERROR", "Excepción actualizar", e)
                    mostrarCarga(false)
                }
            }
        }
    }
    // ELIMINAR PRODUCTO - CORREGIDO
    private fun eliminarProducto(id: Int) {
        mostrarCarga(true)
        tvMensaje.text = "Eliminando ID: $id..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("API_DELETE", "Intentando eliminar ID: $id")
                val response = RetrofitClient.instance.eliminar(id)

                withContext(Dispatchers.Main) {
                    Log.d("API_DELETE", "Respuesta código: ${response.code()}")

                    if (response.isSuccessful) {
                        val mensaje = response.body()?.message ?: "Eliminado correctamente"
                        tvMensaje.text = " $mensaje"
                        Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
                        mostrarTodos() // Refrescar lista
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                        tvMensaje.text = " Error ${response.code()}: $errorBody"
                        Log.e("API_DELETE", "Error ${response.code()}: $errorBody")
                    }
                    mostrarCarga(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvMensaje.text = " Error de conexión: ${e.message}"
                    Log.e("API_DELETE", "Excepción", e)
                    mostrarCarga(false)
                }
            }
        }
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }
}