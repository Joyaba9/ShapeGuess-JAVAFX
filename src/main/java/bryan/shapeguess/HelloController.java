package bryan.shapeguess;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
/**
        * Controller class for shape guessing game.
        * Handles UI , database operations, and game.
        */
public class HelloController {

    @FXML
    protected RadioButton rectangle;
    @FXML
    protected RadioButton circle;
    @FXML
    protected Button guessButton;
    @FXML
    protected TextField guessCount;
    @FXML
    protected TextField correctGuessCount;
    @FXML
    protected Rectangle rectangle1;
    @FXML
    protected Circle circle1;
    @FXML
    private ListView listViewShows;

    @FXML
    protected Button showDB;

    // makes it so that only one RadioButton can be selected at a time.
    private ToggleGroup group = new ToggleGroup();
    /**
     * Generates a random shape "CIRCLE" : "RECTANGLE".
     * @return A string representing the randomly selected shape.
     */
    private String randomShape() {
        Random random = new Random();
        return random.nextBoolean() ? "CIRCLE" : "RECTANGLE";
    }
    /**
     * Handles the guess button click event.
     * Checks if the guessed shape matches the randomly chosen shape and inserts the guess into the database.
     */
    @FXML
    protected void onGuessButtonClick() {
        String chosenShape = randomShape();
        System.out.println("Randomly chosen shape: " + chosenShape);

        RadioButton selectedRadioB = (RadioButton) group.getSelectedToggle();
        String guessedShape = selectedRadioB != null ? selectedRadioB.getText().toUpperCase() : "NONE";

        increaseGuessCount();
        checkGuess(chosenShape);
        insertDB(chosenShape, guessedShape);
        System.out.println("Randomly chosen shape: " + chosenShape + ", guessed: " + guessedShape);
    }

    /**
     * Increases the guess count by 1.
     */
    private void increaseGuessCount() {
        int count = Integer.parseInt(guessCount.getText());
        guessCount.setText(String.valueOf(++count));
    }

    /**
     * Checks if the user's guess matches the randomly chosen shape and updates accordingly.
     * @param chosenShape The shape chosen by the game.
     */
    private void checkGuess(String chosenShape) {
            RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
            if (selectedRadioButton != null) {
                String guessedShape = selectedRadioButton.getText().toUpperCase();
                boolean isCorrect = guessedShape.equalsIgnoreCase(chosenShape);
                if (isCorrect) {
                    System.out.println("Correct guess!");
                    int count = Integer.parseInt(correctGuessCount.getText());
                    correctGuessCount.setText(String.valueOf(++count));
                } else {
                    System.out.println("Incorrect guess.");
                }
                // Trigger animation based on the guess
                animateGuess(isCorrect, guessedShape);
            }
        }
    /**
     * Inserts the guess result into the database.
     * @param correctShape The shape correctly chosen by the game.
     * @param guessedShape The shape guessed by the user.
     */
    private void insertDB(String correctShape, String guessedShape) {
        String dbFilePath = ".//ShapeGuess.accdb";
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

        try (Connection conn = DriverManager.getConnection(databaseURL);
             Statement statement = conn.createStatement()) {

            String sql = "INSERT INTO Guesses (CorrectShape, GuessShape) VALUES ('" + correctShape + "', '" + guessedShape + "')";
            statement.executeUpdate(sql);

        } catch (SQLException ex) {
            Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Displays the guessing history from the database in the ListView.
     */
    @FXML
    protected void onShowDBClick() {
        showGuesses();
    }
    private void showGuesses() {
        String dbFilePath = ".//ShapeGuess.accdb";
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

        listViewShows.getItems().clear();
        ObservableList<String> guesses = listViewShows.getItems();

        try (Connection conn = DriverManager.getConnection(databaseURL);
             Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery("SELECT GuessNum, CorrectShape, GuessShape FROM Guesses ORDER BY GuessNum")) {

            while (result.next()) {
                int GuessNum = result.getInt("GuessNum"); // Ensure this matches your column name
                String correctShape = result.getString("CorrectShape");
                String guessedShape = result.getString("GuessShape");
                // formatting and guessNum is used
                guesses.add(GuessNum + ", correct: " + correctShape + ", guessed: " + guessedShape);
            }

            listViewShows.setItems(guesses);

        } catch (SQLException ex) {
            Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Ensures the database file exists. Creates it if it doesn't.
     */
    private void Database() {
        String dbFilePath = ".//ShapeGuess.accdb";
        File dbFile = new File(dbFilePath);
        if (!dbFile.exists()) {
            try {
                System.out.println("The database file does not exist and will be created.");
                DatabaseBuilder.create(Database.FileFormat.V2010, dbFile);
                System.out.println("The database file has been created.");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
    /**
     * Creates or recreates the "Guesses" table in the database.
     * Is called when initialize or new game runs to reuse code
     */

    private void Table() {
        String databaseURL = "jdbc:ucanaccess://" + ".//ShapeGuess.accdb";
        try (Connection conn = DriverManager.getConnection(databaseURL);
             Statement statement = conn.createStatement()) {

            // Drop the table if it exists
            try {
                statement.executeUpdate("DROP TABLE Guesses");
            } catch (SQLException e) {
                System.out.println("Guesses table does not exist or could not be dropped. " + e.getMessage());
            }

            // create the table
            String sql = "CREATE TABLE Guesses (" + "GuessNum AUTOINCREMENT PRIMARY KEY, " + "CorrectShape NVARCHAR(255), " + "GuessShape NVARCHAR(255))";
            statement.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Animates the guessed shape according to whether the guess was correct or incorrect.
     * @param isCorrect Indicates if the guess was correct.
     * @param guessedShape The shape guessed by the user.
     */
    private void animateGuess(boolean isCorrect, String guessedShape) {
        Shape shapeToAnimate;
        if ("CIRCLE".equals(guessedShape.toUpperCase())) {
            shapeToAnimate = circle1;
        } else if ("RECTANGLE".equals(guessedShape.toUpperCase())) {
            shapeToAnimate = rectangle1;
        } else {
            // If the shape isn't recognized, do nothing
            return;
        }
        guessButton.setDisable(true);
        rectangle.setDisable(true);
        circle.setDisable(true);
        // Set the initial visibility of the shape
        shapeToAnimate.setVisible(true);


        Color targetColor;
        if (isCorrect) {
            targetColor = Color.GREEN;
        } else {
            targetColor = Color.RED;
        }

        FillTransition fillTransition = new FillTransition(Duration.seconds(1), shapeToAnimate, (Color) shapeToAnimate.getFill(), targetColor);
        fillTransition.setAutoReverse(true);
        fillTransition.setCycleCount(2);

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), shapeToAnimate);
        translateTransition.setByX(100);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(2);

        // Run both animations in parallel
        ParallelTransition parallelTransition = new ParallelTransition(shapeToAnimate, fillTransition, translateTransition);
        parallelTransition.setOnFinished(e -> {
            shapeToAnimate.setVisible(false);
            guessButton.setDisable(false);
            rectangle.setDisable(false);
            circle.setDisable(false);
        });
        parallelTransition.play();
    }

    /**
     * Handles the "New Game" button click event.
     * Resets the game for a new game.
     */
    @FXML
    protected void onNewGameClick() {
        // Reset and create table for new game
        guessCount.setText("0");
        correctGuessCount.setText("0");
        listViewShows.setItems(FXCollections.observableArrayList());

        // Drop and create new table to reset game data
        Table();
        System.out.println("Existing Guesses table dropped successfully.");
    }
    /**
     * Handles the "Exit" button click event.
     * Closes the application.
     */
    @FXML
    protected void onExitclick() {
        Platform.exit();
        System.out.println("Exit CLicked");
    }
    /**
     * Initializes the controller.
     * Sets up the game environment and database table.
     */

    public void initialize() {
        System.out.println("initialize called");
        rectangle.setToggleGroup(group);
        circle.setToggleGroup(group);
        guessButton.setDisable(true);

        //  create database if it doesnt exist
        Database();

        // create table at startup
        Table();
        System.out.println("Table created successfully.");

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            guessButton.setDisable(newValue == null);
        });


    }
}