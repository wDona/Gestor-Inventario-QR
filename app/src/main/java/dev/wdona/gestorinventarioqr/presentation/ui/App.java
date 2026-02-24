package dev.wdona.gestorinventarioqr.presentation.ui;

import android.app.Application;

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
import dev.wdona.gestorinventarioqr.mock.JSONUtils;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseController;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.EstanteriaViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.OperacionViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.ProductoViewModel;

public class App extends Application {
    private static App instance;
    public EstanteriaViewModel estanteriaViewModel;
    public ProductoViewModel productoViewModel;
    public OperacionViewModel operacionViewModel;
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            // Inicializar JSONUtils con el contexto ANTES de usarlo
            JSONUtils.init(getApplicationContext());
            inicializarCapaDatos();
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e(TAG, "Error en inicialización: " + e.getMessage(), e);
        }
    }

    public static App getInstance() {
        return instance;
    }

    private void inicializarCapaDatos() {
        try {
            android.util.Log.d(TAG, "Iniciando inicializarCapaDatos...");

            // PRIMERO inicializar los JSONs antes de crear las APIs que los usan
            MockDatabaseController.initialize();
            android.util.Log.d(TAG, "MockDatabaseController inicializado");

            EstanteriaApiImpl estanteriaApi = new EstanteriaApiImpl();
            ProductoApiImpl productoApi = new ProductoApiImpl();
            android.util.Log.d(TAG, "APIs creadas");

            AppDatabase appDatabase = AppDatabase.getDatabase(getApplicationContext());
            android.util.Log.d(TAG, "AppDatabase obtenida");

            EstanteriaLocalDataSourceImpl estanteriaLocalDataSource = new EstanteriaLocalDataSourceImpl(appDatabase.estanteriaDao());
            ProductoLocalDataSourceImpl productoLocalDataSource = new ProductoLocalDataSourceImpl(appDatabase.productoDao(), appDatabase.estanteriaDao());
            OperacionLocalDataSourceImpl operacionLocalDataSource = new OperacionLocalDataSourceImpl(appDatabase.operacionDao());
            android.util.Log.d(TAG, "LocalDataSources creados");

            EstanteriaRemoteDataSourceImpl estanteriaRemoteDataSource = new EstanteriaRemoteDataSourceImpl(estanteriaApi);
            ProductoRemoteDataSourceImpl productoRemoteDataSource = new ProductoRemoteDataSourceImpl(productoApi);
            android.util.Log.d(TAG, "RemoteDataSources creados");

            EstanteriaRepositoryImpl estanteriaRepository = new EstanteriaRepositoryImpl(estanteriaRemoteDataSource, estanteriaLocalDataSource);
            ProductoRepositoryImpl productoRepository = new ProductoRepositoryImpl(productoRemoteDataSource, productoLocalDataSource);
            OperacionRepositoryImpl operacionRepository = new OperacionRepositoryImpl(operacionLocalDataSource, productoRemoteDataSource, estanteriaLocalDataSource, productoLocalDataSource);
            android.util.Log.d(TAG, "Repositories creados");

            this.estanteriaViewModel = new EstanteriaViewModel(estanteriaRepository);
            this.productoViewModel = new ProductoViewModel(productoRepository);
            this.operacionViewModel = new OperacionViewModel(operacionRepository);
            android.util.Log.d(TAG, "ViewModels creados");

            appDatabase.populateInitialData();
            android.util.Log.d(TAG, "populateInitialData ejecutado");


            android.util.Log.d(TAG, "Inicialización completada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e(TAG, "Error en inicializarCapaDatos: " + e.getMessage(), e);
        }
    }
}
