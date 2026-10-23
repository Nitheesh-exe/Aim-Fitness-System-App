package Login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;

public class Login extends Application {
    
        @Override
    public void start(Stage stage) throws Exception {
        
        Main.DatabaseSetup.initializeDatabase(); 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login/Login.fxml"));
        Parent root = loader.load();    
        
        try {
            var iconStream = getClass().getResourceAsStream("/Login/icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.out.println("Login icon stream not found.");
        } 
        stage.initStyle(StageStyle.UNDECORATED);
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.70;
        double height = screenBounds.getHeight() * 0.70;
        
        stage.setScene(new Scene(root, width, height));  
        stage.setTitle("AIM Fitness Park - Admin Login");
        stage.centerOnScreen();
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}