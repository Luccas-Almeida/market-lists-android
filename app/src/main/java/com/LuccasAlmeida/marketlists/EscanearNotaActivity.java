package com.LuccasAlmeida.marketlists;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.ArrayList;
import java.util.List;

public class EscanearNotaActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> qrScannerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escanear_nota);

        // Configura lançadores de permissão e leitor de QR Code
        setupLaunchers();

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_escanear);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(EscanearNotaActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(EscanearNotaActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        startActivity(new Intent(EscanearNotaActivity.this, MinhasComprasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(EscanearNotaActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        return true;
                    } else if (itemId == R.id.nav_despesas) {
                        startActivity(new Intent(EscanearNotaActivity.this, MinhasComprasDespesasActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        }

        MaterialCardView cardNovaNota = findViewById(R.id.cardNovaNota);
        if (cardNovaNota != null) {
            cardNovaNota.setOnClickListener(v -> checarEIniciarCamera());
        }

        // Configurar cliques nos cartões de notas salvos
        MaterialCardView cardNota1 = findViewById(R.id.cardNota1);
        if (cardNota1 != null) {
            cardNota1.setOnClickListener(v -> abrirDetalhesNota("NFCe-001"));
        }

        MaterialCardView cardNota2 = findViewById(R.id.cardNota2);
        if (cardNota2 != null) {
            cardNota2.setOnClickListener(v -> abrirDetalhesNota("NFCe-002"));
        }
    }

    private void setupLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        abrirScannerQrCode();
                    } else {
                        Toast.makeText(this, "Permissão de câmera é necessária para escanear a nota", Toast.LENGTH_LONG).show();
                    }
                }
        );

        qrScannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String qrUrl = result.getData().getStringExtra(QrScannerActivity.EXTRA_QR_RESULT);
                        processarNotaEscaneada(qrUrl);
                    }
                }
        );
    }

    private void checarEIniciarCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirScannerQrCode();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirScannerQrCode() {
        Intent intent = new Intent(EscanearNotaActivity.this, QrScannerActivity.class);
        qrScannerLauncher.launch(intent);
    }

    private void processarNotaEscaneada(String qrUrl) {
        String idNovaNota = "NFCe-" + (System.currentTimeMillis() % 10000);

        List<Produto> produtosLidos = new ArrayList<>();
        produtosLidos.add(new Produto(301, "Arroz Parboilizado 5kg", 28.90, "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699479/mercearia.jpg"));
        produtosLidos.add(new Produto(302, "Feijão Preto 1kg", 8.50, "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699479/mercearia.jpg"));
        produtosLidos.add(new Produto(303, "Carne Bovina Alcatra 1kg", 42.00, "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699458/carnes.jpg"));

        NotaFiscal novaNota = new NotaFiscal(
                idNovaNota,
                "Supermercado Festval - PR",
                "Hoje",
                79.40,
                qrUrl != null ? qrUrl : "http://www.fazenda.pr.gov.br/nfce/consulta",
                produtosLidos
        );

        ProdutoRepository.getInstance().adicionarNota(novaNota);

        Toast.makeText(this, "Nota escaneada com sucesso!", Toast.LENGTH_SHORT).show();

        abrirDetalhesNota(idNovaNota);
    }

    private void abrirDetalhesNota(String idNota) {
        Intent intent = new Intent(EscanearNotaActivity.this, DetalhesNotaActivity.class);
        intent.putExtra(DetalhesNotaActivity.EXTRA_NOTA_ID, idNota);
        startActivity(intent);
    }
}