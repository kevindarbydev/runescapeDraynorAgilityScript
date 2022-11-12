import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;

@ScriptManifest(author = "Brotato", category = Category.AGILITY, description = "Draynor Agility Course From Level 10-20", name = "Draynor Agility", version = 3.0)
public final class BrotatoDraynor extends AbstractScript implements ChatListener {

    // Variables
    private final Area startArea = new Area(3100, 3283, 3108, 3273);
    private int marksCollected;
    private int oldAgilityExp = 0;
    private int lapsCompleted;
    private long startTime = 0;

    private void performLoopActions() {
        if (ScriptManager.getScriptManager().isRunning() && Client.isLoggedIn()) {
            handleDialogues();
            climbRoughWallToStart();
            checkIfWeFell();
            crossTightrope();
            secondTightRope();
            narrowWall();
            jumpWall();
            jumpGap();
            climbDown();
        }
    }

    @Override
    public void onStart() {
        doActionsOnStart();
    }
    private void doActionsOnStart() {
        startTime = System.currentTimeMillis();
        SkillTracker.start(Skill.AGILITY);
        oldAgilityExp = Skills.getExperience(Skill.AGILITY);
        Walking.setRunThreshold(nextInt(50, 92));
    }

    @Override
    public void onExit() {
        doActionsOnExit();
    }
    private void doActionsOnExit() {
        log(String.format("Gained agility xp: %d", (Skills.getExperience(Skill.AGILITY) - oldAgilityExp)));
        log("Runtime: " + getElapsedTimeAsString());
    }

    @Override
    public int onLoop() {
        performLoopActions();
        return nextInt(60, 75);
    }
    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("Laps Completed: " + lapsCompleted, 12, 300);
        g.drawString("Run time: " + getElapsedTimeAsString(), 12, 240);
        g.drawString("Agility Lvl: " + Skills.getBoostedLevels(Skill.AGILITY), 12, 220);
        g.drawString("Exp/Hr: " + SkillTracker.getGainedExperiencePerHour(Skill.AGILITY), 12, 200);
        g.drawString("Time Till Level: " + makeTimeString(SkillTracker.getTimeToLevel(Skill.AGILITY)), 12, 180);
        g.drawString("Marks Collected: " + marksCollected, 12, 160);
    }


   // Helper Functions
   @Override
   public void onPlayerMessage(Message msg) {
       handlePlayerMessage(msg);
   }

    @Override
    public void onMessage(Message msg) {
        handleGameMessages(msg);
    }

    private void handleDialogues() {
        if (Dialogues.inDialogue()) {
            for (int i = 0; i < 4; i++) {
                if (Dialogues.canContinue()) {
                    Dialogues.continueDialogue();
                    sleep(nextInt(500, 750));
                } else {
                    break;
                }
            }
        }
    }

    private String getElapsedTimeAsString() {
        return makeTimeString(getElapsedTime()); //make a formatted string from a long value
    }
    private long getElapsedTime() {
        return System.currentTimeMillis() - startTime; //return elapsed millis since start of script
    }
    private String makeTimeString(long ms) {
        final int seconds = (int) (ms / 1000) % 60;
        final int minutes = (int) ((ms / (1000 * 60)) % 60);
        final int hours = (int) ((ms / (1000 * 60 * 60)) % 24);
        final int days = (int) ((ms / (1000 * 60 * 60 * 24)) % 7);
        final int weeks = (int) (ms / (1000 * 60 * 60 * 24 * 7));
        if (weeks > 0) {
            return String.format("%02dw %03dd %02dh %02dm %02ds", weeks, days, hours, minutes, seconds);
        }
        if (weeks == 0 && days > 0) {
            return String.format("%03dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours > 0) {
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes > 0) {
            return String.format("%02dm %02ds", minutes, seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes == 0) {
            return String.format("%02ds", seconds);
        }
        if (weeks == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return String.format("%04dms", ms);
        }
        return "00";
    }

    private void handlePlayerMessage(Message msg) {
        log(String.format("%d, %d", msg.getTime(), msg.getTypeID()));
    }

    private void handleGameMessages(Message msg) {
        log(msg);    }

    private int nextInt(int lowValIncluded, int highValExcluded) { //get a random value between a range, high end is not included
        return ThreadLocalRandom.current().nextInt(lowValIncluded, highValExcluded);
    }

    private Player player() { //get the local player, less typing
        return getLocalPlayer();
    }

    private int playerX() { //get player x location
        return player().getX();
    }

    private int playerY() { //get player y location
        return player().getY();
    }

    private int playerZ() { //get player z location
        return player().getZ();
    }

    private boolean isMoving() { //true if player is moving
        return player().isMoving();
    }

    private boolean isAnimating() { //true if player is animating. NOT all animations in game results in a true return value
        return player().isAnimating(); //eg. walking on a agility log (arms stretched to the sides), does not return true here
    }

    private boolean atStartArea() { //area before the agility log
        return startArea.contains(player());
    }

    private void checkForMarks() {
        if (Inventory.isFull()) {
            log("Full inventory -- please fix");
        }
        final GroundItem mark = GroundItems.closest("Mark of Grace");
        if (mark != null) {
            mark.interact("Take");
            sleepUntil(() -> false, nextInt(500,1000));
            marksCollected++;
        }
    }
    private void checkIfWeFell(){
        if (playerZ() == 0 && !startArea.contains(player())){
            log("We fell ... attempting to restart course...");
            Walking.walk(startArea);
            sleepUntil(
                    () -> (player().distance(Walking.getDestination()) <= nextInt(3, 5)),
                    () -> isMoving(),
                    nextInt(3600, 4000), //timer duration
                    nextInt(320, 480)); //every time, poll timer is up, check reset condition. If true, then reset timer duration
            climbRoughWallToStart();
        }
    }

        // Obstacle Methods

    private void climbRoughWallToStart() {
        if (atStartArea()) {
            final GameObject rWall = GameObjects.closest(11404);
            if (rWall != null) {
                if (rWall.distance() > 9) {
                    Walking.walk(rWall);
                    sleepUntil(() -> isMoving(), nextInt(500, 1000));
                }
                sleepUntil(
                        () -> (player().distance(Walking.getDestination()) <= nextInt(3, 5)),
                        () -> isMoving(),
                        nextInt(3600, 4000),
                        nextInt(320, 480));
                if (rWall.interact()) { //click the wall to climb it and start course
                    log("Found wall -- climbing...");
                    sleepUntil(
                            () -> playerZ() == 3,
                            () -> isMoving(),
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );
                    log("Wall climbed -- moving to next method...");
                }
            }
        }
    }

    private void crossTightrope() {
        if (playerZ() == 3 && playerY() == 3279 && playerX() == 3102) {
            checkForMarks();
            log("In second method... looking for rope");
            final GameObject tRope = GameObjects.closest(11405);
            if (tRope != null) {
                if (tRope.interact()) {
                    log("Found rope -- crossing...");

                    sleepUntil(() -> isMoving(), nextInt(500, 1000));
                    sleepUntil(
                            () -> playerX() == 3090,
                            () -> isMoving(),
                            nextInt(1000, 2000),
                            nextInt(320, 480)
                    );

                }
            }
                 }
        }

        private void secondTightRope() {
            if (playerZ() == 3 && ((playerX() == 3091) || (playerX() == 3090) || playerX() == 3089)) {
                log("First tightrope succeeded...");
                checkForMarks();
                final GameObject secondRope = GameObjects.closest(11406);

                if (secondRope != null) {
                    log("Attempting to cross second tightrope...");
                    if (secondRope.interact()) {

                        sleepUntil(() -> isMoving(), nextInt(500, 1000));
                        sleepUntil(
                                () -> playerY() == 3266,
                                () -> isMoving(),
                                nextInt(1000, 2000),
                                nextInt(320, 480)
                        );
                    }
                }
            }
        }

        private void narrowWall() {
            if (playerZ() == 3 && playerY() == 3266 && playerX() == 3092) {
                 log("Second tightrope succeeded...");
                 checkForMarks();
                 final GameObject nWall = GameObjects.closest(11430);

                    if (nWall != null) {
                        log("Attempting to climb narrow wall");

                        if (nWall.interact()) {

                            sleepUntil(() -> isMoving(), nextInt(500, 1000));
                            sleepUntil(
                                    () -> playerX() == 3088,
                                    () -> isMoving(),
                                    nextInt(1000, 2000),
                                    nextInt(320, 480)
                            );
                        }
                    }
                }
            }


        private void jumpWall () {
            if (playerZ() == 3 && playerX() == 3088 && playerY() == 3261) {
                log("Wall climb succeeded...");
                checkForMarks();
                    final GameObject jumpWall = GameObjects.closest(11630);
                    if (jumpWall != null) {
                        log("Attempting to jump up wall...");
                        if (jumpWall.interact()) {
                            sleepUntil(() -> isMoving(), nextInt(500, 1000));
                            sleepUntil(() -> playerY() == 3255, nextInt(3000, 3500));
                        }
                    }
                }

        }

        private void jumpGap() {
            if (playerZ() == 3 &&  playerY() == 3255 && playerX() == 3088) {
                log("Wall Jump succeeded...");
                checkForMarks();
                final GameObject jumpGap = GameObjects.closest(11631);

                if (jumpGap != null) {
                    log("Attempting to jump gap...");
                    if (jumpGap.interact()) {
                        
                        sleepUntil(() -> isMoving(), nextInt(500, 1000));
                        sleepUntil(
                                () -> playerX() == 3096, //we succeeded
                                () -> (isMoving() || isAnimating()),
                                nextInt(1000, 2000),
                                nextInt(320, 480)
                        );
                    }
                }
            }
        }

        private void climbDown() {
            if (playerZ() == 3 && playerX() == 3096 && playerY() == 3256) {
                log("success, on last obstacle");
                checkForMarks();
                final GameObject endCourse = GameObjects.closest(11632);
                if (endCourse != null) {
                    if (endCourse.interact()) {
                        sleepUntil(() -> isMoving(), nextInt(500, 1000));
                        sleepUntil(() -> playerZ() == 0, nextInt(500,1000)//we are finished course Z is 0

                        );
                        log("Lap successfully completed!");
                        lapsCompleted++;
                    }
                }
            }
        }
    }
