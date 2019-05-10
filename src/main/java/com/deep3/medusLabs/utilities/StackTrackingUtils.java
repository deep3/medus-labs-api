package com.deep3.medusLabs.utilities;

public class StackTrackingUtils {

    private static int numOfUsers;
    private static int numOfStacksCompleted;
    private static int numOfStacksFailed;

    private StackTrackingUtils() { throw new IllegalStateException("Static Utility class"); }

    /**
     * Set the number of users for the STACK
     * @param i The number of users
     */
    public static void setNumOfUsers(int i) {
        numOfUsers = i;
        numOfStacksCompleted = getNumOfStacksFinished();
        numOfStacksFailed = getNumOfFailedStacks();
    }

    /**
     * Reset the stack properties
     */
    public static void reset() {
        numOfStacksCompleted = 0;
        numOfStacksFailed = 0;
    }

    /**
     * Increment the numOfStacksCompleted property
     */
    public static void increaseNumOfStacksCompleted() {
        numOfStacksCompleted++;
    }

    /**
     * Increment the numOfStacksFailed property
     */
    public static void increaseNumOfStacksFailed() {
        numOfStacksFailed++;
    }

    /**
     * Get the Number Of Users property
     * @return - Integer value
     */
    public static int getNumOfUsers() {
        return numOfUsers;
    }

    /**
     * Get the Number Of Stacks Finished
     * @return - Integer value
     */
    public static int getNumOfStacksFinished() {
        return numOfStacksCompleted + numOfStacksFailed;
    }

    /**
     * Get the Number Of Stacks Failed Property
     * @return - Integer value
     */
    public static int getNumOfFailedStacks() {
        return numOfStacksFailed;
    }
}
