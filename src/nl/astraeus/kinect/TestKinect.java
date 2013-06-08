package nl.astraeus.kinect;

import pKinect.PKinect;
import pKinect.SkeletonData;
import processing.core.PApplet;
import processing.core.PImage;
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

    public TestKinect() {
        kinect = new PKinect(this);
    }

    public void setup() {
        // original setup code here ...
        size(800, 800);

        // prevent thread from starving everything else
        noLoop();
    }

    public void draw() {
        // drawing code goes here
        clear();
        stroke(0xffffff);
        textSize(10);

        synchronized(bodies) {
            for (SkeletonData skeleton : bodies) {
                //System.out.println("Position: " + skeleton.dwTrackingID + " -> " + skeleton.position + " (" + skeleton.skeletonPositions.length + " points)");

                int id = 0;
                for (PVector vector : skeleton.skeletonPositions) {
                    //System.out.println("\t "+vector);
                    float depth = vector.array()[2];
                    depth = depth / 1000f;
                    depth = 255f - (255f / 20f * depth);
                    fill(0, depth, 255);

                    float x = vector.array()[0] * 800;
                    float y = vector.array()[1] * 800;

                    ellipse(x,  y, 10, 10);

                    fill(255, 255, 255);
                    text(String.valueOf(id++), x, y);
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
