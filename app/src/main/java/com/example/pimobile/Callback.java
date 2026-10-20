package com.example.pimobile;

public interface Callback<T> {
    void onSucesso(T resultado);
    void onErro(Exception e);
}