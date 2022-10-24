package com.zappkit.zappid.lemeor.api.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zappkit.zappid.lemeor.api.exception.ApiException;
import com.zappkit.zappid.lemeor.tools.Utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public abstract class AbstractHttpApi implements HttpApi {
    public static final String CHARSET = "UTF-8";
    public static final int CONNECT_TIME_OUT = 25000;
    public static final int READ_TIME_OUT = 25000;
    private static final String LINE_FEED = "\r\n";
    private final String BOUNDARY = "*****";
    private final String TWO_HYPHENS = "--";

    protected String executeHttpPost(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                     String jsonObject) throws ApiException, IOException {
        HttpURLConnection connection = prepareConnection(requestUrl);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=" + CHARSET);
        connection.setRequestProperty("Content-Length", "" + jsonObject.getBytes().length);
        if (headers != null) addHeaderFields(connection, headers);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), CHARSET), true);
        writer.append(jsonObject);
        writer.flush();
        writer.close();
        return executeHttpRequest(connection);
    }

    protected String executeHttpPost(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                     JSONObject jsonObject)
            throws ApiException, IOException {
        return executeHttpPost(requestUrl, headers, jsonObject.toString());
    }

    protected String executeHttpPost(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                     Map<String, String> params)
            throws ApiException, IOException {
        String urlParams = Utilities.getParamsRequest(params);
        byte[] postDataBytes = urlParams.getBytes();

        HttpURLConnection connection = prepareConnection(requestUrl);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + CHARSET);
        connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        if (headers != null) addHeaderFields(connection, headers);
        if (!TextUtils.isEmpty(urlParams)) { connection.getOutputStream().write(postDataBytes); }
        return executeHttpRequest(connection);
    }

    protected String executeHttpGet(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                    @Nullable Map<String, String> params)
            throws ApiException, IOException {
        String urlParams = null;
        if (params != null) urlParams = Utilities.getParamsRequest(params);
        String queryUrl = TextUtils.isEmpty(urlParams) ? requestUrl : (requestUrl + "?" + urlParams);
        HttpURLConnection connection = prepareConnection(queryUrl);
        connection.setRequestMethod("GET");
        if (headers != null) addHeaderFields(connection, headers);
        return executeHttpRequest(connection);
    }

    protected String executeHttpGet(@NonNull String requestUrl, @Nullable Map<String, String> headers)
            throws ApiException, IOException {
        HttpURLConnection connection = prepareConnection(requestUrl);
        connection.setRequestMethod("GET");
        if (headers != null) addHeaderFields(connection, headers);
        return executeHttpRequest(connection);
    }

    protected String executeHttpMultipart(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                          Map<String, String> params, Map<String, File> files)
            throws ApiException, IOException {

        HttpURLConnection connection = prepareConnection(requestUrl);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "multipart/form-data; charset=" + CHARSET + "; boundary=" + BOUNDARY);
        if (headers != null) addHeaderFields(connection, headers);
        OutputStream outputStream = connection.getOutputStream();
        DataOutputStream dataStream = new DataOutputStream(outputStream);
        if (params != null) addFormFields(dataStream, params);
        if (files != null && files.size() > 0) addFileParts(dataStream, outputStream, files);
        dataStream.writeBytes(TWO_HYPHENS + BOUNDARY +
                TWO_HYPHENS + LINE_FEED);
        dataStream.flush();
        dataStream.close();
        return executeHttpRequest(connection);
    }

    protected String executeHttpMultipartImages(@NonNull String requestUrl, @Nullable Map<String, String> headers,
                                                Map<String, String> params, ArrayList<File> files)
            throws ApiException, IOException {

        HttpURLConnection connection = prepareConnection(requestUrl);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "multipart/form-data; charset=" + CHARSET + "; boundary=" + BOUNDARY);
        if (headers != null) addHeaderFields(connection, headers);
        OutputStream outputStream = connection.getOutputStream();
        DataOutputStream dataStream = new DataOutputStream(outputStream);
        if (params != null) addFormFields(dataStream, params);
        if (files != null && files.size() > 0) addFileImagesParts(dataStream, outputStream, files);
        dataStream.writeBytes(TWO_HYPHENS + BOUNDARY +
                TWO_HYPHENS + LINE_FEED);
        dataStream.flush();
        dataStream.close();
        return executeHttpRequest(connection);
    }

    private HttpURLConnection prepareConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("connection", "close");
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        connection.setUseCaches(false);
        return connection;
    }

    private String executeHttpRequest(HttpURLConnection connection)
            throws ApiException, IOException {
        StringBuilder response = new StringBuilder();
        // checks server's status code first
        int status = connection.getResponseCode();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    status == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
        } catch (Exception ex) {
            throw new ApiException(ApiException.NETWORK_ERROR, "Server returned non-OK status: " + status);
        }
        return response.toString();
    }

    public void addHeaderFields(HttpURLConnection connection, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    public void addFormFields(DataOutputStream stream, Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            addFormField(stream, entry.getKey(), entry.getValue());
        }
    }

    private void addFileParts(DataOutputStream stream, OutputStream outputStream, Map<String, File> files) throws IOException {
        for (Map.Entry<String, File> entry : files.entrySet()) {
            if (entry.getValue() != null)// && entry.getValue().exists()
                addFilePart(stream, outputStream, entry.getKey(), entry.getValue());
        }
    }

    private void addFileImagesParts(DataOutputStream stream, OutputStream outputStream, ArrayList<File> images) throws IOException {
        for (File item : images) {
                addFilePart(stream, outputStream, "ImageFiles", item);
        }
    }

    public void addFormField(DataOutputStream dataStream, String name, String value) throws IOException {
        if (value != null && !value.equals("null")) {
            dataStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_FEED);
            dataStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED);
            dataStream.writeBytes(LINE_FEED);
            dataStream.write(value.getBytes(CHARSET));
            dataStream.writeBytes(LINE_FEED);
            dataStream.flush();
        }
    }

    public void addFilePart(DataOutputStream stream, OutputStream outputStream, String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        stream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_FEED);
        stream.writeBytes(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        stream.writeBytes(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName));
        stream.writeBytes(LINE_FEED);
        stream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
        stream.writeBytes(LINE_FEED);
        stream.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        stream.writeBytes(LINE_FEED);
        stream.flush();
    }
}
