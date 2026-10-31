package com.example.pimobile;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloudinaryUploader {
    private static final String CLOUD_NAME = "hmjknkca";
    private static final String UPLOAD_PRESET = "servmais_perfil_unsigned";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface CallbackUpload {
        void onSucesso(String secureUrl, String publicId);
        void onErro(Exception e);
    }

    private CloudinaryUploader() {}

    public static void enviar(Context context, Uri uri, CallbackUpload callback) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                ContentResolver resolver = appContext.getContentResolver();
                String mime = resolver.getType(uri);
                if (mime == null || !mime.startsWith("image/")) {
                    throw new IllegalArgumentException("Selecione um arquivo de imagem.");
                }
                String boundary = "----ServMais" + UUID.randomUUID().toString().replace("-", "");
                URL url = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream raw = connection.getOutputStream();
                     DataOutputStream out = new DataOutputStream(raw)) {
                    campo(out, boundary, "upload_preset", UPLOAD_PRESET);
                    campo(out, boundary, "folder", "servmais/perfis");
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"perfil\"\r\n");
                    out.writeBytes("Content-Type: " + mime + "\r\n\r\n");
                    try (InputStream in = resolver.openInputStream(uri)) {
                        if (in == null) throw new IllegalStateException("Não foi possível abrir a imagem selecionada.");
                        byte[] buffer = new byte[8192];
                        int n;
                        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                    }
                    out.writeBytes("\r\n--" + boundary + "--\r\n");
                    out.flush();
                }

                int status = connection.getResponseCode();
                InputStream response = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }
                JSONObject json = new JSONObject(body.toString());
                if (status < 200 || status >= 300) {
                    String message = json.optString("error", "");
                    if (message.isEmpty()) message = "Falha no envio da foto (HTTP " + status + ").";
                    else if (json.optJSONObject("error") != null) message = json.optJSONObject("error").optString("message", message);
                    throw new IllegalStateException(message);
                }
                String secureUrl = json.optString("secure_url", "");
                String publicId = json.optString("public_id", "");
                if (secureUrl.isEmpty() || publicId.isEmpty()) throw new IllegalStateException("A Cloudinary não retornou os dados da foto.");
                String finalUrl = secureUrl;
                String finalPublicId = publicId;
                MAIN.post(() -> callback.onSucesso(finalUrl, finalPublicId));
            } catch (Exception e) {
                MAIN.post(() -> callback.onErro(e));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static void campo(DataOutputStream out, String boundary, String nome, String valor) throws Exception {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + nome + "\"\r\n\r\n");
        out.write(valor.getBytes(StandardCharsets.UTF_8));
        out.writeBytes("\r\n");
    }
}
