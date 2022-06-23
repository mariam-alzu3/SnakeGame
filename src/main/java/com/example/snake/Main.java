package com.example.snake;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main extends Application {

    private static final int width = 500;
    private static final int height = 600;
    private static final int rows = 20;
    private static final int columns = 20;
    private static final int square = width / rows;

    private static final int right = 0;
    private static final int left = 1;
    private static final int up = 2;
    private static final int down = 3;
    private int currentDirection;

    private GraphicsContext gc;
    private final List<Point> snake = new ArrayList<>();
    private Point snakeHead;

    private Image appleImage;
    private Point food;
    private int foodX;
    private int foodY;

    private boolean gameOver;
    private int score = 0;
    int highScore;


    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Snake Game");
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        gc = canvas.getGraphicsContext2D();


        for (int i = 0; i < 3; i++) {
            snake.add(new Point(10, rows / 2));
        }
        snakeHead = snake.get(0);

        generateFood();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(130), e -> {
            try {
                run(gc);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // move snake with keyboard
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode code = event.getCode();
                if (code == KeyCode.RIGHT || code == KeyCode.D) {
                    if (currentDirection != left) {
                        currentDirection = right;
                    }

                } else if (code == KeyCode.LEFT || code == KeyCode.A) {
                    if (currentDirection != right) {
                        currentDirection = left;
                    }

                } else if (code == KeyCode.UP || code == KeyCode.W) {
                    if (currentDirection != down) {
                        currentDirection = up;
                    }

                } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                    if (currentDirection != up) {
                        currentDirection = down;
                    }

                } else if (code == KeyCode.R) {
                    timeline.stop();
                    restart();
                    try {
                        start(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void run(GraphicsContext gc) throws IOException {

        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("ArcadeClassic", 50));
            gc.fillText("GAME OVER", 140, 290);


            File file = new File("HighScore.txt");
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextInt())
            {
                highScore = scanner.nextInt();
            }

            if (score > highScore) {
                PrintWriter writer = new PrintWriter("HighScore.txt");
                writer.println(score);
                writer.close();

                gc.setFill(Color.YELLOW);
                gc.setFont(new Font("Digital-7", 20));
                gc.fillText("High Score: " + score, 200, 320);

            } else {
                gc.setFill(Color.YELLOW);
                gc.setFont(new Font("Digital-7", 20));
                gc.fillText("High Score: " + highScore, 200, 320);
                return;
            }
        }

        draw(gc);

        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }

        switch (currentDirection) {
            case right:
                moveRight();
                break;

            case left:
                moveLeft();
                break;

            case up:
                moveUp();
                break;

            case down:
                moveDown();
                break;
        }

        checkCollisions();
        eatFood();
    }


    // snake movement
    private void moveRight() {
        snakeHead.x++;
    }

    private void moveLeft() {
        snakeHead.x--;
    }

    private void moveUp() {
        snakeHead.y--;
    }

    private void moveDown() {
        snakeHead.y++;
    }

    //random spot for food
    private void generateFood() {
        foodX = (int) (Math.random() * rows);
        foodY = (int) (Math.random() * columns);

        appleImage = new Image("file:/Users/mariam/Desktop/1703-p3r.s0n@l/Coding Projects/Java/Snake/src/main/resources/apple.png");

        food = new Point(foodX, foodY);
    }

    private void draw(GraphicsContext gc) {
        //background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width * square, height * square);

        //snake
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(snakeHead.getX() * square, snakeHead.getY() * square, square - 1, square - 1);

        for (int i = 1; i < snake.size(); i++) {
            gc.fillRect(snake.get(i).getX() * square, snake.get(i).getY() * square, square - 1, square - 1);
        }

        //food
        gc.drawImage(appleImage, foodX * square, foodY * square, square, square);

        //score
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(new Font("Digital-7", 20));
        gc.fillText("Score: " + score, 10, 23);
    }

    private void eatFood() {
        //when they collide move food to a new location by calling the generateFood method
        if (snakeHead.getX() == food.getX() && snakeHead.getY() == food.getY()) {
            generateFood();
            //increase snake length
            snake.add(new Point((int) snakeHead.getX(), (int) snakeHead.getY()));
            score++;
        }
    }

    private void checkCollisions() {
        //border death
        if (snakeHead.x < 0 || snakeHead.y < 0 || snakeHead.x * square >= width || snakeHead.y * square >= height) {
            gameOver = true;
        }

        //collision with itself
        for (int i = 1; i < snake.size(); i++) {
            if (snakeHead.x == snake.get(i).getX() && snakeHead.y == snake.get(i).getY()) {
                gameOver = true;
                break;
            }
        }
    }

    private void restart() {
        gameOver = false;
        score = 0;
        snake.clear();
    }

    public static void main(String[] args) {
        launch();
    }
}