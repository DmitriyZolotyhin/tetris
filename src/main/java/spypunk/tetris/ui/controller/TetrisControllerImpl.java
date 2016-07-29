/*
 * Copyright © 2016 spypunk <spypunk@gmail.com>
 *
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package spypunk.tetris.ui.controller;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;

import spypunk.tetris.factory.TetrisFactory;
import spypunk.tetris.model.Movement;
import spypunk.tetris.model.Tetris;
import spypunk.tetris.service.TetrisService;
import spypunk.tetris.ui.view.TetrisRenderer;

@Singleton
public class TetrisControllerImpl implements TetrisController {

    private static final int RENDER_PERIOD = 1000 / 60;

    private static final Map<Integer, Movement> MOVEMENTS = Maps.newHashMap();

    static {
        MOVEMENTS.put(KeyEvent.VK_DOWN, Movement.DOWN);
        MOVEMENTS.put(KeyEvent.VK_RIGHT, Movement.RIGHT);
        MOVEMENTS.put(KeyEvent.VK_LEFT, Movement.LEFT);
        MOVEMENTS.put(KeyEvent.VK_UP, Movement.ROTATE_CW);
    }

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    @Inject
    private TetrisRenderer tetrisRenderer;

    @Inject
    private TetrisService tetrisService;

    @Inject
    private TetrisFactory tetrisFactory;

    private volatile boolean newGame = true;

    private volatile Movement movement;

    private volatile boolean pauseTriggered;

    private Future<?> loopThread;

    private Tetris tetris;

    @Override
    public void start() {
        tetrisRenderer.start();

        loopThread = scheduledExecutorService.scheduleAtFixedRate(() -> onGameLoop(), 0, RENDER_PERIOD,
            TimeUnit.MILLISECONDS);
    }

    @Override
    public void onWindowClosed() {
        loopThread.cancel(false);
        scheduledExecutorService.shutdown();
    }

    @Override
    public void onKeyPressed(int keyCode) {
        if (KeyEvent.VK_SPACE == keyCode) {
            newGame = true;
        } else if (KeyEvent.VK_P == keyCode) {
            pauseTriggered = true;
        }

        movement = MOVEMENTS.get(keyCode);
    }

    private void onGameLoop() {
        if (newGame) {
            handleNewGame();
        } else if (tetris == null) {
            return;
        } else {
            handleMovement();
            handlePause();

            tetrisService.update(tetris);
        }

        tetrisRenderer.render(tetris);
    }

    private void handleNewGame() {
        tetris = tetrisFactory.createTetris();
        newGame = false;
    }

    private void handleMovement() {
        tetris.setMovement(Optional.ofNullable(movement));
        movement = null;
    }

    private void handlePause() {
        if (pauseTriggered) {
            tetris.setPaused(!tetris.isPaused());
            pauseTriggered = false;
        }
    }
}
