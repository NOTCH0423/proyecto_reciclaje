package com.reciclaje.web;

import com.reciclaje.config.Config;
import com.reciclaje.inventario.InventarioClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    private final InventarioClient client;

    public InventarioController() {
        this.client = new InventarioClient(Config.getInventarioScriptUrl());
    }

    @GetMapping("/categorias")
    public ResponseEntity<String> listarCategorias() {
        try {
            String json = client.getCategorias();
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al obtener categorías: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/inventario")
    public ResponseEntity<String> listarInventario() {
        try {
            String json = client.getInventario();
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al obtener inventario: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<String> buscarProducto(@RequestParam("q") String query) {
        try {
            String json = client.buscarProducto(query);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al buscar producto: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/iniciar")
    public ResponseEntity<String> iniciarBaseDatos() {
        try {
            String json = client.iniciarBaseDeDatos();
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al iniciar la base de datos: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/resetear")
    public ResponseEntity<String> resetearBaseDatos() {
        try {
            String json = client.resetearBaseDeDatos();
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al resetear la base de datos: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/categorias")
    public ResponseEntity<String> crearCategoria(@RequestBody Map<String, Object> payload) {
        Object nombre = payload.get("nombre");
        if (nombre == null || nombre.toString().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("{\"status\":\"error\",\"message\":\"El campo 'nombre' es obligatorio\"}");
        }
        try {
            String json = client.agregarCategoria(nombre.toString());
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al crear categoría: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/productos")
    public ResponseEntity<String> crearProducto(@RequestBody Map<String, Object> payload) {
        try {
            String json = client.agregarProducto(payload);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al crear producto: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/transacciones")
    public ResponseEntity<String> registrarTransaccion(@RequestBody Map<String, Object> payload) {
        try {
            String json = client.registrarTransaccion(payload);
            return ResponseEntity.ok(json);
        } catch (IOException e) {
            return ResponseEntity.status(502).body("{\"status\":\"error\",\"message\":\"Error al registrar transacción: " + e.getMessage() + "\"}");
        }
    }
}

