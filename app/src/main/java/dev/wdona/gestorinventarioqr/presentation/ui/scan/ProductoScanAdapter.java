package dev.wdona.gestorinventarioqr.presentation.ui.scan;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoScanAdapter extends RecyclerView.Adapter<ProductoScanAdapter.ViewHolder> {

    private List<Producto> productos = new ArrayList<>();
    private OnProductoInteractionListener listener;

    public interface OnProductoInteractionListener {
        void onProductoClick(Producto producto);
        void onAddStock(Producto producto);
        void onRemoveStock(Producto producto);
    }

    public ProductoScanAdapter(OnProductoInteractionListener listener) {
        this.listener = listener;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombre;
        private final TextView tvCantidad;
        private final TextView tvPrecio;
        private final View btnAddStock;
        private final View btnRemoveStock;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvProductoNombre);
            tvCantidad = itemView.findViewById(R.id.tvProductoCantidad);
            tvPrecio = itemView.findViewById(R.id.tvProductoPrecio);
            btnAddStock = itemView.findViewById(R.id.btnAddStock);
            btnRemoveStock = itemView.findViewById(R.id.btnRemoveStock);
            
            // Contenedor de información clickeable
            View infoContainer = itemView.findViewById(R.id.infoContainer);
            if (infoContainer != null) {
                // Si existe el container específico, el click va ahí
                infoContainer.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onProductoClick(productos.get(pos));
                    }
                });
                
                // Y desactivamos el click en el padre para evitar conflictos
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
            } else {
                // Fallback: Si no se encuentra el container, usamos el itemView completo
                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onProductoClick(productos.get(pos));
                    }
                });
            }

            btnAddStock.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddStock(productos.get(pos));
                }
            });

            btnRemoveStock.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRemoveStock(productos.get(pos));
                }
            });
        }

        @SuppressLint("DefaultLocale")
        void bind(Producto producto) {
            tvNombre.setText(producto.getNombre());
            tvCantidad.setText(String.valueOf(producto.getCantidad()));
            tvPrecio.setText(String.format("%.2f €", producto.getPrecio()));
        }
    }
}

