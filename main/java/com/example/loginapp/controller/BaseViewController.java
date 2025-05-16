package com.example.loginapp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.loginapp.service.JwtSecretKeyService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@Component
public class BaseViewController {

    @FXML private Label usernameLabel;
    @FXML private StackPane contentArea;

    @FXML private Button billingButton, summaryButton, savingButton, footprintButton, setButton, aiButton;
    @FXML private HBox billingMenuItem, summaryMenuItem, savingMenuItem, footprintMenuItem, setMenuItem;

    private String currentView = "";
    private String authToken;

    private JwtSecretKeyService jwtService;

    public BaseViewController() {
    }

    @Autowired
    public void setJwtService(JwtSecretKeyService jwtService) {
        this.jwtService = jwtService;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        if (jwtService != null) {
            String userId = jwtService.extractUserId(token);
            if (usernameLabel != null) {
                usernameLabel.setText(userId);
            }
        }
    }

    @FXML
    public void initialize() {
        setupButtonHandlers();
        loadBillingView();
    }

    private void setupButtonHandlers() {
        billingButton.setOnAction(e -> loadBillingView());
        summaryButton.setOnAction(e -> loadSummaryView());
        savingButton.setOnAction(e -> loadSavingView());
        footprintButton.setOnAction(e -> System.out.println("Footprint clicked"));
        setButton.setOnAction(e -> loadSetView());
        aiButton.setOnAction(e -> openAIDialog());
    }

    private void loadBillingView() { loadView("billing", BillingViewController.class); }
    private void loadSummaryView() { loadView("summary", SummaryViewController.class); }
    public void loadSavingView() { loadView("saving", SavingViewController.class); }
    private void loadSetView() { loadView("set", SetViewController.class); }

    private void loadView(String viewName, Class<?> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + viewName + "View.fxml"));
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = controllerClass.getDeclaredConstructor().newInstance();
                    if (controller instanceof TokenAwareController) {
                        ((TokenAwareController) controller).setAuthToken(authToken);
                    }
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create controller: " + controllerClass.getName(), e);
                }
            });
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            currentView = viewName;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAIDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AIView.fxml"));
            loader.setControllerFactory(clazz -> {
                try {
                    AIViewController controller = new AIViewController(jwtService);
                    controller.setAuthToken(authToken);
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create AI controller", e);
                }
            });
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AI Chat");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
