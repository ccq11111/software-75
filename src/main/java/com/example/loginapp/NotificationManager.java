package com.example.loginapp;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Utility class for showing animated notifications
 */
public class NotificationManager {

    // Notification types
    public enum NotificationType {
        ERROR, SUCCESS, INFO, WARNING
    }

    // SVG paths for icons
    private static final String ERROR_ICON = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z";
    private static final String SUCCESS_ICON = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";
    private static final String INFO_ICON = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
    private static final String WARNING_ICON = "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";

    // Duration for showing notification (in seconds)
    private static final double NOTIFICATION_DURATION = 5.0;

    /**
     * Show a notification
     * 
     * @param owner The window that owns the notification
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type
     */
    public static void showNotification(Window owner, String title, String message, NotificationType type) {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        // Create notification content
        HBox notification = createNotification(title, message, type, popup);
        
        // Add style class based on type
        notification.getStyleClass().add("notification");
        notification.getStyleClass().add("notification-" + type.toString().toLowerCase());
        
        // Add animation class
        notification.getStyleClass().add("notification-slide-in");

        // Add notification to popup
        popup.getContent().add(notification);

        // Show popup
        popup.show(owner);
        
        // Position popup in the top-right corner with some padding
        popup.setX(owner.getX() + owner.getWidth() - notification.getPrefWidth() - 20);
        popup.setY(owner.getY() + 20);

        // Create animation for showing notification
        Timeline showAnimation = new Timeline();
        KeyFrame showKeyFrame = new KeyFrame(Duration.seconds(0.3),
                new KeyValue(notification.translateYProperty(), 0),
                new KeyValue(notification.opacityProperty(), 1));
        showAnimation.getKeyFrames().add(showKeyFrame);
        showAnimation.play();

        // Create animation for hiding notification
        Timeline hideAnimation = new Timeline();
        KeyFrame hideKeyFrame = new KeyFrame(Duration.seconds(0.3),
                new KeyValue(notification.translateYProperty(), -20),
                new KeyValue(notification.opacityProperty(), 0));
        hideAnimation.getKeyFrames().add(hideKeyFrame);
        hideAnimation.setOnFinished(event -> popup.hide());

        // Schedule hiding notification
        Timeline hideTimer = new Timeline(new KeyFrame(Duration.seconds(NOTIFICATION_DURATION), event -> hideAnimation.play()));
        hideTimer.play();
    }

    /**
     * Create a notification node
     * 
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type
     * @param popup The popup containing the notification
     * @return The notification node
     */
    private static HBox createNotification(String title, String message, NotificationType type, Popup popup) {
        // Create notification container
        HBox notification = new HBox();
        notification.setPrefWidth(300);
        notification.setMinHeight(60);
        notification.setSpacing(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        
        // Create icon
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("notification-icon");
        
        // Set icon path based on type
        switch (type) {
            case ERROR:
                icon.setContent(ERROR_ICON);
                break;
            case SUCCESS:
                icon.setContent(SUCCESS_ICON);
                break;
            case INFO:
                icon.setContent(INFO_ICON);
                break;
            case WARNING:
                icon.setContent(WARNING_ICON);
                break;
        }
        
        // Scale icon
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        
        // Create content container
        VBox content = new VBox();
        content.setSpacing(5);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        // Create title label
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notification-title");
        
        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);
        
        // Add title and message to content
        content.getChildren().addAll(titleLabel, messageLabel);
        
        // Create close button
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("notification-close-button");
        closeButton.setOnAction(event -> {
            // Create animation for hiding notification
            Timeline hideAnimation = new Timeline();
            KeyFrame hideKeyFrame = new KeyFrame(Duration.seconds(0.3),
                    new KeyValue(notification.translateYProperty(), -20),
                    new KeyValue(notification.opacityProperty(), 0));
            hideAnimation.getKeyFrames().add(hideKeyFrame);
            hideAnimation.setOnFinished(e -> popup.hide());
            hideAnimation.play();
        });
        
        // Add icon, content and close button to notification
        notification.getChildren().addAll(icon, content, closeButton);
        
        return notification;
    }
    
    /**
     * Show an error notification
     * 
     * @param owner The window that owns the notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showError(Window owner, String title, String message) {
        showNotification(owner, title, message, NotificationType.ERROR);
    }
    
    /**
     * Show a success notification
     * 
     * @param owner The window that owns the notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showSuccess(Window owner, String title, String message) {
        showNotification(owner, title, message, NotificationType.SUCCESS);
    }
    
    /**
     * Show an info notification
     * 
     * @param owner The window that owns the notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showInfo(Window owner, String title, String message) {
        showNotification(owner, title, message, NotificationType.INFO);
    }
    
    /**
     * Show a warning notification
     * 
     * @param owner The window that owns the notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showWarning(Window owner, String title, String message) {
        showNotification(owner, title, message, NotificationType.WARNING);
    }
}
