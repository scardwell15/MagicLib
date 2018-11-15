//By Nicke535
//This file isn't meant to be used directly; use the MagicTrailPlugin to actually do anything properly. Your mod will
//most likely lose backwards-compatibility if you try to call this class' constructor manually, so don't.
package data.scripts.util;

import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

public class MagicTrailObject {
    //Private, non-varying values
    private float inDuration = 0f;
    private float mainDuration = 0f;
    private float outDuration = 0f;
    private float startSize = 0f;
    private float endSize = 0f;
    private float startAngleVelocity = 0f;
    private float endAngleVelocity = 0f;
    private float mainOpacity = 0f;
    private float startSpeed = 0f;
    private float endSpeed = 0f;
    private Color startColor = new Color(255, 255, 255);
    private Color endColor = new Color(255, 255, 255);
    private Vector2f offsetVelocity = new Vector2f(0f, 0f);
    private float sizePulseWidth = 0f;
    private float sizePulseCount = 0;

    //Public, non-varying values
    public int blendModeSRC = 0;
    public int blendModeDEST = 0;
    public float textureLoopLength = 0;
    public float aggressiveCulling = -1f;

    //Public, varying values
    public Color currentColor = new Color(255, 255, 255);
    public float currentSize = 0f;
    public float spentLifetime = 0f;
    public float currentAngularVelocity = 0f;
    public float angle = 0f;
    public float currentSpeed = 0f;
    public Vector2f currentLocation = new Vector2f(0f, 0f);
    public float currentOpacity = 0f;

    //Main instantiator: generates a full MagicTrailObject with all necessary values
    public MagicTrailObject (float inDuration, float mainDuration, float outDuration, float startSize, float endSize, float startAngleVelocity,
                                    float endAngleVelocity, float mainOpacity, int blendModeSRC, int blendModeDEST, float startSpeed, float endSpeed,
                                    Color startColor, Color endColor, float angle, Vector2f spawnLocation, float textureLoopLength,
                                    Vector2f offsetVelocity, float aggressiveCulling, float sizePulseWidth, float sizePulseCount) {
        this.inDuration = inDuration;
        this.mainDuration = mainDuration;
        this.outDuration = outDuration;
        this.startSize = startSize;
        this.endSize = endSize;
        this.startAngleVelocity = startAngleVelocity;
        this.endAngleVelocity = endAngleVelocity;
        this.mainOpacity = mainOpacity;
        this.blendModeSRC = blendModeSRC;
        this.blendModeDEST = blendModeDEST;
        this.startSpeed = startSpeed;
        this.endSpeed = endSpeed;
        this.startColor = startColor;
        this.endColor = endColor;
        this.angle = angle;
        this.currentLocation.x = spawnLocation.x;
        this.currentLocation.y = spawnLocation.y;
        this.textureLoopLength = textureLoopLength;
        this.offsetVelocity = offsetVelocity;
        this.aggressiveCulling = aggressiveCulling;
        this.sizePulseWidth = sizePulseWidth;
        this.sizePulseCount = sizePulseCount;

        this.currentColor = startColor;
        this.currentSize = startSize;
        this.spentLifetime = 0f;
        this.currentAngularVelocity = startAngleVelocity;
        this.currentSpeed = startSpeed;
        if (inDuration > 0) {
            this.currentOpacity = 0f;
        } else {
            this.currentOpacity = mainOpacity;
        }
    }

    //Modifies lifetime, position and all other things time-related
    public void tick (float amount) {
        //Increases lifetime
        spentLifetime += amount;

        //If our spent lifetime is higher than our total lifetime, set it to our total lifetime
        if (spentLifetime > getTotalLifetime()) {
            spentLifetime = getTotalLifetime();
        }

        //Calculates our "pulse" size, depending on pulse count, width and lifetime
        //It is "smoothly" transitioned using MagicAnim.smooth(), and repeats itself sizePulseCount times
        //Counts the first "half" of its lifetime as rising, the other as falling. sizePulseCount makes this "internal"
        //lifetime happen multiple times over the trail's true lifetime
        float thisFramePulseWidth = (spentLifetime/getTotalLifetime() * sizePulseCount);
        while (thisFramePulseWidth > 1f) {thisFramePulseWidth--;}
        if (thisFramePulseWidth > 0.5f) {thisFramePulseWidth = 1f - thisFramePulseWidth;}
        thisFramePulseWidth = MagicAnim.smooth(thisFramePulseWidth) * sizePulseWidth;


        //Slides all values along depending on lifetime
        currentSize = startSize * (1 - (spentLifetime / getTotalLifetime())) + endSize * (spentLifetime / getTotalLifetime())
                + thisFramePulseWidth;
        currentSpeed = startSpeed * (1 - (spentLifetime / getTotalLifetime())) + endSpeed * (spentLifetime / getTotalLifetime());
        currentAngularVelocity = startAngleVelocity * (1 - (spentLifetime / getTotalLifetime())) + endAngleVelocity * (spentLifetime / getTotalLifetime());
        int red = ((int)(startColor.getRed() * (1 - (spentLifetime / getTotalLifetime())) + endColor.getRed() * (spentLifetime / getTotalLifetime())));
        int green = ((int)(startColor.getGreen() * (1 - (spentLifetime / getTotalLifetime())) + endColor.getGreen() * (spentLifetime / getTotalLifetime())));
        int blue = ((int)(startColor.getBlue() * (1 - (spentLifetime / getTotalLifetime())) + endColor.getBlue() * (spentLifetime / getTotalLifetime())));
        currentColor = new Color(red, green, blue);

        //Adjusts opacity: slightly differently handled than the otherwise pure linear value sliding
        currentOpacity = mainOpacity;
        if (spentLifetime < inDuration) {
            currentOpacity = mainOpacity * spentLifetime / inDuration;
        } else if (spentLifetime > (inDuration + mainDuration)) {
            currentOpacity = mainOpacity * (1f - ((spentLifetime - (inDuration + mainDuration))/outDuration));
        }

        //Calculates new position and angle from respective velocities
        angle += currentAngularVelocity * amount;
        currentLocation.x += (FastTrig.cos(Math.toRadians(angle)) * currentSpeed + offsetVelocity.x) * amount;
        currentLocation.y += (FastTrig.sin(Math.toRadians(angle)) * currentSpeed + offsetVelocity.y) * amount;
    }

    public float getSpentLifetime () {
        return spentLifetime;
    }
    public float getTotalLifetime () {
        return inDuration + mainDuration + outDuration;
    }
}
