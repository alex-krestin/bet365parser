package it.rehelpstudio.bet365.parser;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Connection {
    public static String postRequest(String url, ArrayList<Header> headers) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);

        Header[] heads = headers.toArray(new Header[headers.size()]);
        post.setHeaders(heads);

        //Execute and get the response.
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        }
        return null;
    }
}
