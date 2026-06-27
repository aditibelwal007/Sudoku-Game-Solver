package com.sudoku.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * JavaFX-friendly countdown/countup timer.
 * Fires on the FX thread — safe to bind directly to a Label.
 */
public class GameTimer {

    private final IntegerProperty seconds = new SimpleIntegerProperty(0);
    private Timeline timeline;

    public GameTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> seconds.set(seconds.get() + 1)));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start()  { timeline.play(); }
    public void pause()  { timeline.pause(); }
    public void resume() { timeline.play(); }

    public void reset() {
        timeline.stop();
        seconds.set(0);
    }

    public void stop() { timeline.stop(); }

    public int getSeconds() { return seconds.get(); }
    public IntegerProperty secondsProperty() { return seconds; }

    /** Format as M:SS */
    public static String format(int totalSeconds) {
        int m = totalSeconds / 60, s = totalSeconds % 60;
        return String.format("%d:%02d", m, s);
    }

    public String getFormatted() { return format(seconds.get()); }
}
