package org.example.cookiesAndStorage;

public class SimpleCookie {
    String name;
    String value;
    String domain;
    String path;
    Long expiry; // Время в миллисекундах (или секундах, если так в JSON)
    boolean isSecure;
    boolean isHttpOnly;
}