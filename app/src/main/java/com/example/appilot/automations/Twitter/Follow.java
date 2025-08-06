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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Follow {
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
    private double profileViewProbability;

    public Follow(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, double probability) {
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
        this.profileViewProbability = probability;
    }

    public void findTweets() {
        Log.d(TAG, "Searching for ViewGroups to process for following");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in findTweets");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findTweets", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding to collect all ViewGroups");

        // Find the parent node containing tweets
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, "android:id/list");

        if (parentNode != null) {
            Log.d(TAG, "Found parent node for tweets");
            // Collect all ViewGroup children
            List<AccessibilityNodeInfo> viewGroupChildren = collectViewGroupChildren(parentNode);
            if (!viewGroupChildren.isEmpty()) {
                Log.d(TAG, "Found " + viewGroupChildren.size() + " ViewGroup children");
                processViewGroupsWithProbability(viewGroupChildren, 0);
            } else {
                Log.d(TAG, "No ViewGroup children found, scrolling to next section");
                int randomDelay = 1000 + random.nextInt(3000);
                handler.postDelayed(this::continueScrolling, randomDelay);
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with android:id/list, scrolling to next section");
            int randomDelay = 1000 + random.nextInt(3000);
            handler.postDelayed(this::continueScrolling, randomDelay);
        }
        rootNode.recycle();
    }
    // Method to collect all ViewGroup children from a parent node
    private List<AccessibilityNodeInfo> collectViewGroupChildren(AccessibilityNodeInfo parentNode) {
        List<AccessibilityNodeInfo> viewGroupChildren = new ArrayList<>();
        if (parentNode == null) {
            return viewGroupChildren;
        }
        try {
            collectViewGroupChildrenRecursive(parentNode, viewGroupChildren);
            Log.d(TAG, "Collected " + viewGroupChildren.size() + " ViewGroup children");
        } catch (Exception e) {
            Log.e(TAG, "Error collecting ViewGroup children: " + e.getMessage());
        }
        return viewGroupChildren;
    }
    // Recursive method to find all ViewGroup children with specific resource ID
    private void collectViewGroupChildrenRecursive(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> viewGroupChildren) {
        if (node == null) {
            return;
        }
        // Check if current node has the specific resource ID for tweet rows
        String resourceId = node.getViewIdResourceName();
        if ("com.twitter.android:id/row".equals(resourceId)) {
            viewGroupChildren.add(node);
            Log.d(TAG, "Added node with row ID to collection");
        }
        // Recursively check all children
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectViewGroupChildrenRecursive(child, viewGroupChildren);
            }
        }
    }
    // Method to process ViewGroups sequentially with probability check
    private void processViewGroupsWithProbability(List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
        // Check if we've processed all ViewGroups
        if (currentIndex >= viewGroupChildren.size()) {
            Log.d(TAG, "Finished processing all ViewGroups on screen, scrolling to next section");
            // Clean up remaining nodes
            for (AccessibilityNodeInfo viewGroup : viewGroupChildren) {
                helperFunctions.safelyRecycleNode(viewGroup);
            }
            int randomDelay = 1000 + random.nextInt(3000);
            handler.postDelayed(this::continueScrolling, randomDelay);
            return;
        }
        // Get current ViewGroup
        AccessibilityNodeInfo currentViewGroup = viewGroupChildren.get(currentIndex);
        if (currentViewGroup == null) {
            Log.d(TAG, "ViewGroup at index " + currentIndex + " is null, moving to next");
            processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            return;
        }
        Log.d(TAG, "Processing ViewGroup at index: " + currentIndex);
        // Apply probability check
        double randomValue = random.nextDouble();
        Log.d(TAG, "Random Value: " + randomValue + ", Profile view probability: " + profileViewProbability);

        if (randomValue < profileViewProbability) {
            Log.d(TAG, "Opening User Profile based on probability (" + (profileViewProbability * 100) + "%)");
            // Find and click the profile in the current ViewGroup
            boolean profileSuccess = findAndClickProfileInViewGroup(currentViewGroup, viewGroupChildren, currentIndex);
            if (!profileSuccess) {
                Log.d(TAG, "Failed to find/click profile in ViewGroup " + currentIndex + ". Moving to next ViewGroup.");
                // Continue to next ViewGroup after a delay
                int randomDelay = 500 + random.nextInt(1500);
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, randomDelay);
            }
            // If profile click was successful, the flow continues in the follow button method
        } else {
            Log.d(TAG, "Skipping ViewGroup " + currentIndex + " based on probability. Moving to next ViewGroup.");
            // Move to next ViewGroup after a short delay
            int randomDelay = 200 + random.nextInt(800);
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, randomDelay);
        }
    }
    // Method to find and click the profile within a ViewGroup
    private boolean findAndClickProfileInViewGroup(AccessibilityNodeInfo viewGroup, List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
        if (viewGroup == null) {
            Log.e(TAG, "ViewGroup is null, cannot find profile");
            return false;
        }
        Log.d(TAG, "Searching for profile image container in ViewGroup...");

        String profileResourceId = "com.twitter.android:id/tweet_profile_image";
        AccessibilityNodeInfo profileContainer = findNodeByResourceIdInSubtree(viewGroup, profileResourceId);
        if (profileContainer != null) {
            Log.d(TAG, "Found profile image container, attempting click");
            boolean clickSuccess = performClick(profileContainer);
            if (clickSuccess) {
                Log.d(TAG, "Profile clicked successfully. Waiting for profile to load...");
                int randomDelay = 1000 + random.nextInt(5000);
                handler.postDelayed(() -> {
                    findAndClickFollowButton(viewGroupChildren, currentIndex);
                }, randomDelay);
                profileContainer.recycle();
                return true;
            } else {
                Log.e(TAG, "Failed to click profile container");
                profileContainer.recycle();
                return false;
            }
        } else {
            Log.e(TAG, "Profile image container not found in this ViewGroup");
            return false;
        }
    }
    // Helper method to find a node by resource ID within a specific subtree
    private AccessibilityNodeInfo findNodeByResourceIdInSubtree(AccessibilityNodeInfo root, String resourceId) {
        if (root == null || resourceId == null) {
            return null;
        }
        // Check if current node matches
        if (resourceId.equals(root.getViewIdResourceName())) {
            return root;
        }
        // Recursively search children
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByResourceIdInSubtree(child, resourceId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    public void findAndClickFollowButton(List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
        Log.d(TAG, "Searching for Follow Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in follow button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with Click logic");
        String nodeId = "com.twitter.android:id/button_bar_follow";
        AccessibilityNodeInfo followButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        boolean clickSuccess = performClick(followButton);
        if (clickSuccess) {
            Log.d(TAG, "Follow clicked successfully. Going back to main feed...");
            int randomDelay = 1000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                clickBackFromProfile(viewGroupChildren, currentIndex);
            }, randomDelay);
        } else {
            Log.d(TAG, "Follow Button is not found, going back to main feed");
            int randomDelay = 1000 + random.nextInt(2000);
            handler.postDelayed(() -> {
                clickBackFromProfile(viewGroupChildren, currentIndex);
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void clickBackFromProfile(List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
        Log.d(TAG, "Starting hierarchy navigation to click back button...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickBackFromProfile", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.twitter.android:id/toolbar";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToBackFromProfile(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "Back button clicked successfully. Moving to next ViewGroup...");
                    int randomDelay = 1000 + random.nextInt(2000);
                    handler.postDelayed(() -> {
                        processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                    }, randomDelay);
                } else {
                    Log.e(TAG, "Failed to click on back button, moving to next ViewGroup");
                    int randomDelay = 1000 + random.nextInt(2000);
                    handler.postDelayed(() -> {
                        processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                    }, randomDelay);
                }
            } else {
                Log.e(TAG, "Could not navigate to back button, moving to next ViewGroup");
                int randomDelay = 1000 + random.nextInt(2000);
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, randomDelay);
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId + ", moving to next ViewGroup");
            int randomDelay = 1000 + random.nextInt(2000);
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private AccessibilityNodeInfo navigateToBackFromProfile(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0)");

            if (parent == null) {
                Log.e(TAG, "Parent node is null");
                return null;
            }
            // Log parent details for debugging
            Log.d(TAG, "Parent class: " + parent.getClassName());
            Log.d(TAG, "Parent child count: " + parent.getChildCount());

            // Step 1: Get first child (0)
            if (parent.getChildCount() < 1) {
                Log.e(TAG, "Parent has no children");
                return null;
            }
            AccessibilityNodeInfo targetElement = parent.getChild(0);
            if (targetElement == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found target element:");
            Log.d(TAG, "Class: " + targetElement.getClassName());
            Log.d(TAG, "Text: " + targetElement.getText());
            Log.d(TAG, "Clickable: " + targetElement.isClickable());

            Rect bounds = new Rect();
            targetElement.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);
            return targetElement;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }
    public void continueScrolling() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");

        if (elapsedTime < duration) {
            int randomDelay = 2000 + random.nextInt(8000);
            handler.postDelayed(() -> {
                Log.d(TAG, "Scrolling... (" + elapsedTime + "/" + duration + " minutes)");
                float endY = 0.0f + random.nextFloat() * (0.7f);
                float startY = endY + 0.4f;
                helperFunctions.performScroll(startY, endY);
                handler.postDelayed(this::findTweets, 1000);
            }, randomDelay);
        } else {
            Log.d(TAG, "Reached duration of " + duration + " minutes. Exiting app...");
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Duration Following the Users Completed..", "final");
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
        }

        if (isClicked) {
            Log.d(TAG, "Button clicked successfully. Proceeding...");
        } else {
            Log.e(TAG, "Failed to click on button.");
        }
        return isClicked;
    }
}
