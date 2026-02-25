package dev.wdona.gestorinventarioqr.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.data.api.impl.EstanteriaApiImpl;
import dev.wdona.gestorinventarioqr.data.api.impl.ProductoApiImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.OperacionLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.db.AppDatabase;
import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.db.OperacionDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.data.repository.EstanteriaRepositoryImpl;
import dev.wdona.gestorinventarioqr.data.repository.OperacionRepositoryImpl;
import dev.wdona.gestorinventarioqr.data.repository.ProductoRepositoryImpl;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseController;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.EstanteriaViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.OperacionViewModel;
import dev.wdona.gestorinventarioqr.presentation.viewmodel.ProductoViewModel;

public class OldMainActivity extends AppCompatActivity {

    // TODO: Pantalla de historial de operaciones, mostrando las operaciones realizadas y las pendientes (si se actua offline)

    // TODO: Registrar operaciones localmente para mostrar historial y pendientes
    //      PARA TESTEAR PENDIENTES, "DESACTIVAR" MOCK DE BASE DE DATOS "ONLINE" Y ACTUAR OFFLINE

    /**
     * Arquitectura
     * ViewModels -> Repositories -> DataSources (Local/Remote)
     * ViewModels: Gestionan la lógica de UI y exponen datos a la vista.
     * Repositories: Actúan como intermediarios entre los ViewModels y las fuentes de datos, gestionando la lógica de negocio.
     * DataSources: Encapsulan el acceso a los datos, ya sea local (SQLite, Room (DAO)) o remoto (API) (RemoteDataSource, LocalDataSource).
     */

    EstanteriaViewModel estanteriaViewModel;
    ProductoViewModel productoViewModel;

    OperacionViewModel operacionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}