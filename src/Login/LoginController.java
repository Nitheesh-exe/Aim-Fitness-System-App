package Login;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import Main.AdminDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        char[] password = passwordField.getText().toCharArray();
        try {
            if (!AdminDao.hasAdminAccount()) {
                try (java.sql.Connection conn = Main.DatabaseSetup.getConnection()) {
                    AdminDao.bootstrapFirstAdmin(conn);
                }
            }
            if (!AdminDao.authenticate(usernameField.getText(), password)) {
                showError("Incorrect username or password. (Default: admin / admin)");
                return;
            }
        } catch (SQLException e) {
            showError("Unable to verify login. Check the database setup.");
            return;
        } finally {
            Arrays.fill(password, '\0');
            passwordField.clear();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main/aimfxml.fxml"));
        Parent dashboardRoot = loader.load();
        Stage dashboardStage = new Stage();
        try {
            var iconStream = getClass().getResourceAsStream("/Main/icon.png");
            if (iconStream == null) {
                iconStream = getClass().getResourceAsStream("/Login/icon.png");
            }
            if (iconStream != null) {
                dashboardStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.out.println("Dashboard icon not found.");
        }
        dashboardStage.setScene(new Scene(dashboardRoot));
        dashboardStage.setTitle("AIM Fitness Park");
        dashboardStage.setMaximized(true); 
        dashboardStage.show();
        Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        loginStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
    
    @FXML
    private void closeApplication(ActionEvent event) {
        System.exit(0);
    }
}