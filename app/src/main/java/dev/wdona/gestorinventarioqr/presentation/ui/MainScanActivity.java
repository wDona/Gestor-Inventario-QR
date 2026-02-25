package dev.wdona.gestorinventarioqr.presentation.ui;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.data.api.impl.EstanteriaApiImpl;
import dev.wdona.gestorinventarioqr.data.api.impl.ProductoApiImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.OperacionLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.db.AppDatabase;
import dev.wdona.gestorinventarioqr.data.repository.EstanteriaRepositoryImpl;
import dev.wdona.gestorinventarioqr.data.repository.OperacionRepositoryImpl;
import dev.wdona.gestorinventarioqr.data.repository.ProductoRepositoryImpl;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockConfig;
import dev.wdona.gestorinventarioqr.presentation.ui.scan.ProductoScanAdapter;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.EstanteriaViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.OperacionViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.ProductoViewModel;
import dev.wdona.gestorinventarioqr.scanner.QRIdentifier;
import dev.wdona.gestorinventarioqr.scanner.ScannerManager;

public class MainScanActivity extends AppCompatActivity implements ScannerManager.ScanCallback {

    private ScannerManager scannerManager;
    private TextView tvStatus;
    private TextView tvScannedData;
    private TextView tvEstanteriaInfo;
    private Button btnScan;
    private Button btnStop;
    private Button btnVerOperaciones;
    private Button btnMoverProductoAqui;
    private Button btnToggleOffline;
    private View llBotonesEstanteria;
    private RecyclerView rvProductos;
    private ProgressDialog progressDialog;
    private EstanteriaViewModel estanteriaViewModel;
    private ProductoViewModel productoViewModel;
    private OperacionViewModel operacionViewModel;
    private Estanteria currentEstanteria;
    private Producto currentProducto;
    private boolean isAsignarProductoAEstanteria = false;
    private boolean isMoverProductoAqui = false;  // Nuevo modo: mover producto a estantería actual
    private boolean syncPendiente = false; // Para controlar sincronización al volver a online
    private ProductoScanAdapter adapter;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        executor = Executors.newSingleThreadExecutor();

        try {
            inicializarViewModels();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error inicializando: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Continuar de todas formas para ver la UI
        }

        initViews();
        initScanner();

        observarProductos();
    }

    private void inicializarViewModels() {
        App app = App.getInstance();

        if (app != null && app.estanteriaViewModel != null && app.productoViewModel != null) {
            this.estanteriaViewModel = app.estanteriaViewModel;
            this.productoViewModel = app.productoViewModel;
            this.operacionViewModel = app.operacionViewModel;
        } else {
            // Inicializar localmente si App falló
            android.util.Log.w("MainScanActivity", "App.getInstance() falló, inicializando localmente");

            AppDatabase appDatabase = AppDatabase.getDatabase(getApplicationContext());

            EstanteriaLocalDataSourceImpl estanteriaLocalDataSource = new EstanteriaLocalDataSourceImpl(appDatabase.estanteriaDao());
            ProductoLocalDataSourceImpl productoLocalDataSource = new ProductoLocalDataSourceImpl(appDatabase.productoDao(), appDatabase.estanteriaDao());

            EstanteriaApiImpl estanteriaApi = new EstanteriaApiImpl();
            ProductoApiImpl productoApi = new ProductoApiImpl();

            EstanteriaRemoteDataSourceImpl estanteriaRemoteDataSource = new EstanteriaRemoteDataSourceImpl(estanteriaApi);
            ProductoRemoteDataSourceImpl productoRemoteDataSource = new ProductoRemoteDataSourceImpl(productoApi);

            EstanteriaRepositoryImpl estanteriaRepository = new EstanteriaRepositoryImpl(estanteriaRemoteDataSource, estanteriaLocalDataSource);
            OperacionRepositoryImpl operacionRepository = new OperacionRepositoryImpl(
                    new OperacionLocalDataSourceImpl(appDatabase.operacionDao()),
                    productoRemoteDataSource,
                    estanteriaLocalDataSource,
                    productoLocalDataSource
            );
            ProductoRepositoryImpl productoRepository = new ProductoRepositoryImpl(productoRemoteDataSource, productoLocalDataSource, operacionRepository);

            this.estanteriaViewModel = new EstanteriaViewModel(estanteriaRepository);
            this.productoViewModel = new ProductoViewModel(productoRepository);
        }
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvScannedData = findViewById(R.id.tvScannedData);
        tvEstanteriaInfo = findViewById(R.id.tvEstanteriaInfo);
        btnScan = findViewById(R.id.btnScan);
        btnStop = findViewById(R.id.btnStop);
        rvProductos = findViewById(R.id.rvProductos);
        btnVerOperaciones = findViewById(R.id.btnVerOperaciones);
        btnMoverProductoAqui = findViewById(R.id.btnMoverProductoAqui);
        llBotonesEstanteria = findViewById(R.id.llBotonesEstanteria);
        btnToggleOffline = findViewById(R.id.btnToggleOffline);

        adapter = new ProductoScanAdapter(this::showProductoOptionsDialog);
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        rvProductos.setAdapter(adapter);

        btnScan.setOnClickListener(v -> startScanning());
        btnStop.setOnClickListener(v -> stopScanning());

        btnVerOperaciones.setOnClickListener(v -> {
            Intent intent = new Intent(this, OperacionesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Botón mover producto aquí
        btnMoverProductoAqui.setOnClickListener(v -> iniciarModoMoverProductoAqui());

        // Botón toggle offline/online
        btnToggleOffline.setOnClickListener(v -> {
            toggleOfflineMode();
        });

        actualizarBotonOffline();

        btnScan.setEnabled(false);
        btnStop.setEnabled(false);
    }

    private void toggleOfflineMode() {
        boolean isOffline = MockConfig.toggleOffline();
        actualizarBotonOffline();
        String mensaje = isOffline ? "Modo OFFLINE activado" : "Modo ONLINE activado";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void actualizarBotonOffline() {
        if (MockConfig.isOffline()) {
            btnToggleOffline.setText("Modo: OFFLINE");
            btnToggleOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF44336)); // Rojo
        } else {
            btnToggleOffline.setText("Modo: ONLINE");
            btnToggleOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Verde

            syncPendiente = true;
        }
    }

    private void sincronizar() {
        List<Producto> productos = productoViewModel.productosLiveData.getValue();
        if (productos != null && !productos.isEmpty()) {
            android.util.Log.d("MainScanActivity", "Sincronización iniciada con " + productos.size() + " productos.");
            productoViewModel.sincronizar(productoViewModel.productosLiveData.getValue().toArray(new Producto[0]));
        } else {
            android.util.Log.d("MainScanActivity", "No hay productos para sincronizar al cambiar a ONLINE.");
        }
    }

    private void observarProductos() {
        productoViewModel.productosLiveData.observe(this, productos -> {
            if (syncPendiente && productos != null && !productos.isEmpty()) {
                sincronizar();
                syncPendiente = false;
            }
        });
    }

    private void initScanner() {
        try {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Inicializando escáner...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            scannerManager = new ScannerManager();
            scannerManager.init(this, this);
        } catch (Exception e) {
            e.printStackTrace();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            tvStatus.setText("Error al inicializar escáner");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

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
                android.util.Log.d("MainScanActivity", "Buscando estanteria con ID: " + estanteriaId);

                Estanteria estanteria = estanteriaViewModel.getEstanteriaConProductosById(estanteriaId);

                android.util.Log.d("MainScanActivity", "Estanteria encontrada: " + (estanteria != null ? estanteria.getNombre() : "null"));
                if (estanteria != null && estanteria.getProductos() != null) {
                    android.util.Log.d("MainScanActivity", "Productos en estanteria: " + estanteria.getProductos().size());
                    for (Producto p : estanteria.getProductos()) {
                        android.util.Log.d("MainScanActivity", "  - Producto: " + p.getNombre() + " (ID: " + p.getId() + ")");
                    }
                }

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

                        // Mostrar botones de estantería
                        llBotonesEstanteria.setVisibility(View.VISIBLE);

                        List<Producto> productos = estanteria.getProductos();
                        adapter.setProductos(new ArrayList<>());
                        adapter.notifyDataSetChanged();
                        adapter.setProductos(productos);

                        if (adapter.getItemCount() == 0) { // Usar getItemCount() sigue siendo la mejor práctica
                            Toast.makeText(this, "Estantería vacía", Toast.LENGTH_SHORT).show();
                        }

                        if (productos == null || productos.isEmpty()) {
                            Toast.makeText(this, "Estantería vacía", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Estantería no encontrada: " + estanteriaId, Toast.LENGTH_SHORT).show();
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

                        // Modo: mover producto a estantería actual
                        if (isMoverProductoAqui) {
                            if (currentEstanteria == null) {
                                Toast.makeText(this, "No hay estantería seleccionada. Escanea una estantería primero.", Toast.LENGTH_SHORT).show();
                                cancelarModoMoverProductoAqui();
                                return;
                            }
                            Toast.makeText(this, "Producto escaneado: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                            mostrarConfirmacionDialog(
                                    this::moverProductoAEstanteriaActual,
                                    "¿Mover " + producto.getNombre() + " a " + currentEstanteria.getNombre() + "?"
                            );
                        }
                        // Modo: asignar producto a estantería (escanear estantería después)
                        else if (isAsignarProductoAEstanteria) {
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

    // ===== MÉTODOS PARA MODO "MOVER PRODUCTO AQUÍ" =====

    private void iniciarModoMoverProductoAqui() {
        if (currentEstanteria == null) {
            Toast.makeText(this, "Primero escanea una estantería", Toast.LENGTH_SHORT).show();
            return;
        }

        isMoverProductoAqui = true;
        tvStatus.setText("Escanea el producto a mover a " + currentEstanteria.getNombre());
        Toast.makeText(this, "Escanea el producto que quieres mover aquí", Toast.LENGTH_SHORT).show();

        // Iniciar escaneo automáticamente
        startScanning();
    }

    private void cancelarModoMoverProductoAqui() {
        isMoverProductoAqui = false;
        tvStatus.setText("Listo para escanear");
        currentProducto = null;
    }

    private void moverProductoAEstanteriaActual() {
        final Producto producto = currentProducto;
        final Estanteria estanteria = currentEstanteria;

        if (producto == null || estanteria == null) {
            Toast.makeText(this, "Error: producto o estantería es null", Toast.LENGTH_SHORT).show();
            cancelarModoMoverProductoAqui();
            return;
        }

        executor.execute(() -> {
            try {
                productoViewModel.assignProductToEstanteria(producto, estanteria);

                runOnUiThread(() -> {
                    Toast.makeText(this, producto.getNombre() + " movido a " + estanteria.getNombre(), Toast.LENGTH_SHORT).show();
                    isMoverProductoAqui = false;
                    currentProducto = null;
                    tvStatus.setText("Listo para escanear");
                });

                // Actualizar lista de productos de la estantería
                actualizarProductosEnEstanteria();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al mover producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cancelarModoMoverProductoAqui();
                });
            }
        });
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

    @Override
    protected void onDestroy() {
        // Cerrar el ProgressDialog para evitar WindowLeaked
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // Liberar el scanner
        if (scannerManager != null) {
            scannerManager.release();
        }

        // Shutdown del executor
        if (executor != null) {
            executor.shutdown();
        }

        super.onDestroy();
    }
}
