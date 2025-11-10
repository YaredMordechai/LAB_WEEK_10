package com.example.lab_week_10

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel

class MainActivity : AppCompatActivity() {

    private val db by lazy { prepareDatabase() }
    private val viewModel: TotalViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        // Tampilkan last update jika ada
        val row = db.totalDao().getById(ID)
        val lastDate = row?.total?.date
        if (lastDate != null) {
            Toast.makeText(this, "Last update: $lastDate", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()

        // Observe LiveData total -> update UI
        viewModel.total.observe(this) { value ->
            findViewById<TextView>(R.id.text_total).text =
                getString(R.string.text_total, value)
        }

        // Tombol increment: update ViewModel + simpan LANGSUNG ke DB (anti reset)
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            val next = (viewModel.total.value ?: 0) + 1
            // Update UI state
            viewModel.setTotal(next)
            // Persist segera ke DB (REPLACE)
            db.totalDao().upsert(
                Total(
                    id = ID,
                    total = TotalObject(
                        value = next,
                        date = java.util.Date().toString()
                    )
                )
            )
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java,
            "total-database"
        )
            .allowMainThreadQueries()           // sesuai modul latihan
            .fallbackToDestructiveMigration()   // aman saat kamu ganti schema/version
            .build()
    }

    private fun initializeValueFromDatabase() {
        val row = db.totalDao().getById(ID)
        if (row == null) {
            // Pertama kali: insert value 0 + date sekarang
            db.totalDao().upsert(
                Total(
                    id = ID,
                    total = TotalObject(
                        value = 0,
                        date = java.util.Date().toString()
                    )
                )
            )
            viewModel.setTotal(0)
        } else {
            viewModel.setTotal(row.total.value)
        }
    }

    override fun onPause() {
        super.onPause()
        // (Opsional) Simpan lagi saat pause sebagai backup
        val current = viewModel.total.value ?: 0
        db.totalDao().upsert(
            Total(
                id = ID,
                total = TotalObject(
                    value = current,
                    date = java.util.Date().toString()
                )
            )
        )
    }

    companion object {
        const val ID: Long = 1
    }
}
