package com.selection.naturalselection;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;
import java.util.Random;
//Объявление класса и переменных
public class Microb extends ImageView {
    private double energy;
    private double size;
    private double speed;
    private double interactionRadius;
    private static final Random random = new Random();
    private int foodCount = 0;
    private Simulation simulation;
    private double moveDirectionX;
    private double moveDirectionY;
    private int moveTicks = 0;

    private static final double SIMULATION_WIDTH = 690;
    private static final double SIMULATION_HEIGHT = 690; // Высота симуляции

    public Microb(double x, double y, double energy, Simulation simulation) {
        super(new Image(Microb.class.getResourceAsStream("/com/selection/naturalselection/micro.png")));
        this.simulation = simulation;
        this.setX(x);
        this.setY(y);
        this.energy = energy;

        this.speed = 0.5 + random.nextDouble();
        this.size = 20 + random.nextDouble();
        this.interactionRadius = 100 + random.nextInt(5);

        updateImageSize();
        applyRandomColor();
        setRandomDirection();
    }

    private void applyRandomColor() {
        double hue = random.nextDouble() * 360 - 180;
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(hue / 360.0);
        this.setEffect(colorAdjust);
    }

    public double getEnergy() {
        return energy;
    }

    public double getSpeed() {
        return speed + (speed / (4 * Math.sqrt(size)));    }

    public double getSize() {
        return size;
    }

    public double getInteractionRadius() {
        return interactionRadius;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setSize(double size) {
        this.size = size;
        updateImageSize();
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setInteractionRadius(double interactionRadius) {
        this.interactionRadius = interactionRadius;
    }

    public void incrementFoodCount() {
        foodCount++;
        if (foodCount > 3) {
            reproduce();
            foodCount = 0;
        }
    }

    public void moveTowards(Food food) {
        double dx = food.getCenterX() - this.getX();
        double dy = food.getCenterY() - this.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            dx /= distance;
            dy /= distance;
            double newX = this.getX() + dx * getSpeed();
            double newY = this.getY() + dy * getSpeed();

            if (newX >= 0 && newX <= SIMULATION_WIDTH - size) {
                this.setX(newX);
            }
            if (newY >= 0 && newY <= SIMULATION_HEIGHT - size) {
                this.setY(newY);
            }


            if (isAtEdge()) {
                setRandomDirection();
            }
        }
    }

    public void moveTowards(Microb target) {
        double dx = target.getX() - this.getX();
        double dy = target.getY() - this.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            dx /= distance;
            dy /= distance;
            double newX = this.getX() + dx * getSpeed();
            double newY = this.getY() + dy * getSpeed();

            if (newX >= 0 && newX <= SIMULATION_WIDTH - size) {
                this.setX(newX);
            }
            if (newY >= 0 && newY <= SIMULATION_HEIGHT - size) {
                this.setY(newY);
            }


            if (isAtEdge()) {
                setRandomDirection();
            }
        }
    }

    public void moveAwayFrom(Microb threat) {
        double dx = this.getX() - threat.getX();
        double dy = this.getY() - threat.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            dx /= distance;
            dy /= distance;
            double newX = this.getX() + dx * getSpeed();
            double newY = this.getY() + dy * getSpeed();

            if (newX >= 0 && newX <= SIMULATION_WIDTH - size) {
                this.setX(newX);
            }
            if (newY >= 0 && newY <= SIMULATION_HEIGHT - size) {
                this.setY(newY);
            }

            // Изменяем направление если клетка уперлась в границу экрана
            if (isAtEdge()) {
                setRandomDirection();
            }
        }
    }


    public void moveRandomly() {
        if (moveTicks <= 0 || isAtEdge()) {
            setRandomDirection();
        }

        Microb prey = findPrey();
        if (prey != null) {
            moveTowards(prey);
            if (isColliding(prey)) {
                resolveCollision(prey);
            }
        } else {
            Microb threat = findThreat();
            if (threat != null) {
                moveAwayFrom(threat);
            } else {
                Food food = findFood();
                if (food != null) {
                    moveTowards(food);
                    if (isInContactWithFood(food)) {
                        setEnergy(getEnergy() + 30); // Животное получает энергию
                        incrementFoodCount(); // Увеличиваем счетчик пищи
                        simulation.removeFood(food);
                    } else {
                        setEnergy(getEnergy() - 0.04); // Животное теряет энергию
                    }
                } else {
                    double newX = this.getX() + moveDirectionX * getSpeed();
                    double newY = this.getY() + moveDirectionY * getSpeed();

                    // Проверка, чтобы не выходить за край SimulationPane
                    if (newX >= 0 && newX <= SIMULATION_WIDTH - size) {
                        this.setX(newX);
                    }
                    if (newY >= 0 && newY <= SIMULATION_HEIGHT - size) {
                        this.setY(newY);
                    }

                    List<Microb> microbs = simulation.getAnimals();
                    for (Microb other : microbs) {
                        if (other != this && isColliding(newX, newY, other)) {
                            if (this.size > other.size * 1.4) {
                                continue;
                            }
                            setRandomDirection();
                            return;
                        }
                    }

                    moveTicks--;
                }
            }
        }
    }

    private Microb findPrey() {
        Microb closestPrey = null;
        double closestDistance = Double.MAX_VALUE;
        for (Microb other : simulation.getAnimals()) {
            if (other != this && this.size > other.size * 1.4) { // Проверка на 40% больше
                double distance = Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
                if (distance < this.interactionRadius && distance < closestDistance) {
                    closestDistance = distance;
                    closestPrey = other;
                }
            }
        }
        return closestPrey;
    }

    private Microb findThreat() {
        Microb closestThreat = null;
        double closestDistance = Double.MAX_VALUE;
        for (Microb other : simulation.getAnimals()) {
            if (other != this && other.size > this.size * 1.4) { // Проверка на 40% больше
                double distance = Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
                if (distance < this.interactionRadius && distance < closestDistance) {
                    closestDistance = distance;
                    closestThreat = other;
                }
            }
        }
        return closestThreat;
    }

    private Food findFood() {
        Food closestFood = null;
        double closestDistance = Double.MAX_VALUE;
        for (Food food : simulation.getFood()) {
            double distance = Math.sqrt(Math.pow(this.getX() - food.getCenterX(), 2) + Math.pow(this.getY() - food.getCenterY(), 2));
            if (distance < this.interactionRadius && distance < closestDistance) {
                closestDistance = distance;
                closestFood = food;
            }
        }
        return closestFood;
    }

    private boolean isAtEdge() {
        double x = this.getX();
        double y = this.getY();
        return x <= 0 || x >= SIMULATION_WIDTH - size || y <= 0 || y >= SIMULATION_HEIGHT - size;
    }

    private void setRandomDirection() {
        double angle;
        if (isAtEdge()) {
            angle = calculateBounceAngle();
        } else {
            angle = random.nextDouble() * 2 * Math.PI;
        }
        moveDirectionX = Math.cos(angle);
        moveDirectionY = Math.sin(angle);
        moveTicks = 100 + random.nextInt(100);
    }


    private double calculateBounceAngle() {
        double x = this.getX();
        double y = this.getY();
        double angle = random.nextDouble() * Math.PI;

        if (x <= 0 || x >= SIMULATION_WIDTH - size) {
            moveDirectionX = -moveDirectionX;
            return Math.atan2(moveDirectionY, moveDirectionX);
        }
        if (y <= 0 || y >= SIMULATION_HEIGHT - size) {
            moveDirectionY = -moveDirectionY;
            return Math.atan2(moveDirectionY, moveDirectionX);
        }
        return angle;
    }

    private void updateImageSize() {
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public boolean isInContactWithFood(Food food) {
        double dx = food.getCenterX() - this.getX();
        double dy = food.getCenterY() - this.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < this.size;
    }

    public boolean isColliding(Microb other) {
        double dx = other.getX() - this.getX();
        double dy = other.getY() - this.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.size + other.size) / 2;
    }

    public boolean isColliding(double newX, double newY, Microb other) {
        double dx = other.getX() - newX;
        double dy = other.getY() - newY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.size + other.size) / 2;
    }

    public void resolveCollision(Microb other) {
        if (this.size > other.size * 1.4) { // Проверка на 40% больше
            // Это животное съедает другое
            this.setEnergy(this.getEnergy() + other.getEnergy() / 10); // о
            this.incrementFoodCount();
            simulation.predationDeaths++; // Увеличиваем счетчик смертей от хищничества
            simulation.removeAnimal(other);
        } else {
            // Отталкивание
            double dx = other.getX() - this.getX();
            double dy = other.getY() - this.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                dx /= distance;
                dy /= distance;

                double newThisX = this.getX() - dx * this.getSpeed();
                double newThisY = this.getY() - dy * this.getSpeed();
                double newOtherX = other.getX() + dx * other.getSpeed();
                double newOtherY = other.getY() + dy * other.getSpeed();

                this.setX(Math.max(0, Math.min(SIMULATION_WIDTH - size, newThisX)));
                this.setY(Math.max(0, Math.min(SIMULATION_HEIGHT - size, newThisY)));
                other.setX(Math.max(0, Math.min(SIMULATION_WIDTH - size, newOtherX)));
                other.setY(Math.max(0, Math.min(SIMULATION_HEIGHT - size, newOtherY)));
            }
        }
    }

//Метод "рождения" нового животного с новыми характеристиками
    private void reproduce() {
        double newSpeed = this.speed * (0.6 + random.nextDouble() *0.7); // Новый животное получает скорость в диапазоне от 90% до 110% от родительской
        double newSize = this.size * (0.6 + random.nextDouble() * 0.7); // Новый животное получает размер в диапазоне от 60% до 90% от родительской
        double newInteractionRadius = this.interactionRadius * (0.9 + random.nextDouble() * 0.2); // Новый животное получает радиус в диапазоне от 90% до 130% от родительского

        Microb newMicrob = new Microb(this.getX(), this.getY(), this.energy / 2, simulation);
        newMicrob.setSpeed(newSpeed);
        newMicrob.setSize(newSize);
        newMicrob.setInteractionRadius(newInteractionRadius);
        

        simulation.addAnimal(newMicrob);
    }

}
