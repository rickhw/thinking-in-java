package rpg.systems.commands;

import java.util.*;

/**
 * Queue for managing command execution with support for priorities and history.
 * Provides command queuing, execution, and undo functionality.
 */
public class CommandQueue {
    private final Queue<PrioritizedCommand> commandQueue;
    private final Deque<Command> commandHistory;
    private final int maxQueueSize;
    private final int maxHistorySize;
    
    /**
     * Create a new command queue with default sizes.
     */
    public CommandQueue() {
        this(100, 50);
    }
    
    /**
     * Create a new command queue with specified sizes.
     * @param maxQueueSize maximum number of commands in the queue
     * @param maxHistorySize maximum number of commands to keep in history
     */
    public CommandQueue(int maxQueueSize, int maxHistorySize) {
        this.commandQueue = new PriorityQueue<>(Comparator.comparingInt(pc -> -pc.priority));
        this.commandHistory = new ArrayDeque<>();
        this.maxQueueSize = maxQueueSize;
        this.maxHistorySize = maxHistorySize;
    }
    
    /**
     * Add a command to the queue with default priority.
     * @param command the command to add
     * @return true if the command was added, false if the queue is full
     */
    public boolean enqueue(Command command) {
        return enqueue(command, 0);
    }
    
    /**
     * Add a command to the queue with specified priority.
     * Higher priority commands are executed first.
     * @param command the command to add
     * @param priority the priority of the command (higher = more important)
     * @return true if the command was added, false if the queue is full
     */
    public boolean enqueue(Command command, int priority) {
        if (command == null) return false;
        
        if (commandQueue.size() >= maxQueueSize) {
            return false; // Queue is full
        }
        
        return commandQueue.offer(new PrioritizedCommand(command, priority));
    }
    
    /**
     * Execute all commands in the queue.
     * Commands are executed in priority order (highest priority first).
     * @return the number of commands executed
     */
    public int executeAll() {
        int executedCount = 0;
        
        while (!commandQueue.isEmpty()) {
            PrioritizedCommand prioritizedCommand = commandQueue.poll();
            if (prioritizedCommand != null) {
                Command command = prioritizedCommand.command;
                
                try {
                    command.execute();
                    addToHistory(command);
                    executedCount++;
                } catch (Exception e) {
                    System.err.println("Error executing command " + command.getDescription() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        return executedCount;
    }
    
    /**
     * Execute the next command in the queue.
     * @return true if a command was executed, false if the queue is empty
     */
    public boolean executeNext() {
        PrioritizedCommand prioritizedCommand = commandQueue.poll();
        if (prioritizedCommand == null) {
            return false;
        }
        
        Command command = prioritizedCommand.command;
        try {
            command.execute();
            addToHistory(command);
            return true;
        } catch (Exception e) {
            System.err.println("Error executing command " + command.getDescription() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Undo the last executed command if possible.
     * @return true if a command was undone, false if no undoable command exists
     */
    public boolean undoLast() {
        while (!commandHistory.isEmpty()) {
            Command command = commandHistory.pollLast();
            if (command.canUndo()) {
                try {
                    command.undo();
                    return true;
                } catch (Exception e) {
                    System.err.println("Error undoing command " + command.getDescription() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    /**
     * Clear all commands from the queue and history.
     */
    public void clear() {
        commandQueue.clear();
        commandHistory.clear();
    }
    
    /**
     * Clear only the command queue, keeping history intact.
     */
    public void clearQueue() {
        commandQueue.clear();
    }
    
    /**
     * Clear only the command history, keeping queue intact.
     */
    public void clearHistory() {
        commandHistory.clear();
    }
    
    /**
     * Get the number of commands currently in the queue.
     * @return the queue size
     */
    public int getQueueSize() {
        return commandQueue.size();
    }
    
    /**
     * Get the number of commands in the history.
     * @return the history size
     */
    public int getHistorySize() {
        return commandHistory.size();
    }
    
    /**
     * Check if the queue is empty.
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return commandQueue.isEmpty();
    }
    
    /**
     * Check if the queue is full.
     * @return true if the queue is full
     */
    public boolean isFull() {
        return commandQueue.size() >= maxQueueSize;
    }
    
    /**
     * Get the maximum queue size.
     * @return the maximum queue size
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    /**
     * Get the maximum history size.
     * @return the maximum history size
     */
    public int getMaxHistorySize() {
        return maxHistorySize;
    }
    
    /**
     * Add a command to the history, maintaining the maximum history size.
     * @param command the command to add to history
     */
    private void addToHistory(Command command) {
        if (command.canUndo()) {
            commandHistory.addLast(command);
            
            // Remove old commands if history is too large
            while (commandHistory.size() > maxHistorySize) {
                commandHistory.pollFirst();
            }
        }
    }
    
    /**
     * Internal class to hold commands with their priorities.
     */
    private static class PrioritizedCommand {
        final Command command;
        final int priority;
        
        PrioritizedCommand(Command command, int priority) {
            this.command = command;
            this.priority = priority;
        }
    }
}