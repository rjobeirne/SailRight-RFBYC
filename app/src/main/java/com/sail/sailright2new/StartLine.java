package com.sail.sailright2new;

import android.location.Location;
import android.util.Log;

public class StartLine {

    // Initialise global object variables
    String startTarget = null;
    Location markA = null;
    Location markH = null;
    double latA = 0;
    double lonA = 0;
    double latFirstMark;
    double slopeLine = 0;
    double constLine = 0;
    float slopeLineAngle;
    Location startPoint = new Location("");

    // Initialise boat details
    Location mCurrentLocation = null;
    double latBoat = 0;
    double lonBoat = 0;
    float boatHeading, bearingToA, bearingToH;
    float displayBoatHeading, displayBearingToA, displayBearingToH;
    double slopeBoat = 0;
    float slopeBoatAngle;
    double constBoat = 0;
    int directionFactor;

    /**
     * Define the start line from the mark locations
     * @param a
     * @param h
     * @param first
     */
    public StartLine (Location a, Location h, Location first) {
        // constructor with 'A' Mark, and 'H' mark location details, and first currentLocation
        markA = a;
        markH = h;
        Location firstMark = first;
        latA = markA.getLatitude();
        lonA = markA.getLongitude();
        latFirstMark = firstMark.getLatitude();

        // Define finish line as linear equation lat = slope * lon + constant
        float lineBearing = markA.bearingTo(markH);

        // Convert line bearing to angle from latitude (not north)
        if ( lineBearing > 0) {
            slopeLineAngle = 90 - lineBearing;
        } else {
            slopeLineAngle = 90 + lineBearing;
        }

        slopeLine = Math.tan(Math.toRadians(slopeLineAngle));
        constLine = latA - (slopeLine * lonA);
    }

    /**
     * Calculate the boat location details
     * @param currentLocation
     */
    private void setBoatDetails(Location currentLocation) {
        // Define boat heading as linear equation lat = slope * lon + constant
        mCurrentLocation = currentLocation;
        latBoat = mCurrentLocation.getLatitude();
        lonBoat = mCurrentLocation.getLongitude();
        boatHeading = mCurrentLocation.getBearing();

        // Convert boat heading to angle from latitude (not north)
        if ( boatHeading > 0) {
            slopeBoatAngle = 90 - boatHeading;
        } else {
            slopeBoatAngle = 90 + boatHeading;
        }

        slopeBoat = Math.tan(Math.toRadians(slopeBoatAngle));
        constBoat = latBoat - (slopeBoat * lonBoat);

        // Convert bearings to compass bearings
                if (boatHeading < 0) {
                    displayBoatHeading = boatHeading + 360;
                } else {
                    displayBoatHeading = boatHeading;
                }
        bearingToA = mCurrentLocation.bearingTo(markA);
                if ( bearingToA < 0) {
                    displayBearingToA = bearingToA + 360;
                } else {
                    displayBearingToA = bearingToA;
                }
        bearingToH = mCurrentLocation.bearingTo(markH);
                if ( bearingToH < 0) {
                    displayBearingToH = bearingToH + 360;
                } else {
                    displayBearingToH = bearingToH;
                }
    }

    /**
     * Determine approach direction to line
     * @return direction factor, 1= North, -1= South
     */
    public int getStartDirection() {
        if (latFirstMark < latA) {
            // Approaching finish from the north
            directionFactor = 1;
        } else {
            // Approach from the south
            directionFactor = -1;
        }
        return  directionFactor;
    }

    /**
     *
     * @return The name of the start point
     * Mark name or Line
     */
    public String getStartTarget(Location currentLocation) {
        // Update current Location of the boat, passed in from Main
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);  // Update current boat location details

        if (directionFactor == 1) {
            // Approaching from the north
            if (displayBoatHeading > displayBearingToA) {
                startTarget = "A";
            } else if (displayBoatHeading < displayBearingToH) {
                startTarget = "H";
            } else {
                startTarget = "Line";
            }

        } else {
            // Approaching from the south
            // Use bearing +/- from north
            if (boatHeading < bearingToA) {
                startTarget = "A";
            } else if (boatHeading > bearingToH) {
                startTarget = "H";
            } else {
                startTarget = "Line";
            }
        }
        return  startTarget;
    }

    /**
     *
     * @return startPoint
     * The location of the start point on the line
     */
    public Location getStartPoint(Location currentLocation) {
        // Update the current Location of the boat
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);

        // Calculate lon & lat of start point
        double finLon = (constLine - constBoat) / (slopeBoat - slopeLine);
        double finLat = slopeLine * finLon + constLine;

        // Define startPoint by lon & lat
        startPoint.setLongitude(finLon);
        startPoint.setLatitude(finLat);
        return startPoint;
    }

    /**
     * Calculate the approach angle to the line
     * @return approachAngle
     */
    public double getApproachAngle() {
        double approachAngle = slopeLineAngle - slopeBoatAngle;
        return approachAngle;
    }

    /**
     * Calculate the closest dist from current location to the line
     * @return
     */
    public double getShortestDist() {
        double shortestDist = Math.abs(slopeLine * lonBoat - latBoat + constLine)
                / Math.sqrt(Math.pow(slopeLine, 2) + 1) * 60 * 1852;
        return shortestDist;
    }

}
