package nl.astraeus.kinect;

import pKinect.PKinect;
import pKinect.SkeletonData;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * User: Gebruiker
 * Date: 8-6-13
 * Time: 14:15
 */
public class TestKinect extends PApplet {

    private ArrayList<SkeletonData> bodies = new ArrayList<SkeletonData>();
    private PKinect kinect;
    private boolean dragging = false;
    private int dragging_hand = 0;
    private float shoulderWidth = 0;

    private boolean animating = false;
    private float dragStartX = 0, dragStartY = 0;
    private float currentXOffset = 0;

    public TestKinect() {
        kinect = new PKinect(this);
    }

    public void setup() {
        // original setup code here ...
        size(800, 800);

        // prevent thread from starving everything else
        noLoop();
    }

    private float getBodyDepth(SkeletonData body) {
        int [] positionsToCheck = new int[7];

        positionsToCheck[0] = SkeletonData.NUI_SKELETON_POSITION_HEAD;
        positionsToCheck[1] = SkeletonData.NUI_SKELETON_POSITION_SHOULDER_CENTER;
        positionsToCheck[2] = SkeletonData.NUI_SKELETON_POSITION_SHOULDER_LEFT;
        positionsToCheck[3] = SkeletonData.NUI_SKELETON_POSITION_SHOULDER_RIGHT;
        positionsToCheck[4] = SkeletonData.NUI_SKELETON_POSITION_HIP_CENTER;
        positionsToCheck[5] = SkeletonData.NUI_SKELETON_POSITION_HIP_LEFT;
        positionsToCheck[6] = SkeletonData.NUI_SKELETON_POSITION_HIP_RIGHT;

        float totalDepth = 0;
        int foundCoords = 0;
        for (int jointID : positionsToCheck) {
            if (body.skeletonPositionTrackingState[jointID] == SkeletonData.NUI_SKELETON_POSITION_TRACKED) {
                totalDepth += body.skeletonPositions[jointID].array()[2];
                foundCoords++;
            }
        }

        if (body.skeletonPositionTrackingState[SkeletonData.NUI_SKELETON_POSITION_SHOULDER_LEFT] == SkeletonData.NUI_SKELETON_POSITION_TRACKED &&
                body.skeletonPositionTrackingState[SkeletonData.NUI_SKELETON_POSITION_SHOULDER_RIGHT] == SkeletonData.NUI_SKELETON_POSITION_TRACKED
                ) {
            shoulderWidth = Math.abs(body.skeletonPositions[SkeletonData.NUI_SKELETON_POSITION_SHOULDER_LEFT].array()[0] -
                    body.skeletonPositions[SkeletonData.NUI_SKELETON_POSITION_SHOULDER_RIGHT].array()[0]);
        }

        if (foundCoords > 0) {
            return totalDepth / (float)foundCoords;
        } else {
            return -1;
        }
    }

    public void draw() {
        // drawing code goes here
        clear();
        stroke(0xffffff);
        textSize(10);

        synchronized(bodies) {

            for (SkeletonData skeleton : bodies) {
                float bodyDepth = getBodyDepth(skeleton);

                if (bodyDepth > 0 && shoulderWidth > 0) {
                    if (animating) {
                        currentXOffset = currentXOffset * 1.2f;

                        if (Math.abs(currentXOffset) > 2) {
                            animating = false;
                            dragging = false;
                            currentXOffset = 0;
                        }
                    } else {
                        // right hand
                        if (skeleton.trackingState == SkeletonData.NUI_SKELETON_TRACKED) {
                            if (skeleton.skeletonPositionTrackingState[SkeletonData.NUI_SKELETON_POSITION_HAND_RIGHT] == SkeletonData.NUI_SKELETON_TRACKED) {
                                float rightHandDepth = skeleton.skeletonPositions[SkeletonData.NUI_SKELETON_POSITION_HAND_RIGHT].array()[2];

                                if (rightHandDepth < bodyDepth - 2500) {
                                    float x =skeleton.skeletonPositions[SkeletonData.NUI_SKELETON_POSITION_HAND_RIGHT].array()[0];
                                    float y =skeleton.skeletonPositions[SkeletonData.NUI_SKELETON_POSITION_HAND_RIGHT].array()[1];

                                    if (dragging) {
                                        currentXOffset = x - dragStartX;

                                        if (shoulderWidth > 0) {
                                            if (currentXOffset > shoulderWidth * 1.5) {
                                                // move right
                                                System.out.println("RIGHT");
                                                animating = true;
                                            } else if (currentXOffset < -shoulderWidth * 1.5) {
                                                // move left
                                                System.out.println("LEFT");
                                                animating = true;
                                            }
                                        }
                                    } else {
                                        dragging = true;
                                        dragStartX = x;
                                        dragStartY = y;
                                        currentXOffset = 0;

                                        System.out.println("Start dragging: "+x+","+y);
                                        System.out.println("Shoulders"+shoulderWidth);
                                    }
                                } else {
                                    if (dragging || animating) {
                                        System.out.println("Stop dragging: "+currentXOffset);
                                    }
                                    dragging = false;
                                    currentXOffset = currentXOffset * 0.9f;
                                }
                            }
                        }
                    }
                }

                for (int id = 0; id < skeleton.skeletonPositions.length; id++) {
                    PVector vector = skeleton.skeletonPositions[id];

                    float red = 0f;

                    if (id == SkeletonData.NUI_SKELETON_POSITION_HAND_RIGHT) {
                        red = 255;
                    }

                    //System.out.println("\t "+vector);
                    float depth = bodyDepth - vector.array()[2];
                    depth = depth / 100f;
                    depth = depth + 30f;

                    //System.out.println("Depth: "+depth);

                depth = (255f * (depth / 80f));
                fill(red, depth, depth);

                float x = vector.array()[0] * 800;
                float y = vector.array()[1] * 800;

                ellipse(x,  y, 10, 10);
            }

            if (dragging) {
                fill(255, 0, 0);
                rect(400 + currentXOffset * 400 , 400, 20, 20);
            }
            }
        }

    }

    public void mousePressed() {
        System.exit(0);
    }

    public void printStatus() {
        synchronized(bodies) {
            if (bodies.isEmpty()) {
                System.out.println("Nobody here!");
            } else {
                for (SkeletonData skeleton : bodies) {
                    System.out.println("Position: " + skeleton.dwTrackingID + " -> " + skeleton.position + " (" + skeleton.skeletonPositions.length + " points)");

                    // show right hand location
                    System.out.println("\t "+skeleton.skeletonPositions[10]);

                    for (PVector vector : skeleton.skeletonPositions) {
                        //System.out.println("\t "+vector);
                    }
                }
            }
        }
    }

    public void appearEvent(SkeletonData _s)
    {
        if (_s.trackingState == PKinect.NUI_SKELETON_NOT_TRACKED)
        {
            return;
        }
        synchronized(bodies) {
            bodies.add(_s);
        }
    }

    public void disappearEvent(SkeletonData _s)
    {
        synchronized(bodies) {
            for (int i=bodies.size()-1; i>=0; i--)
            {
                if (_s.dwTrackingID == bodies.get(i).dwTrackingID)
                {
                    bodies.remove(i);
                }
            }
        }
    }

    public void moveEvent(SkeletonData _b, SkeletonData _a)
    {
        if (_a.trackingState == PKinect.NUI_SKELETON_NOT_TRACKED)
        {
            return;
        }
        synchronized(bodies) {
            for (int i=bodies.size()-1; i>=0; i--)
            {
                if (_b.dwTrackingID == bodies.get(i).dwTrackingID)
                {
                    bodies.get(i).copy(_a);
                    redraw();
                    break;
                }
            }
        }
    }


}
