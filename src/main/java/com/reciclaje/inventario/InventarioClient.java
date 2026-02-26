package com.reciclaje.inventario;

import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InventarioClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl;

    public InventarioClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del script de inventario no está configurada.");
        }
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public String getCategorias() throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "getCategorias")
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(req);
    }

    public String getInventario() throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "getInventario")
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(req);
    }

    public String buscarProducto(String query) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "buscarProducto")
                .addQueryParameter("query", query == null ? "" : query)
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(req);
    }

    public String iniciarBaseDeDatos() throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "iniciar")
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(req);
    }

    public String resetearBaseDeDatos() throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "resetear")
                .build();
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(req);
    }

    public String agregarCategoria(String nombre) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "agregarCategoria");
        payload.put("nombre", nombre);
        return postJson(payload);
    }

    public String agregarProducto(Map<String, Object> data) throws IOException {
        Map<String, Object> payload = new HashMap<>(data);
        payload.put("action", "agregarProducto");
        return postJson(payload);
    }

    public String registrarTransaccion(Map<String, Object> data) throws IOException {
        Map<String, Object> payload = new HashMap<>(data);
        payload.put("action", "registrarTransaccion");
        return postJson(payload);
    }

    private String postJson(Map<String, Object> payload) throws IOException {
        String json = gson.toJson(payload);
        RequestBody body = RequestBody.create(json, JSON);
        Request req = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        return execute(req);
    }

    private String execute(Request req) throws IOException {
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String body = resp.body() != null ? resp.body().string() : "";
                throw new IOException("Error HTTP " + resp.code() + ": " + body);
            }
            if (resp.body() == null) {
                return "";
            }
            return resp.body().string();
        }
    }
}

