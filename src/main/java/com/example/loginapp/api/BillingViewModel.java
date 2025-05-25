package com.example.loginapp.api;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;

public class BillingViewModel {
    // 这里应该添加类的成员变量
    private BillingService billingService;
    private BillingEntry selectedEntry;
    private ObservableList<BillingEntry> billingEntries;
    private Button refreshDataButton;
    private Button addCsvButton;
    private Button buildButton;
    private Button inquireButton;
    private Button deleteButton;
    
    public void deleteSelectedEntry() {
        if (selectedEntry == null) {
            showErrorAlert("请先选择一条记录");
            return;
        }

        setLoading(true);

        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("确认删除记录");
        confirmDialog.setContentText("您确定要删除这条记录吗？此操作无法撤销。");
        
        // 等待用户选择
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("确认删除记录: " + selectedEntry.getEntryId());
            
            // 执行删除操作
            CompletableFuture.supplyAsync(() -> {
                try {
                    return billingService.deleteEntry(selectedEntry.getEntryId());
                } catch (ApiException e) {
                    System.out.println("删除记录时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    return new ApiResponse(false, "删除失败: " + e.getMessage());
                }
            }).thenAccept(response -> {
                System.out.println("删除响应: " + response.isSuccess() + ", " + response.getMessage());
                
                // 更新UI
                Platform.runLater(() -> {
                    setLoading(false);
                    
                    if (response.isSuccess()) {
                        // 从本地列表移除记录
                        billingEntries.remove(selectedEntry);
                        
                        // 重新加载数据以确保与服务器同步
                        loadBillingData();
                        
                        // 提示用户
                        showSuccessAlert("记录已成功删除");
                        
                        // 清除选中的记录
                        selectedEntry = null;
                    } else {
                        showErrorAlert("删除失败: " + response.getMessage());
                    }
                });
            }).exceptionally(ex -> {
                System.out.println("删除操作异常: " + ex.getMessage());
                ex.printStackTrace();
                
                Platform.runLater(() -> {
                    setLoading(false);
                    showErrorAlert("删除失败: " + ex.getMessage());
                });
                
                return null;
            });
        } else {
            setLoading(false);
            System.out.println("用户取消了删除操作");
        }
    }
    
    // 这里需要添加其他方法
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void setLoading(boolean loading) {
        // 可以显示/隐藏加载指示器，或者禁用/启用按钮
        refreshDataButton.setDisable(loading); // 加载时禁用按钮
        // 其他控件也可以在加载时禁用
        addCsvButton.setDisable(loading);
        buildButton.setDisable(loading);
        inquireButton.setDisable(loading);
        deleteButton.setDisable(loading);
    }
    
    private void loadBillingData() {
        // 异步加载账单数据
        CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("开始从后端加载账单数据");
                return billingService.getEntries(null, null, null, null);
            } catch (ApiException e) {
                System.out.println("加载账单数据失败: " + e.getMessage());
                e.printStackTrace();
                throw new CompletionException(e);
            }
        }).thenAccept(entries -> {
            Platform.runLater(() -> {
                System.out.println("加载到 " + entries.size() + " 条账单记录");
                billingEntries.clear();
                billingEntries.addAll(entries);
                setLoading(false);
            });
        }).exceptionally(ex -> {
            System.out.println("加载账单数据异常: " + ex.getMessage());
            ex.printStackTrace();
            
            Platform.runLater(() -> {
                setLoading(false);
                showErrorAlert("加载账单数据失败: " + ex.getMessage());
            });
            
            return null;
        });
    }
}