package com.example.loginapp.controller;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * 帮助工具类：用于从任意 Node 获取 BaseViewController
 */
public class ViewControllerHelper {
    public static BaseViewController getBaseController(Node anyNode) {
        if (anyNode == null) return null;
        Parent root = anyNode.getScene().getRoot();
        Object userData = root.getUserData();
        if (userData instanceof BaseViewController) {
            return (BaseViewController) userData;
        }
        return null;
    }
}
