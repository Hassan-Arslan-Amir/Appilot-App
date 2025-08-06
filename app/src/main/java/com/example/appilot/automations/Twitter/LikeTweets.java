package com.example.appilot.automations.Twitter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;

public class LikeTweets {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;

    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private List<Object> AccountInputs;
    private int duration;
    private long startTime;
    private double likeProbability;

    public LikeTweets(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, double probabilty) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.AccountInputs = AccountInputs;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.likeProbability = probabilty;
    }
    public void findAndClickLikeButton() {
        Log.d(TAG, "Searching for Like Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with decision logic");

        double randomValue = random.nextDouble();
        Log.d(TAG,"Random Value: "+randomValue+", Profile view probability: "+likeProbability);
        if (randomValue < likeProbability) {
            Log.d(TAG, "Clicking Like based on probability ("+(likeProbability*100)+ "%)");
            findAndClickLike(rootNode);
        } else {
            Log.d(TAG, "Moving to next tweet");
            int randomDelay = 1000 + random.nextInt(5000);
            handler.postDelayed(this::onScroll, randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickLike(AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "Searching for Like Button");

        String profileResourceId = "com.twitter.android:id/inline_like";
        AccessibilityNodeInfo likeButton = HelperFunctions.findNodeByResourceId(rootNode, profileResourceId);
        if (likeButton != null) {
            Log.d(TAG, "Found Like Button, attempting click");
            boolean clickSuccess = performClick(likeButton);
            if (clickSuccess) {
                Log.d(TAG, "Like Button clicked successfully. Waiting for profile to load...");
                int randomDelay = 1000 + random.nextInt(5000);
                handler.postDelayed(this::onScroll, randomDelay);
            }
        } else {
            Log.d(TAG, "Like Button is not found");
            handler.postDelayed(this::onScroll, 2000);
        }
    }
    public void onScroll() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");

        if (elapsedTime < duration) {
            int randomDelay = 2000 + random.nextInt(8000);
            handler.postDelayed(() -> {
                Log.d(TAG, "liking... (" + elapsedTime + "/" + duration + " minutes)");
                float endY = 0.0f + random.nextFloat() * (0.7f);
                float startY = endY + 0.3f;
                helperFunctions.performScroll(startY, endY);

                handler.postDelayed(this::findAndClickLikeButton, 1000);
            }, randomDelay);
        } else {
            Log.d(TAG, "Reached duration of " + duration + " minutes. Exiting app...");
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Duration Completed for liking the Tweets...", "final");
            }, 2000);
        }
    }
    private boolean performClick(AccessibilityNodeInfo targetButton) {
        boolean isClicked = false;
        try {
            Log.d(TAG, "Found button. Attempting click.");
            isClicked = targetButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (Exception e) {
            Log.e(TAG, "Error performing click action on button: " + e.getMessage());
        } finally {
            helperFunctions.safelyRecycleNode(targetButton);
        }
        if (isClicked) {
            Log.d(TAG, "Button clicked successfully. Proceeding to next step...");
        } else {
            Log.e(TAG, "Failed to click on button.");
        }
        return isClicked;
    }
}
//package com.example.appilot.automations.Twitter;
//
//import android.accessibilityservice.AccessibilityService;
//import android.accessibilityservice.GestureDescription;
//import android.content.Context;
//import android.graphics.Path;
//import android.graphics.Rect;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.accessibility.AccessibilityNodeInfo;
//
//import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
//import com.example.appilot.services.MyAccessibilityService;
//import com.example.appilot.utils.HelperFunctions;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class LikeTweets {
//    private static final String TAG = "TwitterAutomation";
//    private final Context context;
//    private final Handler handler;
//    private final Random random;
//    private final PopUpHandler popUpHandler;
//    private final MyAccessibilityService service;
//    private HelperFunctions helperFunctions;
//    private String Task_id = null;
//    private String job_id = null;
//    private List<Object> AccountInputs;
//    private int duration;
//    private long startTime;
//    private double likeProbability;
//
//    public LikeTweets(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, double probability) {
//        this.context = service;
//        this.service = service;
//        this.Task_id = taskid;
//        this.job_id = jobid;
//        this.handler = new Handler(Looper.getMainLooper());
//        this.random = new Random();
//        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
//        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
//        this.AccountInputs = AccountInputs;
//        this.duration = duration;
//        this.startTime = System.currentTimeMillis();
//        this.likeProbability = probability;
//    }
//
//    public void findAndClickLikeButton() {
//        Log.d(TAG, "Searching for ViewGroups to process for liking");
//        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
//        if (rootNode == null) {
//            Log.e(TAG, "No root node available in like button");
//            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
//            return;
//        }
//        Log.d(TAG, "Root node available, proceeding to collect all ViewGroups");
//
//        // Find the parent node containing tweets
//        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, "android:id/list");
//
//        if (parentNode != null) {
//            Log.d(TAG, "Found parent node for tweets");
//            // Collect all ViewGroup children
//            List<AccessibilityNodeInfo> viewGroupChildren = collectViewGroupChildren(parentNode);
//            if (!viewGroupChildren.isEmpty()) {
//                Log.d(TAG, "Found " + viewGroupChildren.size() + " ViewGroup children");
//                processViewGroupsWithProbability(viewGroupChildren, 0);
//            } else {
//                Log.d(TAG, "No ViewGroup children found, scrolling to next section");
//                int randomDelay = 1000 + random.nextInt(3000);
//                handler.postDelayed(this::onScroll, randomDelay);
//            }
//            parentNode.recycle();
//        } else {
//            Log.e(TAG, "Could not find parent node with android:id/list, scrolling to next section");
//            int randomDelay = 1000 + random.nextInt(3000);
//            handler.postDelayed(this::onScroll, randomDelay);
//        }
//        rootNode.recycle();
//    }
//
//    // Method to collect all ViewGroup children from a parent node
//    private List<AccessibilityNodeInfo> collectViewGroupChildren(AccessibilityNodeInfo parentNode) {
//        List<AccessibilityNodeInfo> viewGroupChildren = new ArrayList<>();
//        if (parentNode == null) {
//            return viewGroupChildren;
//        }
//        try {
//            collectViewGroupChildrenRecursive(parentNode, viewGroupChildren);
//            Log.d(TAG, "Collected " + viewGroupChildren.size() + " ViewGroup children");
//        } catch (Exception e) {
//            Log.e(TAG, "Error collecting ViewGroup children: " + e.getMessage());
//        }
//        return viewGroupChildren;
//    }
//
//    // Recursive method to find all ViewGroup children with specific resource ID
//    private void collectViewGroupChildrenRecursive(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> viewGroupChildren) {
//        if (node == null) {
//            return;
//        }
//        // Check if current node has the specific resource ID for tweet rows
//        String resourceId = node.getViewIdResourceName();
//        if ("com.twitter.android:id/row".equals(resourceId)) {
//            viewGroupChildren.add(node);
//            Log.d(TAG, "Added node with row ID to collection");
//        }
//        // Recursively check all children
//        int childCount = node.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            AccessibilityNodeInfo child = node.getChild(i);
//            if (child != null) {
//                collectViewGroupChildrenRecursive(child, viewGroupChildren);
//            }
//        }
//    }
//
//    // Method to process ViewGroups sequentially with probability check
//    private void processViewGroupsWithProbability(List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
//        // Check if we've processed all ViewGroups
//        if (currentIndex >= viewGroupChildren.size()) {
//            Log.d(TAG, "Finished processing all ViewGroups on screen, scrolling to next section");
//            // Clean up remaining nodes
//            for (AccessibilityNodeInfo viewGroup : viewGroupChildren) {
//                helperFunctions.safelyRecycleNode(viewGroup);
//            }
//            int randomDelay = 1000 + random.nextInt(3000);
//            handler.postDelayed(this::onScroll, randomDelay);
//            return;
//        }
//
//        // Get current ViewGroup
//        AccessibilityNodeInfo currentViewGroup = viewGroupChildren.get(currentIndex);
//        if (currentViewGroup == null) {
//            Log.d(TAG, "ViewGroup at index " + currentIndex + " is null, moving to next");
//            processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
//            return;
//        }
//
//        Log.d(TAG, "Processing ViewGroup at index: " + currentIndex);
//
//        // Apply probability check
//        double randomValue = random.nextDouble();
//        Log.d(TAG, "Random Value: " + randomValue + ", Like probability: " + likeProbability);
//
//        if (randomValue < likeProbability) {
//            Log.d(TAG, "Clicking Like based on probability (" + (likeProbability * 100) + "%)");
//            // Find and click the like button in the current ViewGroup
//            boolean likeSuccess = clickLikeButtonInViewGroup(currentViewGroup);
//            if (likeSuccess) {
//                Log.d(TAG, "Successfully liked tweet in ViewGroup " + currentIndex + ". Moving to next ViewGroup...");
//                // Add a delay between likes to make it more natural
//                int randomDelay = 500 + random.nextInt(2000);
//                handler.postDelayed(() -> {
//                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
//                }, randomDelay);
//            } else {
//                Log.d(TAG, "Failed to like tweet in ViewGroup " + currentIndex + ". Moving to next ViewGroup.");
//                // Continue to next ViewGroup after a delay
//                int randomDelay = 500 + random.nextInt(1500);
//                handler.postDelayed(() -> {
//                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
//                }, randomDelay);
//            }
//        } else {
//            Log.d(TAG, "Skipping ViewGroup " + currentIndex + " based on probability. Moving to next ViewGroup.");
//            // Move to next ViewGroup after a short delay
//            int randomDelay = 200 + random.nextInt(800);
//            handler.postDelayed(() -> {
//                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
//            }, randomDelay);
//        }
//    }
//
//    // Method to find and click the like button within a ViewGroup
//    private boolean clickLikeButtonInViewGroup(AccessibilityNodeInfo viewGroup) {
//        if (viewGroup == null) {
//            Log.e(TAG, "ViewGroup is null, cannot find like button");
//            return false;
//        }
//        Log.d(TAG, "Searching for like button in ViewGroup...");
//
//        // Find the like button by resource ID within this ViewGroup
//        AccessibilityNodeInfo likeButton = findNodeByResourceIdInSubtree(viewGroup, "com.twitter.android:id/inline_like");
//        if (likeButton != null) {
//            Log.d(TAG, "Found like button, attempting to click...");
//            boolean clickSuccess = performClick(likeButton);
//            if (clickSuccess) {
//                Log.d(TAG, "Successfully clicked like button on tweet");
//                likeButton.recycle();
//                return true;
//            } else {
//                Log.e(TAG, "Failed to click like button");
//                likeButton.recycle();
//                return false;
//            }
//        } else {
//            Log.e(TAG, "No like button found in this ViewGroup");
//            return false;
//        }
//    }
//
//    // Helper method to find a node by resource ID within a specific subtree
//    private AccessibilityNodeInfo findNodeByResourceIdInSubtree(AccessibilityNodeInfo root, String resourceId) {
//        if (root == null || resourceId == null) {
//            return null;
//        }
//        // Check if current node matches
//        if (resourceId.equals(root.getViewIdResourceName())) {
//            return root;
//        }
//        // Recursively search children
//        int childCount = root.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            AccessibilityNodeInfo child = root.getChild(i);
//            if (child != null) {
//                AccessibilityNodeInfo result = findNodeByResourceIdInSubtree(child, resourceId);
//                if (result != null) {
//                    return result;
//                }
//            }
//        }
//        return null;
//    }
//
//    public void onScroll() {
//        Log.d(TAG, "Duration : " + duration + " minutes");
//        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
//
//        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");
//
//        if (elapsedTime < duration) {
//            int randomDelay = 2000 + random.nextInt(8000);
//            handler.postDelayed(() -> {
//                Log.d(TAG, "liking... (" + elapsedTime + "/" + duration + " minutes)");
//                float endY = 0.0f + random.nextFloat() * (0.8f);
//                float startY = endY + 0.3f;
//                helperFunctions.performScroll(startY, endY);
//
//                handler.postDelayed(this::findAndClickLikeButton, 1000);
//            }, randomDelay);
//        } else {
//            Log.d(TAG, "Reached duration of " + duration + " minutes. Exiting app...");
//            handler.postDelayed(() -> {
//                helperFunctions.cleanupAndExit("Duration of Liking the Tweets Completed..", "final");
//            }, 2000);
//        }
//    }
//
//    private boolean performClick(AccessibilityNodeInfo targetButton) {
//        boolean isClicked = false;
//        try {
//            Log.d(TAG, "Found button. Attempting click.");
//            isClicked = targetButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        } catch (Exception e) {
//            Log.e(TAG, "Error performing click action on button: " + e.getMessage());
//        }
//
//        if (isClicked) {
//            Log.d(TAG, "Button clicked successfully. Proceeding to next button...");
//        } else {
//            Log.e(TAG, "Failed to click on button.");
//        }
//        return isClicked;
//    }
//}
