package it.rehelpstudio.bet365.parser;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

class CustomCookiePolicy implements CookiePolicy {
    public boolean shouldAccept(URI uri, HttpCookie cookie) {
        return true;
    }
}
