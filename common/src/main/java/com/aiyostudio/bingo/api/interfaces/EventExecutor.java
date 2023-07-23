package com.aiyostudio.bingo.api.interfaces;

@FunctionalInterface
public interface EventExecutor<T> {
    
    void run(T t);
}