package etruaruta.controllers;

import etruaruta.Main;
import etruaruta.models.*;

import java.util.ArrayList;

/**
 * This is the controller class that processes all
 * the game logic and algorithms.
 *
 * @author Adil Bhayani <abha808@aucklanduni.ac.nz>
 * @author Sakayan Sitsabesan <ssit662@aucklanduni.ac.nz>
 * @version 0.5.0
 */

public class Game{

    private boolean isFinished = false, countingDown = true;
    private int timeElapsed = 0, hiScore = 0, countDown = 91;

    public General[] generals;
    public Ball[] balls;
    private Ball ball;
    public ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();
    public SpeedUp speedUp;
    public ArrayList<AIController> AIs = new ArrayList<AIController>();
    private int ballHoldAngle = 0;
    public ArrayList<Marker> markers = new ArrayList<>();
    private ArrowPointer arrow = new ArrowPointer();
    private boolean heldByFirstPaddle = true;
    private boolean skillsReady = true;
    public boolean skillUsedAtleastOnce = false;
    public int previousAngle = 0;
    public int arrowsIndex = -1;

    private int[] deadPos;

    public String[] generalsMovement = {"", "", "", ""};

    public Game(Ball[] balls, General generalA, General generalB, General generalC, General generalD) {
        this.balls = balls;
        this.generals = new General[4];
        this.generals[0] = generalA;
        this.generals[1] = generalB;
        this.generals[2] = generalC;
        this.generals[3] = generalD;

        this.AIs.add(new AIController());
        this.AIs.get(0).setGeneral(generalA);

        this.AIs.add(new AIController());
        this.AIs.get(1).setGeneral(generalB);

        this.AIs.add(new AIController());
        this.AIs.get(2).setGeneral(generalC);

        this.AIs.add(new AIController());
        this.AIs.get(3).setGeneral(generalD);

        this.deadPos = new int[4];

    }

    public void countdownTick() {
        if (countDown > 0) countDown--;
        else countingDown = false;
    }

    public boolean isCountingDown() {
        return countingDown;
    }

    public void tick(){
            timeElapsed++;
            boolean generalHit = false;
            int deadCount = 0;
            executeActions();

            for (int a = 0; a < balls.length; a++) {
                ball = balls[a];
                boolean ballHit = false;
                if (!ball.getHitLastTick() && ball.getCollisionCounter() <= 0) {
                    for (int i = 0; i < powerUps.size(); i++) {
                        if (!ballHit && (!powerUps.get(i).isHit())) {
                            if (objectCollision(powerUps.get(i), ballHit, false)) {
                                if (powerUps.get(i).getPowerUpName().equals("Speed Up")) {
                                    if (!ball.getSpedUp()) {
                                        powerUps.get(i).setHit(true);
                                        powerUps.get(i).activateEffect(ball, generals);
                                        SoundManager.playSpeedUp();
                                        ball.setSpedUp(true);
                                    }
                                }else if (powerUps.get(i).getPowerUpName().equals("Paddle Size Up")){
                                    boolean activateSizeIncrease = false;
                                    for (int j = 0; j < generals.length;j++){
                                        if (!generals[j].isDead() && !generals[j].paddle.getSizeIncreased()){
                                            activateSizeIncrease = true;
                                        }
                                    }
                                    if (activateSizeIncrease){
                                        powerUps.get(i).setHit(true);
                                        powerUps.get(i).activateEffect(ball, generals);
                                    }
                                }
                            }
                        }
                    }
                    outerLoop:
                    for (int i = 0; i < generals.length; i++) {
                        if (!ballHit && (!generals[i].isDead())){
                            if (ball.getWillBeHeld() && i == 0){
                                ballHit = objectCollision(generals[i].paddle,ballHit,false);//Collision with general 0's first paddle
                                if (ballHit){
                                    holdBall(true);
                                    arrowsIndex += 1;
                                    previousAngle = 0;
                                }
                            }else {
                                ballHit = objectCollision(generals[i].paddle, ballHit);
                            }
                        }
                        if (!ballHit && (!generals[i].isDead()) && Main.numberOfPaddles == 2){
                            if (ball.getWillBeHeld() && i == 0){
                                ballHit = objectCollision(generals[i].paddleFollower,ballHit,false);//Collision with general 0's second paddle
                                if (ballHit){
                                    holdBall(false);
                                    arrowsIndex += 1;
                                    previousAngle = 0;
                                }
                            }else {
                                ballHit = objectCollision(generals[i].paddleFollower, ballHit);
                            }
                        }
                        if (!ballHit && (!generals[i].isDead())) {
                            ballHit = objectCollision(generals[i], ballHit);
                            if (ballHit){
                                generals[i].killGeneral();
                                generalHit = true;
                                SoundManager.playGeneralDeath();

                                if (isSinglePlayer() && i != 0 && skillsReady & skillUsedAtleastOnce){
                                    resetSkills();
                                }
                                break outerLoop;
                            }
                        }
                        if (!ballHit) {
                            for (int j = 0; j < generals[i].wall.length; j++) {
                                for (int k = 0; k < generals[i].wall[j].length; k++) {
                                    if (!generals[i].wall[j][k].isDestroyed()){
                                        if (!ballHit) {
                                            ballHit = objectCollision(generals[i].wall[j][k], ballHit);
                                        }
                                        if (ballHit) {
                                            if (!ball.isExplosive()) {
                                                generals[i].wall[j][k].destroyBrick();
                                            } else{
                                                explosion(i,j,ball); //Cause the explosion.
                                                checkAllExploded(balls);
                                            }
                                            break outerLoop;
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                if (ballHit && !generalHit) SoundManager.playCollision();

                if (!ballHit) {
                    ball.processBall();
                    ball.setHitLastTick(false);
                    ball.decrementCounter();
                }
                ball.checkReduceSpeed();
            }
        for (int i = 0; i < generals.length; i++) {
            if (generals[i].isDead()) deadCount++;
        }

        if (deadCount + 1 == generals.length) {
            for (int i = 0; i < generals.length; i++) {
                if (!generals[i].isDead()) {
                    generals[i].setWon();
                    isFinished = true;
                }
            }
        }

        for (int i = 0; i < AIs.size();i++){
            if (!generals[i].isDead()) {
                AIs.get(i).movePaddle(balls);
            }else{
                for (int j = 0; j < markers.size();j++){
                    if (markers.get(j).getPos() == i){
                        AIs.get(i).moveMarker(markers.get(j), powerUps);
                    }
                }
            }
        }

            if (deadCount > 0 && markers.size() < deadCount ){
                for (int i = 0; i < deadPos.length;i++) {
                    if (deadPos[i] != 1 && generals[i].isDead()) {
                        this.markers.add(new Marker());
                        this.markers.get(this.markers.size() - 1).setPos(i);
                        //System.out.println(this.markers.get(this.markers.size() - 1).getPos());
                        deadPos[i] = 1;
                        break;
                    }
                }
            }

            if (timeElapsed > 3600) {
                isFinished = true;
                hiScore = Math.max(Math.max(generals[0].wallCount(), generals[1].wallCount()), Math.max(generals[2].wallCount(), generals[3].wallCount()));
                for (int i = 0; i < generals.length; i++) {
                    if (hiScore == generals[i].wallCount()) generals[i].setWon();
                }
            }

            if (timeElapsed % 600 == 0){
                generatePowerUp();
            }

            for (int i = 0; i < generals.length;i++){
                generals[i].paddle.checkDecreaseWidth();
                if (Main.numberOfPaddles == 2)generals[i].paddleFollower.checkDecreaseWidth();
            }

            for (int i = 0; i < generals.length;i++){
                if (deadPos[i] == 1 && generals[i].isDead()){
                    for (int j = 0; j < markers.size(); j++){
                        if (markers.get(j).getPos() == i){
                            markers.get(j).decrementReadyCounter();
                        }
                    }
                }
            }
            if (heldByFirstPaddle){
                calculateArrowPivots(true);
            }else{
                calculateArrowPivots(false);
            }

            boolean ballStillHeld = true;
            if (heldByFirstPaddle) {
                if (!generals[0].paddle.checkStillHoldingBall(balls, arrow)) {
                    ballStillHeld = false;
                }
            }else{
                if (!generals[0].paddleFollower.checkStillHoldingBall(balls, arrow)) {
                    ballStillHeld = false;
                }
            }
            if (!ballStillHeld){
                skillsReady = true;
            }

    }


    private boolean objectCollision (IObject object, boolean ballHit) {
        for (int x = object.calcXPos(); x <= (object.calcXPos() + object.getWidth()); x++) {
            for (int y = object.calcYPos(); y <= (object.calcYPos() + object.getHeight()); y++) {
                if (x == object.calcXPos() || y == object.calcYPos() || x == (object.calcXPos() + object.getWidth()) || y == (object.calcYPos() + object.getHeight())) {
                    if (inBallPath(x, y)) {
                        if (x == object.calcXPos()) {
                            if (ball.getXVelocity() > 0) {
                                ball.setXVelocity(-ball.getXVelocity());
                                ball.setHitLastTick(true);
                                //System.out.println(ball.getXPos());
                               // ball.setXPos(ball.getXPos() + ball.getXVelocity());
                                //ball.setYPos(ball.getYPos() + ball.getYVelocity());
                                System.out.println("A, X: " + ball.getXPos() + " Y: " + ball.getYPos() + " Y Velocity: " + ball.getYVelocity() + " X Velocity: " + ball.getXVelocity());
                                return true;
                            }
                        } else if (y == object.calcYPos()) {
                            if (ball.getYVelocity() > 0) {
                                ball.setYVelocity(-ball.getYVelocity());
                                ball.setXPos(ball.getXPos() + ball.getXVelocity());
                                //ball.setYPos(ball.getYPos() + ball.getYVelocity());
                                System.out.println("B, X: " + ball.getXPos() + " Y: " + ball.getYPos() + " Y Velocity: " + ball.getYVelocity() + " X Velocity: " + ball.getXVelocity());
                                ball.setHitLastTick(true);
                                return true;
                            }
                        } else if (x == (object.calcXPos() + object.getWidth())) {
                            if (ball.getXVelocity() < 0) {
                                ball.setXVelocity(-ball.getXVelocity());
                                ball.setHitLastTick(true);
                                //ball.setXPos(ball.getXPos() - ball.getXVelocity());
                                //ball.setYPos(ball.getYPos() - ball.getYVelocity());
                                System.out.println("C, X: " + ball.getXPos() + " Y: " + ball.getYPos() + " Y Velocity: " + ball.getYVelocity() + " X Velocity: " + ball.getXVelocity());
                                return true;
                            }
                        } else if (y == (object.calcYPos() + object.getHeight())) {
                            if (ball.getYVelocity() < 0) {
                                ball.setYVelocity(-ball.getYVelocity());
                                //ball.setXPos(ball.getXPos() - ball.getXVelocity());
                                //ball.setYPos(ball.getYPos() - ball.getYVelocity());
                                System.out.println("D, X: " + ball.getXPos() + " Y: " + ball.getYPos() + " Y Velocity: " + ball.getYVelocity() + " X Velocity: " + ball.getXVelocity());
                                ball.setHitLastTick(true);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return ballHit;
    }

    private boolean objectCollision (IObject object, boolean ballHit, boolean bounce) {
        for (int x = object.calcXPos(); x < (object.calcXPos() + object.getWidth()); x++) {
            for (int y = object.calcYPos(); y < (object.calcYPos() + object.getHeight()); y++) {
                if (x == object.calcXPos() || y == object.calcYPos() || x == (object.calcXPos() + object.getWidth()) || y == (object.calcYPos() + object.getHeight())) {
                    if (inBallPath(x, y)) {
                        if (x == object.calcXPos()) {
                            return true;
                        } else if (y == object.calcYPos()) {
                            return true;
                        } else if (x == (object.calcXPos() + object.getWidth())) {
                            return true;
                        } else if (y == (object.calcYPos() + object.getHeight())) {
                            return true;
                        }
                    }
                }
            }
        }
        return ballHit;
    }

    /* Bresenham Line Drawing Algorithm */
    private boolean inBallPath (int xCord, int yCord) {
        int x1 = ball.getXPos();
        int x2 = ball.getXPos() + ball.getWidth();
        int y1 = ball.getYPos();
        int y2 = ball.getYPos() + ball.getHeight();
        int x, y, dx, dy, p, end;

        dx = Math.abs(x1 - x2);
        dy = Math.abs(y1 - y2);
        p = 2 * dy - dx;
        if(x1 > x2) {
            x = x2;
            y = y2;
            end = x1;
        }
        else {
            x = x1;
            y = y1;
            end = x2;
        }
        if ((x == xCord) && (y == yCord)) return true;
        while(x < end) {
            x = x + 1;
            if(p < 0) {
                p = p + 2 * dy;
            }
            else {
                y = y + 1;
                p = p + 2 * (dy - dx);
            }
            if ((x == xCord) && (y == yCord)) return true;
        }
        return false;
    }

    public boolean isFinished(){
        return isFinished;
    }

    public String getTimeRemaining() {
        int time = (120 - (timeElapsed / 30));
        if (time == 120) return "2:00";
        if (time > 59) return "1:" + String.format("%02d",time-60);
        else return "0:" + String.format("%02d",time);
    }

    public String getCountdownRemaining() {
        int time = (countDown / 30);
        return "0:" + String.format("%02d",time);
    }

    public void setFinished(){
        isFinished = true;
    }


    public void generatePowerUp(){
        int option = (int)(Math.random() * 2);
        if (option == 0){
            this.powerUps.add(new PaddleSizeUp());
        } else if (option == 1){
            this.powerUps.add(new SpeedUp());
        }

        double xPos = Math.random() * 424 + 350;
        double yPos = Math.random() * 608 + 50;
        powerUps.get(powerUps.size()-1).setPos((int) xPos, (int) yPos);
    }

    public void generatePowerUp(int xPos, int yPos){
        int option = (int)(Math.random() * 2);
        //System.out.println(option);
        if (option == 0){
            this.powerUps.add(new PaddleSizeUp());
        } else if (option == 1){
            this.powerUps.add(new SpeedUp());
        }
        powerUps.get(powerUps.size()-1).setPos((int) xPos, (int) yPos);
    }

    public void executeActions(){
            for (int i = 0; i < generalsMovement.length; i++) {
                //System.out.println(generalsMovement[i]);
                switch (generalsMovement[i]) {
                    case "left":
                        if(!generals[i].isDead()) {
                            generals[i].paddle.moveLeft();
                            if (Main.numberOfPaddles > 1.50) generals[i].paddleFollower.moveRight();
                        }else{
                            for (int j = 0; j < markers.size();j++){
                                if (markers.get(j).getPos() == i){
                                    markers.get(j).moveLeft();
                                }
                            }
                        }
                        break;
                    case "right":
                        if(!generals[i].isDead()) {
                            generals[i].paddle.moveRight();
                            if (Main.numberOfPaddles > 1.50) generals[i].paddleFollower.moveLeft();
                        }else{
                            for (int j = 0; j < markers.size();j++){
                                if (markers.get(j).getPos() == i){
                                    markers.get(j).moveRight();
                                }
                            }
                        }
                        break;
                    case "up":
                        if(generals[i].isDead()) {
                            for (int j = 0; j < markers.size();j++){
                                if (markers.get(j).getPos() == i){
                                    markers.get(j).moveUp();
                                }
                            }
                        }
                        break;
                    case "down":
                        if(generals[i].isDead()) {
                            for (int j = 0; j < markers.size();j++){
                                if (markers.get(j).getPos() == i){
                                    markers.get(j).moveDown();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        private void explosion(int i, int j, Ball ball){
            for (int k = 0; k < generals[i].wall[j].length; k++){
                if (!generals[i].wall[j][k].isDestroyed()){
                    generals[i].wall[j][k].destroyBrick();
                }
            }
            ball.setUnexplosive();
            SoundManager.playExplosion();
        }

        private void checkAllExploded(Ball[] balls){
            boolean allExploded = true;
            for (int i = 0; i < balls.length; i ++){
                if (balls[i].isExplosive()){
                    allExploded = false;
                }
            }
            if (allExploded){
                skillsReady = true;
            }
        }

        private void holdBall(boolean firstPaddle){
            ball.setBallHeld();
            if (firstPaddle) {
                generals[0].paddle.setHoldingBall(true);
                ball.setXPos(generals[0].paddle.calcXPos() + generals[0].paddle.getWidth() / 2 - ball.getWidth() / 2);
                ball.setYPos(generals[0].paddle.calcYPos() + generals[0].paddle.getHeight());
                heldByFirstPaddle = true;
            }else{
                generals[0].paddleFollower.setHoldingBall(true);
                ball.setXPos(generals[0].paddleFollower.calcXPos() + generals[0].paddleFollower.getWidth() / 2 - ball.getWidth() / 2);
                ball.setYPos(generals[0].paddleFollower.calcYPos() + generals[0].paddleFollower.getHeight());
                heldByFirstPaddle = false;
            }
            for (int i = 0; i < balls.length; i++){
                balls[i].setWillBeHeld(false);
            }
        }

        public void decreaseBallHoldAngle(){
            arrow.decreaseAngle();
        }

        public void increaseBallHoldAngle(){
            arrow.increaseAngle();
        }

        public int getBallHoldAngle(){
            return arrow.getAngle();
        }

        public void calculateArrowPivots(boolean firstPaddle){
            if (firstPaddle){
                arrow.calcPivots(generals[0].paddle, ball);
            }else{
                arrow.calcPivots(generals[0].paddleFollower, ball);
            }
        }

        public void calculateArrowPosition(boolean firstPaddle){
            if (firstPaddle) {
                arrow.calcPos(generals[0].paddle, ball);
            }else{
                arrow.calcPos(generals[0].paddleFollower, ball);
                System.out.println(generals[0].paddleFollower.calcXPos());
            }
        }

        public int getArrowXPosition(){
            return arrow.getxPos();
        }

        public int getArrowYPosition(){
            return arrow.getyPos();
        }

        public int getArrowXPivot(){
            return arrow.getxPivot();
        }

        public int getArrowYPivot(){
            return arrow.getyPivot();
        }

        public boolean isSinglePlayer(){
            if (Main.gameMode == 2 || Main.gameMode == 4 || Main.gameMode == 6){
                return true;
            }
            return false;
        }

        private void resetSkills(){
            generals[0].resetAllSkills();
        }

    }