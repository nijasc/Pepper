package com.buhlergroup.pepper.action.navigation;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.geometry.Quaternion;
import com.aldebaran.qi.sdk.object.geometry.Transform;

/**
 * Pure pose/geometry helpers for navigation. Stateless; all methods static.
 */
final class PoseUtil {

    private PoseUtil() {
    }

    static Transform robotInMap(QiContext c) {
        Frame robotFrame = c.getActuation().robotFrame();
        Frame mapFrame = c.getMapping().mapFrame();
        return robotFrame.computeTransform(mapFrame).getTransform();
    }

    static double[] pose2d(Transform t) {
        double x = t.getTranslation().getX();
        double y = t.getTranslation().getY();
        Quaternion q = t.getRotation();
        double theta = Math.atan2(
                2.0 * (q.getW() * q.getZ() + q.getX() * q.getY()),
                1.0 - 2.0 * (q.getY() * q.getY() + q.getZ() * q.getZ()));
        return new double[]{x, y, theta};
    }
}
