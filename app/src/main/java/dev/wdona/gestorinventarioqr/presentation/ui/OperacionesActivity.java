package dev.wdona.gestorinventarioqr.presentation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.presentation.ui.operaciones.OperacionAdapter;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.OperacionViewModel;

public class OperacionesActivity extends AppCompatActivity implements OperacionAdapter.OnOperacionClickListener {

    private OperacionViewModel operacionViewModel;
    private RecyclerView recyclerView;
    private OperacionAdapter operacionAdapter;
    private TextView tvEmpty;
    private Button btnReintentarTodas;
    private Button btnLimpiarEnviadas;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operaciones);

        executor = Executors.newSingleThreadExecutor();

        // Obtener ViewModel desde App
        App app = App.getInstance();
        if (app != null && app.operacionViewModel != null) {
            operacionViewModel = app.operacionViewModel;
        } else {
            Toast.makeText(this, "Error: ViewModel no disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        observarOperaciones();
        cargarOperaciones();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvOperaciones);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnReintentarTodas = findViewById(R.id.btnReintentarTodas);
        btnLimpiarEnviadas = findViewById(R.id.btnLimpiarEnviadas);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        operacionAdapter = new OperacionAdapter(this);
        recyclerView.setAdapter(operacionAdapter);

        // Botón limpiar enviadas
        btnLimpiarEnviadas.setOnClickListener(v -> limpiarOperacionesEnviadas());

        // Botón reintentar todas
        btnReintentarTodas.setOnClickListener(v -> reintentarTodasOperaciones());
    }

    private void observarOperaciones() {
        // Observar el LiveData para actualizar la UI automáticamente
        operacionViewModel.operacionLiveData.observe(this, operaciones -> {
            if (operaciones != null && !operaciones.isEmpty()) {
                operacionAdapter.setOperaciones(operaciones);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                btnReintentarTodas.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                btnReintentarTodas.setVisibility(View.GONE);
            }
        });
    }

    private void cargarOperaciones() {
        executor.execute(() -> {
            operacionViewModel.cargarOperaciones();
            // El Observer se encargará de actualizar la UI
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar operaciones al volver a la activity
        cargarOperaciones();
    }

    @Override
    public void onReintentarClick(Operacion operacion) {
        executor.execute(() -> {
            boolean exito = operacionViewModel.reintentarEnvioOperacion(operacion);

            runOnUiThread(() -> {
                if (exito) {
                    Toast.makeText(this, "Operación reenviada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al reenviar operación", Toast.LENGTH_SHORT).show();
                }
                // El Observer actualiza la UI automáticamente
            });
        });
    }

    private void reintentarTodasOperaciones() {
        executor.execute(() -> {
            boolean exito = operacionViewModel.reintentarEnvioAllOperaciones();

            runOnUiThread(() -> {
                if (exito) {
                    Toast.makeText(this, "Todas las operaciones reenviadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Algunas operaciones fallaron", Toast.LENGTH_SHORT).show();
                }
                // El Observer actualiza la UI automáticamente
            });
        });
    }

    private void limpiarOperacionesEnviadas() {
        executor.execute(() -> {
            operacionViewModel.limpiarOperacionesEnviadas();

            runOnUiThread(() -> {
                Toast.makeText(this, "Operaciones enviadas eliminadas", Toast.LENGTH_SHORT).show();
                // El Observer actualiza la UI automáticamente
            });
        });
    }

    @Override
    protected void onDestroy() {
        if (executor != null) {
            executor.shutdown();
        }
        super.onDestroy();
    }
}
