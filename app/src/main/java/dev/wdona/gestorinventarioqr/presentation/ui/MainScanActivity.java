package dev.wdona.gestorinventarioqr.presentation.ui;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
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
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockConfig;
import dev.wdona.gestorinventarioqr.presentation.ui.scan.ProductoScanAdapter;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.EstanteriaViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.OperacionViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.ProductoViewModel;
import dev.wdona.gestorinventarioqr.scanner.ScannerManager;

public class MainScanActivity extends AppCompatActivity implements ScannerManager.ScanCallback {

    private ScannerManager scannerManager;
    private TextView tvStatus;
    private TextView tvScannedData;
    private TextView tvEstanteriaInfo;
    private Button btnScanToggle;
    private Button btnVerOperaciones;
    private Button btnMoverProductoAqui;
    private Button btnToggleOffline;
    private View cardEstanteria;
    private RecyclerView rvProductos;
    private EstanteriaViewModel estanteriaViewModel;
    private ProductoViewModel productoViewModel;
    private OperacionViewModel operacionViewModel;
    private Estanteria currentEstanteria;
    private Producto currentProducto;
    private Producto productoAMover = null;
    private boolean isAsignarProductoAEstanteria = false;
    private boolean isMoverProductoAqui = false;  // Nuevo modo: mover producto a estantería actual
    private boolean isMoverCantidad = false;     // Nuevo modo: mover cantidad específica a estantería
    private int cantidadAMoverGlobal = 0;        // Cantidad específica a mover

    // Variables para flujo de creación de nuevos items
    private String pendingQrCode = null;
    private boolean isWaitingForEstanteriaToCreateProduct = false;

    private boolean syncOperacionesPendiente = false; // Para controlar sincronización al volver a online
    private boolean syncProductosPendiente = false; // Para controlar sincronización al volver a online
    private boolean syncedOperacionesPrincipio = false;
    private boolean isOffline = MockConfig.isOffline();
    private ProductoScanAdapter adapter;
    private ExecutorService executor;
    private boolean isScanning = false;

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

        observarDatos();
        observarProductos();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Siempre recargar estantería actual si existe
        if (currentEstanteria != null) {
            android.util.Log.d("MainScan", "onResume - Recargando estantería: " + currentEstanteria.getNombre());
            recargarEstanteriaCompleta();
        }
    }

    private void recargarEstanteriaCompleta() {
        if (currentEstanteria == null) return;

        String estanteriaId = currentEstanteria.getId();
        android.util.Log.d("MainScan", "Recargando estantería ID: " + estanteriaId);

        executor.execute(() -> {
            try {
                // Forzar recarga completa desde repository
                Estanteria estanteriaActualizada = estanteriaViewModel.getEstanteriaConProductosById(estanteriaId);

                runOnUiThread(() -> {
                    if (estanteriaActualizada != null) {
                        currentEstanteria = estanteriaActualizada;
                        mostrarEstanteria(estanteriaActualizada);
                        android.util.Log.d("MainScan", "Estantería recargada con " +
                                (estanteriaActualizada.getProductos() != null ? estanteriaActualizada.getProductos().size() : 0) + " productos");
                    } else {
                        android.util.Log.e("MainScan", "Error: No se pudo recargar la estantería");
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("MainScan", "Error recargando estantería: " + e.getMessage(), e);
            }
        });
    }

    private void mostrarEstanteria(Estanteria estanteria) {
        tvEstanteriaInfo.setText(estanteria.getNombre());
        cardEstanteria.setVisibility(View.VISIBLE);

        List<Producto> productos = estanteria.getProductos();
        if (productos != null && !productos.isEmpty()) {
            adapter.setProductos(productos);
            rvProductos.setVisibility(View.VISIBLE);
        } else {
            adapter.setProductos(new ArrayList<>());
            rvProductos.setVisibility(View.GONE);
        }
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

            EstanteriaLocalDataSourceImpl estanteriaLocalDataSource = new EstanteriaLocalDataSourceImpl(appDatabase.estanteriaDao(), appDatabase.productoDao());
            ProductoLocalDataSourceImpl productoLocalDataSource = new ProductoLocalDataSourceImpl(appDatabase.productoDao(), appDatabase.estanteriaDao(), appDatabase.productoEstanteriaDao());

            EstanteriaApiImpl estanteriaApi = new EstanteriaApiImpl();
            ProductoApiImpl productoApi = new ProductoApiImpl();

            EstanteriaRemoteDataSourceImpl estanteriaRemoteDataSource = new EstanteriaRemoteDataSourceImpl(estanteriaApi);
            ProductoRemoteDataSourceImpl productoRemoteDataSource = new ProductoRemoteDataSourceImpl(productoApi);

            OperacionRepositoryImpl operacionRepository = new OperacionRepositoryImpl(
                    new OperacionLocalDataSourceImpl(appDatabase.operacionDao()),
                    productoRemoteDataSource,
                    estanteriaLocalDataSource,
                    productoLocalDataSource,
                    estanteriaRemoteDataSource
            );

            EstanteriaRepositoryImpl estanteriaRepository = new EstanteriaRepositoryImpl(estanteriaRemoteDataSource, estanteriaLocalDataSource, operacionRepository);
            ProductoRepositoryImpl productoRepository = new ProductoRepositoryImpl(productoRemoteDataSource, productoLocalDataSource, operacionRepository);

            this.estanteriaViewModel = new EstanteriaViewModel(estanteriaRepository);
            this.productoViewModel = new ProductoViewModel(productoRepository);
            this.operacionViewModel = new OperacionViewModel(operacionRepository);
        }
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvScannedData = findViewById(R.id.tvScannedData);
        tvEstanteriaInfo = findViewById(R.id.tvEstanteriaInfo);
        btnScanToggle = findViewById(R.id.btnScanToggle);
        rvProductos = findViewById(R.id.rvProductos);
        btnVerOperaciones = findViewById(R.id.btnVerOperaciones);
        btnMoverProductoAqui = findViewById(R.id.btnMoverProductoAqui);
        cardEstanteria = findViewById(R.id.cardEstanteria);
        btnToggleOffline = findViewById(R.id.btnToggleOffline);
        ImageButton btnEliminarEstanteria = findViewById(R.id.btnEliminarEstanteria);

        adapter = new ProductoScanAdapter(new ProductoScanAdapter.OnProductoInteractionListener() {
            @Override
            public void onProductoClick(Producto producto) {
                showProductoOptionsDialog(producto);
            }

            @Override
            public void onAddStock(Producto producto) {
                if (producto.getEstanteria() == null && currentEstanteria != null) {
                    producto.setEstanteria(currentEstanteria);
                }
                modificarStockProducto(producto, 1);
            }

            @Override
            public void onRemoveStock(Producto producto) {
                if (producto.getEstanteria() == null && currentEstanteria != null) {
                    producto.setEstanteria(currentEstanteria);
                }
                modificarStockProducto(producto, -1);
            }
        });
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        rvProductos.setAdapter(adapter);

        btnScanToggle.setOnClickListener(v -> toggleScanning());

        btnVerOperaciones.setOnClickListener(v -> {
            Intent intent = new Intent(this, OperacionesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Botón mover producto aquí
        btnMoverProductoAqui.setOnClickListener(v -> iniciarModoMoverProductoAqui());

        // Botón eliminar estantería
        if (btnEliminarEstanteria != null) {
            btnEliminarEstanteria.setOnClickListener(v -> confirmarEliminarEstanteria());
        }

        // Botón toggle offline/online
//        btnToggleOffline.setOnClickListener(v -> {
//            toggleOfflineMode();
//        });

        actualizarBotonOffline();

        btnScanToggle.setEnabled(false);
    }

    private void toggleOfflineMode() {
        isOffline = MockConfig.toggleOffline();
        actualizarBotonOffline();
        String mensaje = isOffline ? "Modo OFFLINE activado" : "Modo ONLINE activado";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void actualizarBotonOffline() {
        if (MockConfig.isOffline()) {
            btnToggleOffline.setText("");  // Círculo vacío para offline
            btnToggleOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.status_offline)));
        } else {
            btnToggleOffline.setText("");  // Círculo lleno para online
            btnToggleOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.status_online)));

            syncOperacionesPendiente = true;
            syncProductosPendiente = true;

            // Al cambiar a online, ejecutar sincronización automática
            ejecutarSincronizacionCompleta();
        }

        refrescarEstanteriaActual();
    }

    private void ejecutarSincronizacionCompleta() {
        Toast.makeText(this, "Sincronizando datos...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                reintentarOperacionesPendientes();
                sincronizarProductos();

                if (currentEstanteria != null) {
                    recargarEstanteriaCompleta();
                }

                runOnUiThread(() -> {
                    Toast.makeText(MainScanActivity.this, "Sincronización completada", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                android.util.Log.e("MainScan", "Error en sincronización automática: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainScanActivity.this, "Error en sincronización", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void reintentarOperacionesPendientes() {
        android.util.Log.d("MainScan", "Reintentando todas las operaciones pendientes...");
        operacionViewModel.reintentarEnvioAllOperaciones();
    }

    private void sincronizarProductos() {
        List<Producto> productos = productoViewModel.productosLiveData.getValue();
        if (productos != null && !productos.isEmpty()) {
            android.util.Log.d("MainScanActivity", "Sincronización iniciada con " + productos.size() + " productos.");
            productoViewModel.sincronizar(productoViewModel.productosLiveData.getValue().toArray(new Producto[0]));
        } else {
            android.util.Log.d("MainScanActivity", "No hay productos para sincronizar al cambiar a ONLINE.");
        }
    }

    private void observarDatos() {
        MediatorLiveData<Boolean> syncMediator = new MediatorLiveData<>();

        syncMediator.addSource(productoViewModel.productosLiveData, productos -> {
            isListo(syncMediator, productos, operacionViewModel.operacionLiveData.getValue());
        });

        syncMediator.addSource(operacionViewModel.operacionLiveData, operaciones -> {
            isListo(syncMediator, productoViewModel.productosLiveData.getValue(), operaciones);
        });

        syncMediator.observe(this, listo -> {
            if (!listo) return;
            if (!syncedOperacionesPrincipio && !MockConfig.isOffline()) {
                syncedOperacionesPrincipio = true;
                operacionViewModel.reintentarEnvioAllOperaciones();
                sincronizarProductos();
            }

            if ((syncOperacionesPendiente || syncProductosPendiente)) {
                if (syncOperacionesPendiente) {
                    operacionViewModel.reintentarEnvioAllOperaciones();
                    syncOperacionesPendiente = false;
                }
                if (syncProductosPendiente) {
                    sincronizarProductos();
                    syncProductosPendiente = false;
                }
            }


        });
    }

    private void isListo(MediatorLiveData<Boolean> syncMediator, List<Producto> productos, List<Operacion> operaciones) {
        if (productos != null && !productos.isEmpty() && operaciones != null && !operaciones.isEmpty() && !syncedOperacionesPrincipio) {
            syncMediator.setValue(true);
        } else if ((productos == null || productos.isEmpty()) && (operaciones == null || operaciones.isEmpty())) {
            syncMediator.setValue(false);
        } else if (productos != null && !productos.isEmpty() && operaciones != null && !operaciones.isEmpty() && (syncProductosPendiente || syncOperacionesPendiente)){
            syncMediator.setValue(true);
        } else if (productos != null && !productos.isEmpty() && operaciones != null && !operaciones.isEmpty() && isOffline) {
            syncMediator.setValue(true);
        }
    }

    private void refrescarEstanteriaActual() {
        if (currentEstanteria != null) {
            handleEstanteriaScan(currentEstanteria.getId());
        }
    }

    private void observarProductos() {
        productoViewModel.productosLiveData.observe(this, productos -> {
            if (syncProductosPendiente && productos != null && !productos.isEmpty()) {
                sincronizarProductos();
                syncProductosPendiente = false;
            }
        });
    }


    private void initScanner() {
        try {
            tvStatus.setText("Inicializando escáner...");

            scannerManager = new ScannerManager();
            scannerManager.init(this, this);
        } catch (Exception e) {
            e.printStackTrace();
            tvStatus.setText("Error al inicializar escáner");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startScanning() {
        scannerManager.startScan();
        isScanning = true;
        updateScanButtonUI();
        btnMoverProductoAqui.setEnabled(false); // Deshabilitar otras acciones mientras escanea
    }

    private void stopScanning() {
        scannerManager.stopScan();
        isScanning = false;
        updateScanButtonUI();
        btnMoverProductoAqui.setEnabled(true);
    }

    private void toggleScanning() {
        if (isScanning) {
            stopScanning();
        } else {
            startScanning();
        }
    }

    private void updateScanButtonUI() {
        if (isScanning) {
            tvStatus.setText("Escaneando...");
            btnScanToggle.setText("■ Detener");
            btnScanToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_offline)));
        } else {
            tvStatus.setText("Listo para escanear");
            btnScanToggle.setText("▶ Escanear");
            btnScanToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_online)));
        }
    }

    @Override
    public void onScanResult(String data) {
        String trimmedData = data != null ? data.trim() : "";
        
        // Detener siempre el escaneo al recibir resultado (sea por botón o hardware)
        stopScanning();
        
        if (trimmedData.isEmpty()) {
            tvScannedData.setText("No se ha encontrado ningún código");
            tvScannedData.setVisibility(View.VISIBLE);
            return;
        }
        tvScannedData.setText("Escaneado: " + trimmedData);
        tvScannedData.setVisibility(View.VISIBLE);
        processScanResult(trimmedData);
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

        if (success) {
            tvStatus.setText("Escáner listo");
            btnScanToggle.setEnabled(true);
            updateScanButtonUI();
        } else {
            tvStatus.setText("Error al inicializar");
            Toast.makeText(this, "No se pudo inicializar el escáner", Toast.LENGTH_LONG).show();
        }

    }

    private void processScanResult(String data) {
        // Consultar el data en la base de datos para determinar si es estantería o producto
        executor.execute(() -> {
            Estanteria estanteria = estanteriaViewModel.getEstanteriaById(data);
            if (estanteria != null) {
                runOnUiThread(() -> handleEstanteriaScan(data));
                return;
            }

            Producto producto = productoViewModel.getProductoById(data);
            if (producto != null) {
                runOnUiThread(() -> handleProductoScan(data));
                return;
            }

            runOnUiThread(() -> mostrarDialogoCrearNuevoItem(data));
        });
    }

    private void mostrarDialogoCrearNuevoItem(String qrCode) {
        new AlertDialog.Builder(this)
                .setTitle("Código no reconocido: " + qrCode)
                .setMessage("¿Qué desea crear con este código?")
                .setPositiveButton("Crear Producto", (dialog, which) -> {
                    iniciarCreacionProducto(qrCode);
                })
                .setNegativeButton("Crear Estantería", (dialog, which) -> {
                    iniciarCreacionEstanteria(qrCode);
                })
                .setNeutralButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void iniciarCreacionProducto(String qrCode) {
        // Si ya estamos en contexto de una estantería (ej: Mover Producto Aquí, o simplemente viendo una)
        // Solo usamos la estantería actual automáticamente si estamos en modo "Mover Producto Aquí"
        if (currentEstanteria != null && isMoverProductoAqui) {
            mostrarFormularioNuevoProducto(qrCode, currentEstanteria);
        } else {
            pendingQrCode = qrCode;
            isWaitingForEstanteriaToCreateProduct = true;
            
            Toast.makeText(this, "Escanea la estantería donde se ubicará el producto", Toast.LENGTH_LONG).show();
            tvStatus.setText("Escanea estantería para nuevo producto");
        }
    }

    private void iniciarCreacionEstanteria(String qrCode) {
        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre de la estantería");

        new AlertDialog.Builder(this)
                .setTitle("Nueva Estantería")
                .setMessage("Código: " + qrCode)
                .setView(inputNombre)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    crearEstanteria(qrCode, nombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearEstanteria(String id, String nombre) {
        executor.execute(() -> {
            try {
                Estanteria nuevaEstanteria = new Estanteria(id, nombre);
                // Usamos createEstanteria para registrar operación si falla remote
                estanteriaViewModel.createEstanteria(nuevaEstanteria);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Estantería creada exitosamente", Toast.LENGTH_SHORT).show();
                    // Opcional: Cargarla inmediatamente
                    handleEstanteriaScan(id);
                });
            } catch (Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error al crear estantería: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void confirmarEliminarEstanteria() {
        if (currentEstanteria == null) {
            Toast.makeText(this, "No hay estantería seleccionada para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentEstanteria.getProductos() != null && !currentEstanteria.getProductos().isEmpty()) {
            Toast.makeText(this, "No se puede eliminar: La estantería tiene productos.", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Estantería")
                .setMessage("¿Estás seguro de que deseas eliminar la estantería '" + currentEstanteria.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEstanteria(currentEstanteria))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEstanteria(Estanteria estanteria) {
        executor.execute(() -> {
            try {
                estanteriaViewModel.deleteEstanteria(estanteria.getId());
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Estantería eliminada exitosamente", Toast.LENGTH_SHORT).show();
                    currentEstanteria = null;
                    tvEstanteriaInfo.setText("Escanea un código QR");
                    cardEstanteria.setVisibility(View.GONE);
                    adapter.setProductos(new ArrayList<>());
                    rvProductos.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error al eliminar estantería: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void mostrarFormularioNuevoProducto(String productoId, Estanteria estanteria) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del producto");
        layout.addView(inputNombre);

        final EditText inputPrecio = new EditText(this);
        inputPrecio.setHint("Precio (ej: 10.5)");
        inputPrecio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputPrecio);

        final EditText inputCantidad = new EditText(this);
        inputCantidad.setHint("Cantidad inicial");
        inputCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputCantidad);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Producto")
                .setMessage("Código: " + productoId + "\nUbicación: " + estanteria.getNombre())
                .setView(layout)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    String precioStr = inputPrecio.getText().toString().trim();
                    String cantidadStr = inputCantidad.getText().toString().trim();

                    if (nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double precio = Double.parseDouble(precioStr);
                        int cantidad = Integer.parseInt(cantidadStr);

                        if (cantidad < 0) {
                            Toast.makeText(this, "La cantidad no puede ser negativa", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        crearProducto(productoId, nombre, precio, cantidad, estanteria);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Formato numérico inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearProducto(String id, String nombre, double precio, int cantidad, Estanteria estanteria) {
        executor.execute(() -> {
            try {
                // 1. Crear el producto base
                Producto nuevoProducto = new Producto(id, nombre, precio, 0, null);
                
                // Guardamos metadatos del producto con createProducto para registrar operación
                productoViewModel.createProducto(nuevoProducto);

                // 2. Asignar a la estantería con la cantidad inicial
                // Creamos un objeto con la cantidad para enviarlo a 'assignProductToEstanteria' o usamos 'addUnds' si ya asignamos la estantería.
                // Sin embargo, assignProductToEstanteria suele tomar la cantidad del objeto producto.
                
                // Mejor aproximación con los métodos existentes:
                nuevoProducto.setCantidad(cantidad);
                productoViewModel.assignProductToEstanteria(nuevoProducto, estanteria);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto creado exitosamente", Toast.LENGTH_SHORT).show();
                    // Cargar la estantería para ver el nuevo producto
                     handleEstanteriaScan(estanteria.getId());
                });

            } catch (Exception e) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error al crear producto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void handleEstanteriaScan(String id) {
        executor.execute(() -> {
            try {
                // Interceptamos si estamos esperando estantería para crear producto
                if (isWaitingForEstanteriaToCreateProduct && pendingQrCode != null) {
                   Estanteria estanteria = estanteriaViewModel.getEstanteriaById(id);
                   if (estanteria != null) {
                       runOnUiThread(() -> {
                           isWaitingForEstanteriaToCreateProduct = false; // Reset flag
                           mostrarFormularioNuevoProducto(pendingQrCode, estanteria);
                           pendingQrCode = null; 
                       });
                       return;
                   } else {
                       runOnUiThread(() -> Toast.makeText(this, "Estantería no válida, intenta de nuevo", Toast.LENGTH_SHORT).show());
                       // No reseteamos flag, permitimos reintentar
                       return;
                   }
                }

                String estanteriaId = id;
                android.util.Log.d("MainScanActivity", "Buscando estanteria con ID: " + estanteriaId);

                Estanteria estanteria = estanteriaViewModel.getEstanteriaConProductosById(estanteriaId);

                loggearEstanteria(estanteria);

                runOnUiThread(() -> {
                    if (estanteria != null) {
                        
                        // Modo: mover cantidad específica a estantería escaneada
                        if (isMoverCantidad) {
                            // Usamos el producto guardado
                            Producto prodTarget = (productoAMover != null) ? productoAMover : currentProducto;
                            
                            if (prodTarget == null || cantidadAMoverGlobal <= 0) {
                                Toast.makeText(this, "Error en datos de movimiento. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                                estadoMoverCantidadFalse();
                                return;
                            }

                            // Guardamos referencia final para lambda
                            Estanteria estanteriaDestino = estanteria;

                            mostrarConfirmacionDialog(
                                    () -> {
                                        // AL CONFIRMAR: Actualizamos UI y estado
                                        currentEstanteria = estanteriaDestino;
                                        mostrarEstanteria(estanteriaDestino);
                                        ejecutarMovimientoCantidad();
                                    },
                                    "¿Mover " + cantidadAMoverGlobal + " unidades de " + prodTarget.getNombre() + " a " + estanteria.getNombre() + "?"
                            );
                            // IMPORTANTE: Retornamos aquí para NO actualizar la UI con la estantería escaneada si el usuario cancela
                            return;
                        }

                        if (isAsignarProductoAEstanteria) {
                            if (currentProducto == null) {
                                Toast.makeText(this, "No hay producto escaneado para asignar a la estantería. Realiza de nuevo la operacion", Toast.LENGTH_SHORT).show();
                                estadoAsignarProductoAEstanteriaFalse();
                                return;
                            }

                            mostrarConfirmacionDialog(this::asignarProductoEscaneadoAEstanteriaEscaneada, "Seguro que quieres asignar " + currentProducto.getNombre());
                            Toast.makeText(this, "Estantería escaneada para asignar producto: " + estanteria.getNombre(), Toast.LENGTH_SHORT).show();
                        }

                        // Flujo normal: actualizamos vista
                        currentEstanteria = estanteria;
                        mostrarEstanteria(estanteria);

                        if (estanteria.getProductos() == null || estanteria.getProductos().isEmpty()) {
                            Toast.makeText(this, "Estantería vacía", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Estantería no encontrada: " + estanteriaId, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "ID de producto inválido: " + id, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private static void loggearEstanteria(Estanteria estanteria) {
        android.util.Log.d("MainScanActivity", "Estanteria encontrada: " + (estanteria != null ? estanteria.getNombre() : "null"));
        if (estanteria != null && estanteria.getProductos() != null) {
            android.util.Log.d("MainScanActivity", "Productos en estanteria: " + estanteria.getProductos().size());
            for (Producto p : estanteria.getProductos()) {
                android.util.Log.d("MainScanActivity", "  - Producto: " + p.getNombre() + " (ID: " + p.getId() + ")");
            }
        }
    }

    private void handleProductoScan(String id) {
        executor.execute(() -> {
            try {
                String productoId = id;
                Producto producto = productoViewModel.getProductoById(productoId);

                // Si estamos en modo mover aquí, obtener ubicaciones reales
                final List<Producto> ubicaciones;
                if (isMoverProductoAqui && producto != null) {
                    ubicaciones = productoViewModel.getUbicacionesProducto(productoId);
                } else {
                    ubicaciones = null;
                }

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
                            showDialogCantidadMoverAqui(producto, ubicaciones);
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
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al procesar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void modificarStockProducto(Producto producto, int cantidad) {
        if (producto == null) return;
        
        executor.execute(() -> {
            try {
                boolean success = false;
                
                // Asegurarse de tener la referencia a la estantería actual si no existe
                if (producto.getEstanteria() == null && currentEstanteria != null) {
                    producto.setEstanteria(currentEstanteria);
                }

                if (cantidad > 0) {
                    success = productoViewModel.addUndsProduct(producto, cantidad);
                } else if (cantidad < 0) {
                    success = productoViewModel.removeUndsProduct(producto, Math.abs(cantidad));
                }

                if (success) {
                    runOnUiThread(() -> {
                        refrescarEstanteriaActual();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No se pudo actualizar el stock. Verifica disponibilidad o conexión.", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al actualizar stock: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        });
    }

    private void showProductoOptionsDialog(Producto producto) {
        String[] opciones = {"Abrir estantería", "Añadir unidades", "Quitar unidades", "Mover cantidad a estantería", "Mostrar detalles", "Eliminar producto"};

        new AlertDialog.Builder(this)
                .setTitle(producto.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            abrirEstanteriaDeProducto(producto);
                            break;
                        case 1:
                            showCantidadDialog(producto, true);
                            break;
                        case 2:
                            showCantidadDialog(producto, false);
                            break;
                        case 3:
                            showMoverCantidadDialog(producto);
                            break;
                        case 4:
                            // Ejecutar en segundo plano para obtener todas las ubicaciones
                            executor.execute(() -> {
                                List<Producto> ubicaciones = productoViewModel.getUbicacionesProducto(producto.getId());

                                StringBuilder sb = new StringBuilder();
                                sb.append("ID: ").append(producto.getId()).append("\n");
                                sb.append("Nombre: ").append(producto.getNombre()).append("\n");
                                sb.append("Precio: ").append(producto.getPrecio()).append("\n\n");

                                if (ubicaciones != null && !ubicaciones.isEmpty()) {
                                    sb.append("Ubicaciones:\n");
                                    int total = 0;
                                    for (Producto ubi : ubicaciones) {
                                        if (ubi.getEstanteria() != null) {
                                            sb.append("• ").append(ubi.getEstanteria().getNombre())
                                                    .append(": ").append(ubi.getCantidad()).append(" uds\n");
                                            total += ubi.getCantidad();
                                        }
                                    }
                                    sb.append("\nTotal: ").append(total).append(" uds");
                                } else {
                                    sb.append("Cantidad actual: ").append(producto.getCantidad());
                                    sb.append("\n(Sin ubicación asignada)");
                                }

                                runOnUiThread(() -> {
                                    new AlertDialog.Builder(MainScanActivity.this)
                                            .setTitle("Detalles del producto")
                                            .setMessage(sb.toString())
                                            .setPositiveButton("OK", null)
                                            .show();
                                });
                            });
                            break;
                        case 5:
                            confirmarEliminarProducto(producto);
                            break;
                        case 6:
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirEstanteriaDeProducto(Producto producto) {
        executor.execute(() -> {
            try {
                List<Producto> ubicaciones = productoViewModel.getUbicacionesProducto(producto.getId());
                
                List<Estanteria> estanteriasConProducto = new ArrayList<>();
                // Filtrar ubicaciones válidas
                if (ubicaciones != null) {
                    for (Producto ubi : ubicaciones) {
                        if (ubi.getEstanteria() != null) {
                            // Añadimos la estantería pero mantenemos referencia a cantidad si quisiéramos mostrarla
                            // Aquí guardamos la estantería asociada a esa ubicación
                            Estanteria est = ubi.getEstanteria();
                            // Hack: Guardar cantidad en un campo transient o simplemente usar el objeto ubi
                            estanteriasConProducto.add(est);
                        }
                    }
                }

                runOnUiThread(() -> {
                    if (estanteriasConProducto.isEmpty()) {
                        Toast.makeText(this, "El producto no está asignado a ninguna estantería", Toast.LENGTH_SHORT).show();
                    } else if (estanteriasConProducto.size() == 1) {
                        // Solo una, abrir directo
                        Estanteria target = estanteriasConProducto.get(0);
                        Toast.makeText(this, "Abriendo estantería: " + target.getNombre(), Toast.LENGTH_SHORT).show();
                        handleEstanteriaScan(target.getId());
                    } else {
                        // Múltiples, elegir
                        String[] nombresEstanterias = new String[estanteriasConProducto.size()];
                        for (int i = 0; i < estanteriasConProducto.size(); i++) {
                            // Sería ideal mostrar la cantidad en cada una, pero requeriría mapear mejor los datos
                            // Como getUbicaciones retorna Productos con getEstanteria(), podemos recuperar la cantidad desde 'ubicaciones'
                            // Re-hacemos el loop para ser precisos con el índice
                             Producto ubi = ubicaciones.get(i); // Ojo, ubicaciones podría tener nulos, mejor iterar parejo
                        }
                        
                        // Re-construcción segura para el diálogo
                        List<Producto> ubicacionesValidas = new ArrayList<>();
                        for (Producto ubi : ubicaciones) {
                            if (ubi.getEstanteria() != null) ubicacionesValidas.add(ubi);
                        }

                        String[] items = new String[ubicacionesValidas.size()];
                        for (int i = 0; i < ubicacionesValidas.size(); i++) {
                            Producto ubi = ubicacionesValidas.get(i);
                            items[i] = ubi.getEstanteria().getNombre() + " (" + ubi.getCantidad() + " uds)";
                        }

                        new AlertDialog.Builder(this)
                                .setTitle("Selecciona estantería")
                                .setItems(items, (dialog, which) -> {
                                    Estanteria selected = ubicacionesValidas.get(which).getEstanteria();
                                    handleEstanteriaScan(selected.getId());
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al buscar ubicaciones: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmarEliminarProducto(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de eliminar '" + producto.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto(producto))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto(Producto producto) {
        executor.execute(() -> {
            try {
                productoViewModel.deleteProducto(producto.getId());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    currentProducto = null;
                    if (currentEstanteria != null) {
                        handleEstanteriaScan(currentEstanteria.getId());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> 
                        Toast.makeText(this, "Error al eliminar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showCantidadDialog(Producto producto, boolean esAgregar) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Cantidad");

        // Si el producto no tiene estantería, pero estamos en una, se la asignamos visualmente para el diálogo
        // Pero el objeto producto sigue siendo el mismo en memoria hasta que lo modifiquemos
        String titulo = esAgregar ? "Añadir unidades" : "Quitar unidades";
        Estanteria estanteriaTarget = producto.getEstanteria();
        if (estanteriaTarget == null && currentEstanteria != null) {
            estanteriaTarget = currentEstanteria;
            titulo += " en " + estanteriaTarget.getNombre();
        } else if (estanteriaTarget != null) {
             titulo += " en " + estanteriaTarget.getNombre();
        } else {
             titulo += " (Sin ubicación asignada)";
        }

        final Estanteria estanteriaFinal = estanteriaTarget;

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setView(input)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String cantidadStr = input.getText().toString();
                    if (!cantidadStr.isEmpty()) {
                        int cantidad = Integer.parseInt(cantidadStr);

                        if (esAgregar && estanteriaFinal == null) {
                             Toast.makeText(this, "Error: Debes escanear una estantería primero o usar 'Mover a estantería' para asignar existencias.", Toast.LENGTH_LONG).show();
                             return;
                        }

                        // Mostrar diálogo de confirmación en el hilo principal
                        mostrarConfirmacionDialog(() -> {
                            executor.execute(() -> {
                                boolean exito;

                                try {
                                    // Si no tenía estantería en el objeto original pero hemos determinado una (la actual)
                                    // Debemos pasar un objeto producto con esa estantería setada
                                    Producto productoParaOperacion = producto;
                                    if (producto.getEstanteria() == null && estanteriaFinal != null) {
                                         // Crear una copia o modificar (modificar es seguro aquí pq es el producto en memoria de UI)
                                         producto.setEstanteria(estanteriaFinal);
                                    }

                                    if (esAgregar) {
                                        exito = productoViewModel.addUndsProduct(productoParaOperacion, cantidad);
                                    } else {
                                        exito = productoViewModel.removeUndsProduct(productoParaOperacion, cantidad);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("MainScanActivity", "Error al modificar unidades: " + e.getMessage());
                                    exito = false;
                                }

                                if (exito) {
                                    // Recargar producto
                                    Producto productoActualizado = productoViewModel.getProductoById(producto.getId());

                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Operación realizada: " +
                                                        (productoActualizado != null ? productoActualizado.getCantidad() + " uds" : ""),
                                                Toast.LENGTH_SHORT).show();
                                    });

                                    // Si estamos viendo una estantería, actualizarla
                                    if (currentEstanteria != null) {
                                        actualizarProductosEnEstanteria();
                                    }
                                } else {
                                    runOnUiThread(() ->
                                            Toast.makeText(this, "Error en la operación. Asegúrate de estar en una estantería válida.", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            });
                        }, "¿Seguro que quieres " + (esAgregar ? "añadir" : "quitar") + " " + cantidad + " unidades a " + producto.getNombre() + (estanteriaFinal!=null ? " en " + estanteriaFinal.getNombre() : "") + "?");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarMovimientoCantidad() {
        // Usar la variable dedicada productoAMover, fallback a currentProducto por seguridad
        final Producto producto = (productoAMover != null) ? productoAMover : currentProducto;
        final Estanteria estanteriaDestino = currentEstanteria;
        final int cantidad = cantidadAMoverGlobal;

        if (producto == null || estanteriaDestino == null || cantidad <= 0) {
            Toast.makeText(this, "Error en datos de movimiento", Toast.LENGTH_SHORT).show();
            estadoMoverCantidadFalse();
            return;
        }

        // Necesitamos la estantería origen del producto
        final Estanteria estanteriaOrigen = producto.getEstanteria();
        String estanteriaOrigenId = estanteriaOrigen != null ? estanteriaOrigen.getId() : null;

        // Si no tiene estantería de origen, se asume que es una asignación inicial (o movimiento desde "sin ubicación")
        // No bloqueamos, permitimos mover con origen null.

        executor.execute(() -> {
            try {
                productoViewModel.moverCantidad(
                        producto.getId(),
                        estanteriaOrigenId,
                        estanteriaDestino.getId(),
                        cantidad
                );

                runOnUiThread(() -> {
                    Toast.makeText(this, cantidad + " unidades de " + producto.getNombre() + " movidas a " + estanteriaDestino.getNombre(), Toast.LENGTH_SHORT).show();
                    estadoMoverCantidadFalse();
                    actualizarProductosEnEstanteria();
                });

            } catch (Exception e) {
                android.util.Log.e("MainScan", "Error en movimiento de cantidad: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al mover cantidad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    estadoMoverCantidadFalse();
                });
            }
        });
    }

    private void asignarProductoEscaneadoAEstanteriaEscaneada() {
        final Producto producto = currentProducto;
        final Estanteria estanteriaDestino = currentEstanteria;

        if (producto == null || estanteriaDestino == null) {
            Toast.makeText(this, "Error: producto o estantería es null", Toast.LENGTH_SHORT).show();
            estadoAsignarProductoAEstanteriaFalse();
            return;
        }

        final Estanteria estanteriaOrigen = producto.getEstanteria();
        
        // Si tiene origen y es el mismo que destino, no hacer nada
        if (estanteriaOrigen != null && estanteriaOrigen.getId().equals(estanteriaDestino.getId())) {
            Toast.makeText(this, "El producto ya está en esta estantería", Toast.LENGTH_SHORT).show();
            estadoAsignarProductoAEstanteriaFalse();
            return;
        }
        
        String estanteriaOrigenId = estanteriaOrigen != null ? estanteriaOrigen.getId() : null;
        final int cantidadAMover = producto.getCantidad();
        
        // Permitimos asignar (mover) aunque no tenga origen

        executor.execute(() -> {
            try {
                productoViewModel.moverCantidad(
                        producto.getId(),
                        estanteriaOrigenId,
                        estanteriaDestino.getId(),
                        cantidadAMover
                );
                if (currentEstanteria != null) {
                    actualizarProductosEnEstanteria();
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, cantidadAMover + " uds de " + producto.getNombre() + " movidas a " + estanteriaDestino.getNombre(), Toast.LENGTH_SHORT).show();
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

    private void estadoMoverCantidadTrue() {
        isMoverCantidad = true;
        // Priorizar productoAMover para mostrar el nombre correcto
        Producto p = (productoAMover != null) ? productoAMover : currentProducto;
        String nombre = (p != null) ? p.getNombre() : "Producto";
        tvStatus.setText("Escanea estantería destino para mover " + cantidadAMoverGlobal + " unidades de " + nombre);
    }

    private void estadoMoverCantidadFalse() {
        isMoverCantidad = false;
        cantidadAMoverGlobal = 0;
        productoAMover = null;
        tvStatus.setText("Listo para escanear");
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

    private void showDialogCantidadMoverAqui(Producto producto, List<Producto> ubicaciones) {
        final Estanteria estanteriaDestino = currentEstanteria;

        // Si no está en ninguna estantería, verificar si tiene cantidad global "flotante"
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            if (producto.getCantidad() > 0) {
                // Caso especial: Producto con cantidad pero sin ubicación (recién creado o flotante)
                // Usamos el producto global como "origen" (su estantería será null)
                showInputCantidadMoverAqui(producto, estanteriaDestino);
                return;
            } else {
                Toast.makeText(this, "El producto no está en ninguna estantería y no tiene stock", Toast.LENGTH_SHORT).show();
                cancelarModoMoverProductoAqui();
                return;
            }
        }

        // Filtrar ubicaciones: quitar la estantería destino (no tiene sentido mover de A a A)
        List<Producto> origenesDisponibles = new ArrayList<>();
        for (Producto ub : ubicaciones) {
            if (ub.getEstanteria() != null && !ub.getEstanteria().getId().equals(estanteriaDestino.getId())) {
                origenesDisponibles.add(ub);
            }
        }

        if (origenesDisponibles.isEmpty()) {
            Toast.makeText(this, "El producto ya está completamente en esta estantería", Toast.LENGTH_SHORT).show();
            cancelarModoMoverProductoAqui();
            return;
        }

        // Si hay un solo origen, ir directo al diálogo de cantidad
        if (origenesDisponibles.size() == 1) {
            showInputCantidadMoverAqui(origenesDisponibles.get(0), estanteriaDestino);
        } else {
            // Múltiples orígenes: dejar elegir de cuál estantería mover
            String[] opciones = new String[origenesDisponibles.size()];
            for (int i = 0; i < origenesDisponibles.size(); i++) {
                Producto ub = origenesDisponibles.get(i);
                opciones[i] = ub.getEstanteria().getNombre() + " (" + ub.getCantidad() + " uds)";
            }

            new AlertDialog.Builder(this)
                    .setTitle("¿De qué estantería mover " + producto.getNombre() + "?")
                    .setItems(opciones, (dialog, which) -> {
                        showInputCantidadMoverAqui(origenesDisponibles.get(which), estanteriaDestino);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> cancelarModoMoverProductoAqui())
                    .setCancelable(false)
                    .show();
        }
    }

    private void showInputCantidadMoverAqui(Producto productoEnOrigen, Estanteria estanteriaDestino) {
        int cantidadDisponible = productoEnOrigen.getCantidad();
        Estanteria estanteriaOrigen = productoEnOrigen.getEstanteria();
        
        String nombreOrigen = estanteriaOrigen != null ? estanteriaOrigen.getNombre() : "Sin asignar / Inventario General";

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Cantidad (máx: " + cantidadDisponible + ")");

        new AlertDialog.Builder(this)
                .setTitle("Mover " + productoEnOrigen.getNombre())
                .setMessage("De: " + nombreOrigen + " (" + cantidadDisponible + " uds)\nA: " + estanteriaDestino.getNombre())
                .setView(input)
                .setPositiveButton("Mover", (dialog, which) -> {
                    String cantidadStr = input.getText().toString().trim();
                    if (cantidadStr.isEmpty()) {
                        Toast.makeText(this, "Ingresa una cantidad", Toast.LENGTH_SHORT).show();
                        cancelarModoMoverProductoAqui();
                        return;
                    }

                    try {
                        int cantidad = Integer.parseInt(cantidadStr);

                        if (cantidad <= 0) {
                            Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                            cancelarModoMoverProductoAqui();
                            return;
                        }

                        if (cantidad > cantidadDisponible) {
                            Toast.makeText(this, "Solo hay " + cantidadDisponible + " uds en " + nombreOrigen, Toast.LENGTH_SHORT).show();
                            cancelarModoMoverProductoAqui();
                            return;
                        }

                        mostrarConfirmacionDialog(() -> {
                            executor.execute(() -> {
                                try {
                                    productoViewModel.moverCantidad(
                                            productoEnOrigen.getId(),
                                            estanteriaOrigen != null ? estanteriaOrigen.getId() : null,
                                            estanteriaDestino.getId(),
                                            cantidad
                                    );

                                    runOnUiThread(() -> {
                                        Toast.makeText(this, cantidad + " uds de " + productoEnOrigen.getNombre() + " movidas a " + estanteriaDestino.getNombre(), Toast.LENGTH_SHORT).show();
                                        cancelarModoMoverProductoAqui();
                                    });

                                    actualizarProductosEnEstanteria();

                                } catch (Exception e) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Error al mover: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        cancelarModoMoverProductoAqui();
                                    });
                                }
                            });
                        }, "¿Mover " + cantidad + " uds de " + productoEnOrigen.getNombre() + "\nde " + nombreOrigen + "\na " + estanteriaDestino.getNombre() + "?");

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        cancelarModoMoverProductoAqui();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> cancelarModoMoverProductoAqui())
                .setCancelable(false)
                .show();
    }

    private void actualizarProductosEnEstanteria() {
        if (currentEstanteria != null) {
            executor.execute(() -> {
                Estanteria estanteriaActualizada = estanteriaViewModel.getEstanteriaConProductosById(currentEstanteria.getId());
                runOnUiThread(() -> {
                    if (estanteriaActualizada != null) {
                        currentEstanteria = estanteriaActualizada;
                        mostrarEstanteria(estanteriaActualizada);
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
                    // Acción al cancelar - resetear los estados de operación pero mantener contexto
                    dialog.dismiss();
                    // NO reseteamos currentEstanteria ni currentProducto globalmente 
                    // para no perder el contexto visual si el usuario simplemente se arrepiente.
                    // Solo limpiamos los flags de modo.
                    isAsignarProductoAEstanteria = false;
                    estadoMoverCantidadFalse();
                    cancelarModoMoverProductoAqui();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {

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

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        // Códigos comunes de botones de escaneo en PDAs Android (Zebra, Honeywell, Chainway, etc.)
        if (keyCode == 139 || keyCode == 280 || keyCode == 293 || 
            keyCode == android.view.KeyEvent.KEYCODE_BUTTON_R1 || 
            keyCode == android.view.KeyEvent.KEYCODE_BUTTON_L1 ||
            keyCode == android.view.KeyEvent.KEYCODE_F1 || // A veces mapeado a F1
            keyCode == android.view.KeyEvent.KEYCODE_F2) {
            
            if (!isScanning) {
                startScanning();
            } else {
                stopScanning();
            }
            return true; // Consumimos el evento para manejarlo nosotros
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showMoverCantidadDialog(Producto producto) {
        if (producto == null) {
            Toast.makeText(this, "Error: Producto nulo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Cantidad a mover (Máx: " + producto.getCantidad() + ")");
    
            new AlertDialog.Builder(this)
                    .setTitle("Mover cantidad de " + producto.getNombre())
                    .setMessage("Ingresa la cantidad y escanea la estantería destino.")
                    .setView(input)
                    .setPositiveButton("Siguiente", (dialog, which) -> {
                        String cantidadStr = input.getText().toString();
                        if (!cantidadStr.isEmpty()) {
                            try {
                                int cantidad = Integer.parseInt(cantidadStr);
                                if (cantidad > 0 && cantidad <= producto.getCantidad()) {
                                    cantidadAMoverGlobal = cantidad;
                                    // Guardamos el producto en una variable persistente
                                    productoAMover = producto;
                                    estadoMoverCantidadTrue();
                                    Toast.makeText(this, "Escanea la estantería destino", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Cantidad inválida, excede stock o producto sin estanteria", Toast.LENGTH_SHORT).show();
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "Número inválido", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Error al iniciar escaneo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } catch (Exception e) {
            android.util.Log.e("MainScanActivity", "Error mostrando dialogo mover: " + e.getMessage());
            Toast.makeText(this, "Error interno al mostrar diálogo", Toast.LENGTH_SHORT).show();
        }
    }
}
