package it.rehelpstudio.bet365.parser;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.*;

public class CustomCookieStore implements CookieStore {
    private final Map<URI, List<HttpCookie>> map = new HashMap<>();

    public void add(URI uri, HttpCookie cookie) {
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<>();
            map.put(uri, cookies);
        }
        cookies.add(cookie);
    }

    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<>();
            map.put(uri, cookies);
        }
        return cookies;
    }

    public List<HttpCookie> getCookies() {
        Collection<List<HttpCookie>> values = map.values();
        List<HttpCookie> result = new ArrayList<>();
        values.forEach(result::addAll);
        return result;
    }

    public List<URI> getURIs() {
        Set<URI> keys = map.keySet();
        return new ArrayList<>(keys);

    }

    public boolean remove(URI uri, HttpCookie cookie) {
        List<HttpCookie> cookies = map.get(uri);
        return cookies != null && cookies.remove(cookie);
    }

    public boolean removeAll() {
        map.clear();
        return true;
    }
}