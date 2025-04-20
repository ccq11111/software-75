package com.example.loginapp;

/**
 * 应用程序启动器类
 * 这个类的目的是解决使用maven-shade-plugin打包JavaFX应用的问题
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}