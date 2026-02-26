package com.reciclaje.web;

import com.google.gson.Gson;
import com.reciclaje.config.Config;
import com.reciclaje.excel.ExcelInventarioClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

//@RestController
@RequestMapping("/api/excel-inventario")
@CrossOrigin(origins = "*")
public class ExcelInventarioController {

    private final ExcelInventarioClient client;
    private final Gson gson = new Gson();

    public ExcelInventarioController() {
        this.client = new ExcelInventarioClient(
                Config.getMsTenantId(),
                Config.getMsClientId(),
                Config.getMsClientSecret(),
                Config.getExcelDriveId(),
                Config.getExcelItemId()
        );
    }

    @GetMapping("/categorias")
    public ResponseEntity<String> categorias() {
        try {
            Map<String, Object> result = client.getCategorias();
            return ResponseEntity.ok(gson.toJson(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body("{\"status\":\"error\",\"message\":\"Configuración de Excel incompleta: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al leer categorías desde Excel: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/productos")
    public ResponseEntity<String> productos() {
        try {
            Map<String, Object> result = client.getProductos();
            return ResponseEntity.ok(gson.toJson(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body("{\"status\":\"error\",\"message\":\"Configuración de Excel incompleta: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al leer productos desde Excel: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/categorias")
    public ResponseEntity<String> crearCategoria(@RequestBody Map<String, Object> payload) {
        try {
            Object nombre = payload.get("nombre");
            if (nombre == null || nombre.toString().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("{\"status\":\"error\",\"message\":\"El campo 'nombre' es obligatorio\"}");
            }
            Map<String, Object> result = client.agregarCategoria(nombre.toString());
            return ResponseEntity.ok(gson.toJson(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body("{\"status\":\"error\",\"message\":\"Configuración de Excel incompleta: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al crear categoría en Excel: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/productos")
    public ResponseEntity<String> crearProducto(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> result = client.agregarProducto(payload);
            return ResponseEntity.ok(gson.toJson(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body("{\"status\":\"error\",\"message\":\"Configuración de Excel incompleta: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al crear producto en Excel: " + e.getMessage() + "\"}");
        }
    }
}

