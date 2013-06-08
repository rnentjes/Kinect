package nl.astraeus.kinect;

import pKinect.PKinect;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;

/**
 * User: Gebruiker
 * Date: 8-6-13
 * Time: 15:53
 */
public class TestFrame extends Frame {

    public static void main(String [] args) {
        PApplet.main(new String[] { "--present", "nl.astraeus.kinect.TestKinect" });
    }

    public TestFrame() {
        super("Embedded PApplet");

        setLayout(new BorderLayout());
        PApplet testKinect = new TestKinect();
        add(testKinect, BorderLayout.CENTER);

        // important to call this whenever embedding a PApplet.
        // It ensures that the animation thread is started and
        // that other internal variables are properly set.
        testKinect.init();
    }
}
