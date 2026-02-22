package com.venomgrave.hexvg.task;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Bukkit;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


public class TaskQueue {

    private static final int MAX_PER_TICK = 256;

    private final HexVGPlanets plugin;
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private int taskId = -1;

    public TaskQueue(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    public void start() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::processTick, 1L, 1L).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        queue.clear();
    }

    public void add(Runnable task) {
        queue.offer(task);
    }

    private void processTick() {
        int processed = 0;
        Runnable task;
        while (processed < MAX_PER_TICK && (task = queue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "TaskQueue error", e);
            }
            processed++;
        }
    }

    public int size() {
        return queue.size();
    }
}