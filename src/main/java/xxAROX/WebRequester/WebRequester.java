/*
 * Copyright (c) $originalComment.match("Copyright \(c\) (\d+)", 1, "-", "$today.year")2024. By Jan-Michael Sohn also known as @xxAROX.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.WebRequester;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@AllArgsConstructor
public class WebRequester {
    private static boolean initialized = false;
    private static Gson GSON = null;
    private static ExecutorService executor = null;

    public static void init(Gson GSON, ExecutorService executor) {
        if (initialized) return;
        initialized = true;
        WebRequester.GSON = GSON;
        WebRequester.executor = executor;
    }
    private static void check() {
        if (!initialized) throw new Error("WebRequester must be initialized first");
    }

    public static Future<Result> get(String url) {
        return get(url, Collections.emptyMap());
    }
    public static Future<Result> get(String url, Map<String, String> headers) {
        check();
        return executor.submit(() -> {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");

            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> headerEntry : conn.getHeaderFields().entrySet().stream().filter(headerEntry -> headerEntry.getKey() != null).toList())
                responseHeaders.put(headerEntry.getKey(), String.join(",", headerEntry.getValue()));

            return new Result(responseCode, response.toString(), responseHeaders);
        });
    }
    public static CompletableFuture<Result> getAsync(String url) {
        return getAsync(url, Collections.emptyMap());
    }
    public static CompletableFuture<Result> getAsync(String url, Map<String, String> headers) {
        check();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return get(url, headers).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public static Future<Result> post(String url, Map<String, String> headers) {
        return post(url, headers, Collections.emptyMap());
    }
    public static Future<Result> post(String url) {
        return post(url, Collections.emptyMap(), Collections.emptyMap());
    }
    public static Future<Result> post(String url, Map<String, String> headers, Map<String, String> body) {
        check();
        return executor.submit(() -> {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setRequestProperty("Content-Type", "application/json");
            String jsonBody = GSON.toJson(body);
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(jsonBody);
                wr.flush();
            }


            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> headerEntry : conn.getHeaderFields().entrySet().stream().filter(headerEntry -> headerEntry.getKey() != null).toList())
                responseHeaders.put(headerEntry.getKey(), String.join(",", headerEntry.getValue()));

            return new Result(responseCode, response.toString(), responseHeaders);
        });
    }
    public static CompletableFuture<Result> postAsync(String url) {
        return postAsync(url, Collections.emptyMap(), Collections.emptyMap());
    }
    public static CompletableFuture<Result> postAsync(String url, Map<String, String> headers) {
        return postAsync(url, headers, Collections.emptyMap());
    }
    public static CompletableFuture<Result> postAsync(String url, Map<String, String> headers, Map<String, String> body) {
        check();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(url, headers, body).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }


    @AllArgsConstructor @Getter
    public static class Result {
        protected Integer status;
        protected String body;
        protected Map<String, String> headers;
    }
}
