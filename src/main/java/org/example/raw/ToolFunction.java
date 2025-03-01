package org.example.raw;

@FunctionalInterface
interface ToolFunction<T> {
    T execute(String query);
}

