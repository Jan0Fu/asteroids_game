package asteroids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AsteroidsApplication extends Application {
    
    public static int WIDTH = 500;
    public static int HEIGHT = 360;
    Pane pane = new Pane();
    
    @Override
    public void start(Stage stage) throws Exception {
        Text text = new Text(10, 20, "Points: 0");
        pane.setPrefSize(WIDTH, HEIGHT);
        pane.getChildren().add(text);
        
        AtomicInteger points = new AtomicInteger();
        
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        List<Character> projectiles = new ArrayList<>();
        List<Character> asteroids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }
        
        pane.getChildren().add(ship.getCharacter());
        for (Character asteroid: asteroids) {
            pane.getChildren().add(asteroid.getCharacter());
        }
        
        Scene scene = new Scene(pane);
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        
        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });
        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });
        
        Point2D movement = new Point2D(1, 0);
        
        projectiles.forEach(projectile -> {
            asteroids.forEach(asteroid -> {
                if(projectile.collide(asteroid)) {
                    projectile.setAlive(false);
                    asteroid.setAlive(false);
                }
            });
        });
        
        projectiles.stream()
            .filter(projectile -> !projectile.isAlive())
            .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
        projectiles.removeAll(projectiles.stream()
                                .filter(projectile -> !projectile.isAlive())
                                .collect(Collectors.toList()));
        
        asteroids.stream()
                .filter(asteroid -> !asteroid.isAlive())
                .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
        asteroids.removeAll(asteroids.stream()
                                    .filter(asteroid -> !asteroid.isAlive())
                                    .collect(Collectors.toList()));
        
        
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                text.setText("Points: " + points.incrementAndGet());
                
                if (pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }
                if (pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }
                if (pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    ship.accelerate();
                }
                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 3) {
                    Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                    projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                    projectiles.add(projectile);
                    
                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                    
                    pane.getChildren().add(projectile.getCharacter());
                }
                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());
                
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if (projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                        }
                    });
                    if(!projectile.isAlive()) {
                        text.setText("Points: " + points.addAndGet(1000));
                    }

                });
                
                kill(projectiles);
                kill(asteroids);
                
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                    stop();
                    Text gameOver = new Text(HEIGHT / 2, WIDTH / 2, "GAME OVER");
                    pane.getChildren().add(gameOver);
                    }
                });
                if (Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }
                
            }
        }.start();
        
        stage.setTitle("Asteroids!");
        stage.setScene(scene);
        stage.show();
        
    }
    public void kill(List<Character> characters) {
        characters.stream()
                .filter(character -> !character.isAlive())
                .forEach(character -> pane.getChildren().remove(character.getCharacter()));
        characters.removeAll(characters.stream()
                .filter(character -> !character.isAlive())
                .collect(Collectors.toList())
        );
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static int partsCompleted() {
        // State how many parts you have completed using the return value of this method
        return 4;
    }

}
