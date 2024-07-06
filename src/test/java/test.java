import com.google.gson.Gson;
import xxAROX.WebRequester.WebRequester;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class test {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Java HTTP Client");
        HashMap<String, String> body = new HashMap<>();
        headers.put("Hello", "World");
        headers.put("Foo", "Bar");

        WebRequester.init(new Gson(), executor);

        //CompletableFuture<WebRequestResult> future = WebRequester.getAsync("https://presence-man.com/api/v1/partners", headers);
        CompletableFuture<WebRequester.Result> future = WebRequester.postAsync("https://jsonplaceholder.typicode.com/posts", headers, body);

        try {
            WebRequester.Result result = future.get();
            System.out.println(result.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
