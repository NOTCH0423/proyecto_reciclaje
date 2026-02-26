package com.reciclaje.excel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelInventarioClient {

    private static final String GRAPH_BASE = "https://graph.microsoft.com/v1.0";
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded");
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String driveId;
    private final String itemId;

    public ExcelInventarioClient(String tenantId,
                                 String clientId,
                                 String clientSecret,
                                 String driveId,
                                 String itemId) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.driveId = driveId;
        this.itemId = itemId;
        this.client = new OkHttpClient();
        this.gson = new Gson();

        if (tenantId == null || tenantId.isBlank()
                || clientId == null || clientId.isBlank()
                || clientSecret == null || clientSecret.isBlank()
                || driveId == null || driveId.isBlank()
                || itemId == null || itemId.isBlank()) {
            throw new IllegalStateException("Faltan variables de entorno para conectar con Excel (MS_TENANT_ID, MS_CLIENT_ID, MS_CLIENT_SECRET, EXCEL_DRIVE_ID, EXCEL_ITEM_ID).");
        }
    }

    public Map<String, Object> getCategorias() throws IOException {
        return readSheetAsObjects("Categorias");
    }

    public Map<String, Object> getProductos() throws IOException {
        return readSheetAsObjects("Productos");
    }

    public Map<String, Object> agregarCategoria(String nombre) throws IOException {
        if (nombre == null || nombre.isBlank()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "El nombre de la categoría es obligatorio.");
            return result;
        }
        String id = generateUniqueId();
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("nombre", nombre);
        return appendRow("Categorias", values, "Categoría '" + nombre + "' creada con ID " + id);
    }

    public Map<String, Object> agregarProducto(Map<String, Object> data) throws IOException {
        if (data == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Datos de producto vacíos.");
            return result;
        }
        String id = generateUniqueId();
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        // Claves esperadas según el modelo original
        copyIfPresent(data, values, "nombre");
        copyIfPresent(data, values, "código");
        copyIfPresent(data, values, "codigo"); // por si viene sin tilde
        copyIfPresent(data, values, "categoría");
        copyIfPresent(data, values, "categoria");
        copyIfPresent(data, values, "precio_compra");
        copyIfPresent(data, values, "precio_venta");
        copyIfPresent(data, values, "stock");
        // Fecha de creación si la hoja tiene esa columna
        values.putIfAbsent("fecha_creado", LocalDateTime.now().toString());

        return appendRow("Productos", values, "Producto creado con ID " + id);
    }

    private Map<String, Object> readSheetAsObjects(String sheetName) throws IOException {
        String token = getAccessToken();

        HttpUrl url = HttpUrl.parse(GRAPH_BASE + "/drives/" + driveId + "/items/" + itemId
                + "/workbook/worksheets('" + sheetName + "')/usedRange(valuesOnly=true)").newBuilder()
                .build();

        Request req = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String body = resp.body() != null ? resp.body().string() : "";
                throw new IOException("Error HTTP Graph " + resp.code() + ": " + body);
            }
            String body = resp.body() != null ? resp.body().string() : "";
            JsonObject root = gson.fromJson(body, JsonObject.class);
            JsonArray values = root.has("values") ? root.getAsJsonArray("values") : null;
            if (values == null || values.size() < 2) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "error");
                result.put("message", "Hoja '" + sheetName + "' vacía o sin datos suficientes.");
                return result;
            }

            JsonArray headersArray = values.get(0).getAsJsonArray();
            List<String> headers = new ArrayList<>();
            for (JsonElement h : headersArray) {
                headers.add(h.isJsonNull() ? "" : h.getAsString());
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            for (int i = 1; i < values.size(); i++) {
                JsonArray rowArray = values.get(i).getAsJsonArray();
                Map<String, Object> entry = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    if (header == null || header.isBlank()) continue;
                    JsonElement cell = j < rowArray.size() ? rowArray.get(j) : null;
                    if (cell == null || cell.isJsonNull()) {
                        entry.put(header, "");
                    } else if (cell.isJsonPrimitive()) {
                        if (cell.getAsJsonPrimitive().isNumber()) {
                            entry.put(header, cell.getAsNumber());
                        } else {
                            entry.put(header, cell.getAsString());
                        }
                    } else {
                        entry.put(header, cell.toString());
                    }
                }
                boolean empty = entry.values().stream().allMatch(v -> v == null || v.toString().isBlank());
                if (!empty) {
                    rows.add(entry);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", rows);
            return result;
        }
    }

    private Map<String, Object> appendRow(String sheetName, Map<String, Object> valuesByHeader, String successMessage) throws IOException {
        String token = getAccessToken();

        // Primero leemos encabezados y número actual de filas
        HttpUrl url = HttpUrl.parse(GRAPH_BASE + "/drives/" + driveId + "/items/" + itemId
                + "/workbook/worksheets('" + sheetName + "')/usedRange(valuesOnly=true)").newBuilder()
                .build();

        Request getReq = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response resp = client.newCall(getReq).execute()) {
            if (!resp.isSuccessful()) {
                String body = resp.body() != null ? resp.body().string() : "";
                throw new IOException("Error HTTP Graph " + resp.code() + " al leer hoja '" + sheetName + "': " + body);
            }
            String body = resp.body() != null ? resp.body().string() : "";
            JsonObject root = gson.fromJson(body, JsonObject.class);
            JsonArray values = root.has("values") ? root.getAsJsonArray("values") : null;
            if (values == null || values.size() == 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "error");
                result.put("message", "La hoja '" + sheetName + "' no tiene encabezados definidos.");
                return result;
            }

            JsonArray headersArray = values.get(0).getAsJsonArray();
            List<String> headers = new ArrayList<>();
            for (JsonElement h : headersArray) {
                headers.add(h.isJsonNull() ? "" : h.getAsString());
            }

            int currentRows = values.size(); // incluye fila de encabezados
            int newRowIndex = currentRows + 1; // Excel es 1-based (fila nueva debajo de la última)

            // Ordenar los valores según los encabezados
            List<Object> ordered = new ArrayList<>();
            for (String header : headers) {
                if (header == null || header.isBlank()) {
                    ordered.add("");
                } else if (valuesByHeader.containsKey(header)) {
                    ordered.add(valuesByHeader.get(header));
                } else {
                    ordered.add("");
                }
            }

            String lastColLetter = columnIndexToLetter(headers.size() - 1);
            String address = "A" + newRowIndex + ":" + lastColLetter + newRowIndex;

            HttpUrl patchUrl = HttpUrl.parse(GRAPH_BASE + "/drives/" + driveId + "/items/" + itemId
                    + "/workbook/worksheets('" + sheetName + "')/range(address='" + address + "')").newBuilder()
                    .build();

            Map<String, Object> payload = new HashMap<>();
            List<List<Object>> matrix = new ArrayList<>();
            matrix.add(ordered);
            payload.put("values", matrix);

            RequestBody patchBody = RequestBody.create(gson.toJson(payload), JSON);

            Request patchReq = new Request.Builder()
                    .url(patchUrl)
                    .patch(patchBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            try (Response patchResp = client.newCall(patchReq).execute()) {
                if (!patchResp.isSuccessful()) {
                    String respBody = patchResp.body() != null ? patchResp.body().string() : "";
                    throw new IOException("Error HTTP Graph " + patchResp.code() + " al escribir en hoja '" + sheetName + "': " + respBody);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", successMessage);
            return result;
        }
    }

    private String columnIndexToLetter(int index) {
        StringBuilder sb = new StringBuilder();
        int i = index;
        while (i >= 0) {
            int rem = i % 26;
            sb.insert(0, (char) ('A' + rem));
            i = (i / 26) - 1;
        }
        return sb.toString();
    }

    private String generateUniqueId() {
        return "id-" + (Long.toString(System.currentTimeMillis(), 36));
    }

    private void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String key) {
        if (from.containsKey(key) && from.get(key) != null) {
            to.put(key, from.get(key));
        }
    }

    private String getAccessToken() throws IOException {
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        RequestBody body = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", "https://graph.microsoft.com/.default")
                .add("grant_type", "client_credentials")
                .build();

        Request req = new Request.Builder()
                .url(tokenUrl)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                throw new IOException("Error al obtener token de Microsoft: " + resp.code() + " - " + respBody);
            }
            String respBody = resp.body() != null ? resp.body().string() : "";
            JsonObject root = gson.fromJson(respBody, JsonObject.class);
            if (!root.has("access_token")) {
                throw new IOException("Respuesta de token sin 'access_token': " + respBody);
            }
            return root.get("access_token").getAsString();
        }
    }
}

