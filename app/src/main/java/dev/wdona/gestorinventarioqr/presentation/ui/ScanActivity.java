package dev.wdona.gestorinventarioqr.presentation.ui;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.data.api.impl.EstanteriaApiImpl;
import dev.wdona.gestorinventarioqr.data.api.impl.ProductoApiImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.db.AppDatabase;
import dev.wdona.gestorinventarioqr.data.repository.EstanteriaRepositoryImpl;
import dev.wdona.gestorinventarioqr.data.repository.ProductoRepositoryImpl;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.presentation.ui.scan.ProductoScanAdapter;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.EstanteriaViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.ProductoViewModel;
import dev.wdona.gestorinventarioqr.scanner.QRIdentifier;
import dev.wdona.gestorinventarioqr.scanner.ScannerManager;

public class ScanActivity extends AppCompatActivity implements ScannerManager.ScanCallback {

    private ScannerManager scannerManager;
    private TextView tvStatus;
    private TextView tvScannedData;
    private TextView tvEstanteriaInfo;
    private Button btnScan;
    private Button btnStop;
    private RecyclerView rvProductos;
    private ProgressDialog progressDialog;

    private EstanteriaViewModel estanteriaViewModel;
    private ProductoViewModel productoViewModel;

    private Estanteria currentEstanteria;
    private Producto currentProducto;
    private boolean isAsignarProductoAEstanteria = false;
    private ProductoScanAdapter adapter;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        executor = Executors.newSingleThreadExecutor();
        initViewModels();
        initViews();
        initScanner();
    }

    private void initViewModels() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

        EstanteriaLocalDataSourceImpl estanteriaLocal = new EstanteriaLocalDataSourceImpl(db.estanteriaDao());
        ProductoLocalDataSourceImpl productoLocal = new ProductoLocalDataSourceImpl(db.productoDao(), db.estanteriaDao());

        EstanteriaRemoteDataSourceImpl estanteriaRemote = new EstanteriaRemoteDataSourceImpl(new EstanteriaApiImpl());
        ProductoRemoteDataSourceImpl productoRemote = new ProductoRemoteDataSourceImpl(new ProductoApiImpl());

        EstanteriaRepositoryImpl estanteriaRepo = new EstanteriaRepositoryImpl(estanteriaRemote, estanteriaLocal);
        ProductoRepositoryImpl productoRepo = new ProductoRepositoryImpl(productoRemote, productoLocal);

        estanteriaViewModel = new EstanteriaViewModel(estanteriaRepo);
        productoViewModel = new ProductoViewModel(productoRepo);
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvScannedData = findViewById(R.id.tvScannedData);
        tvEstanteriaInfo = findViewById(R.id.tvEstanteriaInfo);
        btnScan = findViewById(R.id.btnScan);
        btnStop = findViewById(R.id.btnStop);
        rvProductos = findViewById(R.id.rvProductos);

        adapter = new ProductoScanAdapter(this::showProductoOptionsDialog);
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        rvProductos.setAdapter(adapter);

        btnScan.setOnClickListener(v -> startScanning());
        btnStop.setOnClickListener(v -> stopScanning());

        btnScan.setEnabled(false);
        btnStop.setEnabled(false);
    }

    private void initScanner() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Inicializando escáner...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        scannerManager = new ScannerManager();
        scannerManager.init(this, this);
    }

    private void startScanning() {
        scannerManager.startScan();
        tvStatus.setText("Escaneando...");
        btnScan.setEnabled(false);
        btnStop.setEnabled(true);
    }

    private void stopScanning() {
        scannerManager.stopScan();
        tvStatus.setText("Listo para escanear");
        btnScan.setEnabled(true);
        btnStop.setEnabled(false);
    }

    @Override
    public void onScanResult(String data) {
        tvScannedData.setText("Último escaneo: " + data);
        stopScanning();
        processScanResult(data);
    }

    @Override
    public void onScanError(String error) {
        runOnUiThread(() -> Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onInitialized(boolean success) {
        progressDialog.dismiss();
        if (success) {
            tvStatus.setText("Escáner listo");
            btnScan.setEnabled(true);
        } else {
            tvStatus.setText("Error al inicializar");
            Toast.makeText(this, "No se pudo inicializar el escáner", Toast.LENGTH_LONG).show();
        }
    }

    private void processScanResult(String data) {
        QRIdentifier.QRResult result = QRIdentifier.identify(data);

        switch (result.type) {
            case ESTANTERIA:
                handleEstanteriaScan(result.id);
                break;
            case PRODUCTO:
                handleProductoScan(result.id);
                break;
            case UNKNOWN:
                Toast.makeText(this, "QR no reconocido: " + data, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleEstanteriaScan(String id) {
        executor.execute(() -> {
            try {
                Long estanteriaId = Long.parseLong(id);
                Estanteria estanteria = estanteriaViewModel.getEstanteriaConProductosById(estanteriaId);

                runOnUiThread(() -> {
                    if (estanteria != null) {
                        currentEstanteria = estanteria;

                        if (isAsignarProductoAEstanteria) {
                            if (currentProducto == null) {
                                Toast.makeText(this, "No hay producto escaneado para asignar a la estantería. Realiza de nuevo la operacion", Toast.LENGTH_SHORT).show();
                                estadoAsignarProductoAEstanteriaFalse();
                                return;
                            }
                            mostrarConfirmacionDialog(this::asignarProductoEscaneadoAEstanteriaEscaneada, "Seguro que quieres asignar " + currentProducto.getNombre());
                            Toast.makeText(this, "Estantería escaneada para asignar producto: " + estanteria.getNombre(), Toast.LENGTH_SHORT).show();
                        }

                        tvEstanteriaInfo.setText("Estantería: " + estanteria.getNombre());
                        tvEstanteriaInfo.setVisibility(View.VISIBLE);

                        List<Producto> productos = estanteria.getProductos();
                        adapter.setProductos(productos);

                        if (productos == null || productos.isEmpty()) {
                            Toast.makeText(this, "Estantería vacía", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Estantería no encontrada", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "ID de producto inválido: " + id, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void handleProductoScan(String id) {
        executor.execute(() -> {
            try {
                Long productoId = Long.parseLong(id);
                Producto producto = productoViewModel.getProductoById(productoId);

                runOnUiThread(() -> {
                    if (producto != null) {
                        currentProducto = producto;
                        if (isAsignarProductoAEstanteria) {
                            if (currentEstanteria == null) {
                                Toast.makeText(this, "No hay estantería escaneada para asignar el producto. Realiza de nuevo la operacion", Toast.LENGTH_SHORT).show();
                                estadoAsignarProductoAEstanteriaFalse();
                                return;
                            }
                            Toast.makeText(this, "Producto escaneado para asignar a estantería: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                            mostrarConfirmacionDialog(this::asignarProductoEscaneadoAEstanteriaEscaneada, "Seguro que quieres asignar " + producto.getNombre());
                        } else {
                            showProductoOptionsDialog(producto);
                        }
                    } else {
                        Toast.makeText(this, "Producto no encontrado, null (handleProductoScan, id: " + productoId + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "ID de producto inválido", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showProductoOptionsDialog(Producto producto) {
        String[] opciones = {"Añadir unidades", "Quitar unidades", "Asignar a estantería", "Mostrar detalles", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle(producto.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showCantidadDialog(producto, true);
                            break;
                        case 1:
                            showCantidadDialog(producto, false);
                            break;
                        case 2:
                            Toast.makeText(this, "Escanea una estanteria", Toast.LENGTH_SHORT).show();
                            currentProducto = producto;
                            estadoAsignarProductoAEstanteriaTrue();
                            mostrarMensajeCambioEstanteria();
                            break;
                        case 3:
                            String info = "ID: " + producto.getId() +
                                    "\nNombre: " + producto.getNombre() +
                                    "\nCantidad: " + producto.getCantidad() +
                                    "\nPrecio: " + producto.getPrecio() +
                                    "\nEstantería: " + (producto.getEstanteria() != null ? producto.getEstanteria().getNombre() : "Sin asignar");
                            new AlertDialog.Builder(this)
                                    .setTitle("Detalles del producto")
                                    .setMessage(info)
                                    .setPositiveButton("OK", null)
                                    .show();
                            break;
                        case 4:
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCantidadDialog(Producto producto, boolean esAgregar) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Cantidad");

        new AlertDialog.Builder(this)
                .setTitle(esAgregar ? "Añadir unidades" : "Quitar unidades")
                .setView(input)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String cantidadStr = input.getText().toString();
                    if (!cantidadStr.isEmpty()) {
                        int cantidad = Integer.parseInt(cantidadStr);

                        // Mostrar diálogo de confirmación en el hilo principal
                        mostrarConfirmacionDialog(() -> {
                            executor.execute(() -> {
                                boolean exito;
                                if (esAgregar) {
                                    exito = productoViewModel.addUndsProduct(producto, cantidad);
                                } else {
                                    exito = productoViewModel.removeUndsProduct(producto, cantidad);
                                }

                                if (exito) {
                                    Producto productoActualizado = productoViewModel.getProductoById(producto.getId());

                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Operación realizada: " +
                                                        (productoActualizado != null ? productoActualizado.getCantidad() + " uds" : ""),
                                                Toast.LENGTH_SHORT).show();
                                    });

                                    if (currentEstanteria != null) {
                                        actualizarProductosEnEstanteria();
                                    }
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(this, "Error en la operación", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            });
                        }, "¿Seguro que quieres " + (esAgregar ? "añadir" : "quitar") + " " + cantidad + " unidades a " + producto.getNombre() + "?");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void asignarProductoEscaneadoAEstanteriaEscaneada() {
        final Producto producto = currentProducto;
        final Estanteria estanteria = currentEstanteria;

        if (producto == null || estanteria == null) {
            Toast.makeText(this, "Error: producto o estantería es null", Toast.LENGTH_SHORT).show();
            estadoAsignarProductoAEstanteriaFalse();
            return;
        }

        executor.execute(() -> {
            try {
                productoViewModel.assignProductToEstanteria(producto, estanteria);
                if (currentEstanteria != null) {
                    actualizarProductosEnEstanteria();
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto asignado a " + estanteria.getNombre(), Toast.LENGTH_SHORT).show();
                    isAsignarProductoAEstanteria = false;
                    currentProducto = null;
                    currentEstanteria = null;
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al asignar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isAsignarProductoAEstanteria = false;
                });
            }
        });
    }

    private void mostrarMensajeCambioEstanteria() {
        if (isAsignarProductoAEstanteria) {
            Toast.makeText(this, "Escanea una estantería/producto para asignar.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Asignar estanteria ha cambiado de estado", Toast.LENGTH_SHORT).show();
        }
    }

    private void estadoAsignarProductoAEstanteriaTrue() {
        isAsignarProductoAEstanteria = true;
        Toast.makeText(this, "Escanea una estantería/producto para asignar.", Toast.LENGTH_SHORT).show();
    }

    private void estadoAsignarProductoAEstanteriaFalse() {
        isAsignarProductoAEstanteria = false;

        if (android.os.Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Toast.makeText(this, "Asignar estanteria cambio de estado", Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() ->
                Toast.makeText(this, "Asignar estanteria cambio de estado", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void actualizarProductosEnEstanteria() {
        if (currentEstanteria != null) {
            executor.execute(() -> {
                Estanteria estanteriaActualizada = estanteriaViewModel.getEstanteriaConProductosById(currentEstanteria.getId());
                runOnUiThread(() -> {
                    if (estanteriaActualizada != null) {
                        currentEstanteria = estanteriaActualizada;
                        adapter.setProductos(estanteriaActualizada.getProductos());
                    }
                });
            });
        }
    }

    private void mostrarConfirmacionDialog(Runnable ejecutarOperacion, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar operación")
                .setMessage(mensaje)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Acción al confirmar
                    ejecutarOperacion.run();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Acción al cancelar (opcional)
                    dialog.dismiss();
                    currentProducto = null;
                    currentEstanteria = null;
                    isAsignarProductoAEstanteria = false;
                })
                .setCancelable(false)
                .show();
    }


}
