package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lazywizard.lazylib.ui.LazyFont.DrawableString;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Dark.Revenant, Tartiflette, LazyWizard, Snrasha
 * 
 */

public class MagicUI {
    //Color of the HUD when the ship is alive or the hud
    public static final Color GREENCOLOR;
    //Color of the HUD when the ship is not alive.
    public static final Color BLUCOLOR;
    //Color of the HUD for the red color.
    public static final Color REDCOLOR;
    private static DrawableString TODRAW14;
    private static DrawableString TODRAW10;

    private static Color lastColor;
    private static float lastUpdate;

    private static final Vector2f SYSTEMBARVEC1 = new Vector2f(0, 0);
    private static final Vector2f SYSTEMBARVEC2 = new Vector2f(29f, 58f);
    private static final Vector2f PERCENTBARVEC1 = new Vector2f(21f, 0f); // Just 21 pixel of width of difference.
    private static final Vector2f PERCENTBARVEC2 = new Vector2f(50f, 58f);

    static {
        GREENCOLOR = Global.getSettings().getColor("textFriendColor");
        BLUCOLOR = Global.getSettings().getColor("textNeutralColor");
        REDCOLOR = Global.getSettings().getColor("textEnemyColor");

        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            TODRAW14 = fontdraw.createText();

            fontdraw = LazyFont.loadFont("graphics/fonts/victor10.fnt");
            TODRAW10 = fontdraw.createText();

        } catch (FontException ex) {
        }
    }
    
    ///////////////////////////////////
    //                               //
    //          UI WIDGET            //
    //                               //
    ///////////////////////////////////
    
    /**
     * Draws a small UI bar next to the ship's system
     *
     * @param ship Ship concerned (the element will only be drawn if that ship
     * is the player ship)
     *
     * @param intendedColor Color of the filling. If null, the filling will be
     * UI-green
     *
     * @param fill Filling level
     *
     * @param blendTime Time to smoothly switch between colors. Can be set to 0
     * for instant changes
     */
    public static void drawSystemBar(ShipAPI ship, Color intendedColor, float fill, float blendTime) {

        if (!ship.isAlive() || ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }

        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
        final float boxWidth = 27f;
        final float boxHeight = 7f;

        // Used to properly interpolate between colors
        final CombatEngineAPI engine = Global.getCombatEngine();
        final float elapsed = engine.getTotalElapsedTime(true);
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }

        // Calculate what color to use        
        Color actualColor;
        if (blendTime > 0) {

            Color targetColor;
            if (intendedColor == null) {
                targetColor = GREENCOLOR;
            } else {
                targetColor = intendedColor;
            }

            if (lastUpdate > elapsed) {
                lastUpdate = elapsed;
            }
            float progress = (elapsed - lastUpdate) / blendTime;
            if (lastColor == null || lastColor == targetColor || progress > 1f) {
                lastColor = targetColor;
                actualColor = lastColor;
                lastUpdate = elapsed;
            } else {
                actualColor = interpolateColor(lastColor, targetColor, progress);
            }
        } else {
            if (intendedColor == null) {
                actualColor = GREENCOLOR;
            } else {
                actualColor = intendedColor;
            }
        }

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        final Vector2f boxLoc = Vector2f.add(new Vector2f(497f, 80f),
                getInterfaceOffsetFromSystemBar(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(498f, 79f),
                getInterfaceOffsetFromSystemBar(ship, ship.getVariant()), null);

        // Render the drop shadow
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxWidth + 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxWidth + 1f, shadowLoc.y + boxHeight + 1f);
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y + boxHeight + 1f);

        // Render the border transparency fix
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y + boxHeight + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxHeight + 1f);

        // Render the border
        GL11.glColor4f(GREENCOLOR.getRed() / 255f, GREENCOLOR.getGreen() / 255f, GREENCOLOR.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y + boxHeight + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxHeight + 1f);

        // Render the background
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
        GL11.glEnd();

        // Render the fill element
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glColor4f(actualColor.getRed() / 255f, actualColor.getGreen() / 255f, actualColor.getBlue() / 255f,
                alpha * (actualColor.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth * fill, boxLoc.y);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth * fill, boxLoc.y + boxHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    /**
     * Draws a small UI box next to the ship's system
     *
     * @param ship Ship concerned (the element will only be drawn if that ship
     * is the player ship)
     *
     * @param intendedColor Color of the filling. If null, the filling will be
     * UI-green
     *
     * @param blendTime Time to smoothly switch between colors. Can be set to 0
     * for instant changes
     */
    public static void drawSystemBox(ShipAPI ship, Color intendedColor, float blendTime) {

        if (!ship.isAlive() || ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }

        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
        final float boxSide = 7f;

        // Used to properly interpolate between colors
        final CombatEngineAPI engine = Global.getCombatEngine();
        final float elapsed = engine.getTotalElapsedTime(true);
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }

        // Calculate what color to use        
        Color actualColor;
        if (blendTime > 0) {

            Color targetColor;
            if (intendedColor == null) {
                targetColor = GREENCOLOR;
            } else {
                targetColor = intendedColor;
            }

            if (lastUpdate > elapsed) {
                lastUpdate = elapsed;
            }
            float progress = (elapsed - lastUpdate) / blendTime;
            if (lastColor == null || lastColor == targetColor || progress > 1f) {
                lastColor = targetColor;
                actualColor = lastColor;
                lastUpdate = elapsed;
            } else {
                actualColor = interpolateColor(lastColor, targetColor, progress);
            }
        } else {
            if (intendedColor == null) {
                actualColor = GREENCOLOR;
            } else {
                actualColor = intendedColor;
            }
        }

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        final Vector2f boxLoc = Vector2f.add(new Vector2f(497f, 80f),
                getInterfaceOffsetFromSystemBar(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(498f, 79f),
                getInterfaceOffsetFromSystemBar(ship, ship.getVariant()), null);

        // Render the drop shadow
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxSide + 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxSide + 1f, shadowLoc.y + boxSide + 1f);
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y + boxSide + 1f);

        // Render the border transparency fix
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxSide + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxSide + 1f, boxLoc.y + boxSide + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxSide + 1f);

        // Render the border
        GL11.glColor4f(GREENCOLOR.getRed() / 255f, GREENCOLOR.getGreen() / 255f, GREENCOLOR.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxSide + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxSide + 1f, boxLoc.y + boxSide + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxSide + 1f);

        // Render the background
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxSide, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxSide, boxLoc.y + boxSide);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxSide);
        GL11.glEnd();

        // Render the fill element
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glColor4f(actualColor.getRed() / 255f, actualColor.getGreen() / 255f, actualColor.getBlue() / 255f,
                alpha * (actualColor.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxSide, boxLoc.y);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxSide);
        GL11.glVertex2f(boxLoc.x + boxSide, boxLoc.y + boxSide);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    ///////////////////////////////////
    //                               //
    //          STATUS BAR           //
    //                               //
    ///////////////////////////////////
    
    /**
     * Draw a third status bar above the Flux and Hull ones on the User Interface.
     * With a text of the left and the number on the right.
     *
     * @param ship Player ship.
     *
     * @param fill Filling level of the bar. 0 to 1
     *
     * @param innerColor Color of the bar. If null, the vanilla green UI color will be used.
     *
     * @param borderColor Color of the border. If null, the vanilla green UI color will be used.
     *
     * @param secondfill Wider filling like the soft/hard-flux. 0 to 1.
     * 
     * @param text The text written to the left, automatically cut if too large.
     * 
     * @param number The number displayed on the right. Can go from 0 to 999 999.
     */
    public static void drawInterfaceStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, String text, int number) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        addInterfaceStatusBar(ship, fill, innerColor, borderColor, secondfill);
        if (TODRAW14 != null) {
            addInterfaceStatusText(ship, text);
            addInterfaceStatusNumber(ship, number);
        }

    }
    /**
     * Draw a status bar next to the player ship on the top left corner of the hud
     * Can write two bits of text on its left side
     * 
     * @param ship Player ship.
     *
     * @param fill Filling level of the bar. 0 to 1
     *
     * @param innerColor Color of the bar. If null, the vanilla green UI color will be used.
     *
     * @param borderColor Color of the border. If null, the vanilla green UI color will be used.
     *
     * @param secondfill Wider filling like the soft/hard-flux. 0 to 1.
     * 
     * @param bottext Write a text just on the left of the bar. Example: 'flux'
     * 
     * @param toptext Write a text just above the bar. Example: 'player' or 'target'
     * 
     * @param offset Offsets the bar a few pixels upward. 
     * Can be used for example to display the targeted enemy special status bar.
     * 
     */
    public static void drawHUDStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, String bottext, String toptext, boolean offset) {
        Vector2f pos = ship.getLocation();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.getViewport() == null) {
            return;
        }
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        Vector2f pos2 = new Vector2f((int) Global.getCombatEngine().getViewport().convertWorldXtoScreenX(pos.getX()), (int) Global.getCombatEngine().getViewport().convertWorldYtoScreenY(pos.getY()));
        if (offset) {
            pos2.translate(0f, 16f);
        }
        addHUDStatusBar(ship, fill, innerColor, borderColor, secondfill, pos2);
        if (TODRAW10 != null) {
            TODRAW10.setText(bottext);
            int pixelleft =(int)TODRAW10.getWidth();
            pos2.translate(-pixelleft-5f, 8f);
            addHUDStatusText(ship, innerColor, pos2);
            pos2.translate(0, 8f);
            TODRAW10.setText(toptext);
            addHUDStatusText(ship, innerColor, pos2);
        }
    }
    
    ///// UTILS /////

    private static Color interpolateColor(Color old, Color dest, float progress) {
        final float clampedProgress = Math.max(0f, Math.min(1f, progress));
        final float antiProgress = 1f - clampedProgress;
        final float[] ccOld = old.getComponents(null), ccNew = dest.getComponents(null);
        return new Color((ccOld[0] * antiProgress) + (ccNew[0] * clampedProgress),
                (ccOld[1] * antiProgress) + (ccNew[1] * clampedProgress),
                (ccOld[2] * antiProgress) + (ccNew[2] * clampedProgress),
                (ccOld[3] * antiProgress) + (ccNew[3] * clampedProgress));
    }
    
    /**
     * Get the UI Element Offset.
     * (Depends on the weapon groups and wings present)
     *
     * @param ship The player ship.
     * @param variant The variant of the ship.
     * @param vec1 Vector2f used if there are no wing and less than 2 weapon groups.
     * @param vec2 Vector2f used if there are wings but less than 2 weapon groups.
     *
     * @return the offset.
     */
    private static Vector2f getUIElementOffset(ShipAPI ship, ShipVariantAPI variant, Vector2f vec1, Vector2f vec2) {
        int numEntries = 0;
        final List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
        final List<WeaponAPI> usableWeapons = ship.getUsableWeapons();
        for (WeaponGroupSpec group : weaponGroups) {
            final Set<String> uniqueWeapons = new HashSet<>(group.getSlots().size());
            for (String slot : group.getSlots()) {
                boolean isUsable = false;
                for (WeaponAPI weapon : usableWeapons) {
                    if (weapon.getSlot().getId().contentEquals(slot)) {
                        isUsable = true;
                        break;
                    }
                }
                if (!isUsable) {
                    continue;
                }
                String id = variant.getWeaponId(slot);
                if (id != null) {
                    uniqueWeapons.add(id);
                }
            }
            numEntries += uniqueWeapons.size();
        }
        if (variant.getFittedWings().isEmpty()) {
            if (numEntries < 2) {
                return vec1;
            }
            return new Vector2f(30f + ((numEntries - 2) * 13f), 18f + ((numEntries - 2) * 26f));
        } else {
            if (numEntries < 2) {
                return vec2;
            }
            return new Vector2f(59f + ((numEntries - 2) * 13f), 76f + ((numEntries - 2) * 26f));
        }
    }
    
    
    /**
     * Draws a small UI bar above the flux bar. The HUD color change to blu when
     * the ship is not alive. Bug: When you left the battle, the hud
     * keep for qew second, no solution found. Bug: Also for other
     * normal drawBox, when paused, they switch brutally of "color".
     * 
     * @param ship Ship concerned (the element will only be drawn if that ship
     * is the player ship)
     * @param fill Filling level
     * @param innerColor Color of the bar. If null, use the vanilla HUD color.
     * @param borderColor Color of the border. If null, use the vanilla HUD
     * color.
     * @param secondfill Like the hardflux of the fluxbar. 0 per default.
     */
    private static void addInterfaceStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill) {

        final int boxWidth = 79;
        final int boxHeight = 7;
        final Vector2f element = getInterfaceOffsetFromStatusBars(ship, ship.getVariant());
        final Vector2f boxLoc = Vector2f.add(new Vector2f(224f, 120f), element, null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(225f, 119f), element, null);

        // Used to properly interpolate between colors
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }

        Color innerCol = innerColor == null ? GREENCOLOR : innerColor;
        Color borderCol = borderColor == null ? GREENCOLOR : borderColor;
        if (!ship.isAlive()) {
            innerCol = BLUCOLOR;
            borderCol = BLUCOLOR;
        }
        float hardfill = secondfill < 0 ? 0 : secondfill;
        hardfill = hardfill > 1 ? 1 : hardfill;
        if (hardfill >= fill) {
            hardfill = fill;
        }
        int pixelHardfill = (int) (boxWidth * hardfill);
        pixelHardfill = pixelHardfill <= 3 ? -pixelHardfill : -3;

        int hfboxWidth = (int) (boxWidth * hardfill);
        int fboxWidth = (int) (boxWidth * fill);

        OpenGLBar(ship, alpha, borderCol, innerCol, fboxWidth, hfboxWidth, boxHeight, boxWidth, pixelHardfill, shadowLoc, boxLoc);
    }

    /**
     * Draw the text with the font victor14.
     * @param ship The player ship
     * @param text The text to write.
     */
    private static void addInterfaceStatusText(ShipAPI ship, String text) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }
        Color borderCol = GREENCOLOR;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }
        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(177f - 1f, 120f + 11f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(178f - 1f, 119f + 11f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);

        openGL11ForText();
        TODRAW14.setText(text);
        TODRAW14.setMaxWidth(46);
        TODRAW14.setMaxHeight(14);
        TODRAW14.setColor(shadowcolor);
        TODRAW14.draw(shadowLoc);
        TODRAW14.setColor(color);
        TODRAW14.draw(boxLoc);
        closeGL11ForText();

    }

    /**
     * Draw number at the right of the percent bar.
     * @param ship The player ship died or alive.
     * @param number The number displayed, bounded per the method to 0 at 999
     * 999.
     */
    private static void addInterfaceStatusNumber(ShipAPI ship, int number) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }
        int numb = number;
        if (numb > 999999) {
            numb = 999999;
        }
        if (numb < 0) {
            numb = 0;
        }

        Color borderCol = GREENCOLOR;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }
        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(356f - 1f, 120f + 11f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(357f - 1f, 119f + 11f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);

        openGL11ForText();
        TODRAW14.setText(numb + "");
        float width = TODRAW14.getWidth() - 1;
        TODRAW14.setColor(shadowcolor);
        TODRAW14.draw(shadowLoc.x - width, shadowLoc.y);
        TODRAW14.setColor(color);
        TODRAW14.draw(boxLoc.x - width, boxLoc.y);
        closeGL11ForText();
    }

    /**
     * Draws a small UI bar above the flux bar. The HUD color change to blu when
     * the ship is not alive. //TODO: Bug: When you left the battle, the hud
     * keep for qew second, no solution found. //TODO: Bug: Also for other
     * normal drawBox, when paused, they switch brutally of "color".
     *
     * @param ship Ship concerned (the element will only be drawn if that ship
     * is the player ship)
     *
     * @param fill Filling level
     *
     * @param innerColor Color of the bar. If null, use the vanilla HUD color.
     *
     * @param borderColor Color of the border. If null, use the vanilla HUD
     * color.
     *
     * @param secondfill Like the hardflux of the fluxbar. 0 per default.
     * @param screenPos The position on the Screen.
     *
     */
    private static void addHUDStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, Vector2f screenPos) {

        final int boxWidth = 59;
        final int boxHeight = 5;

        final Vector2f element = getHUDOffset(ship);
        final Vector2f boxLoc = Vector2f.add(new Vector2f(screenPos.getX(), screenPos.getY()), element, null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f), element, null);

        // Used to properly interpolate between colors
        float alpha = 1f;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }

        Color innerCol = innerColor == null ? GREENCOLOR : innerColor;
        Color borderCol = borderColor == null ? GREENCOLOR : borderColor;
        if (!ship.isAlive()) {
            innerCol = BLUCOLOR;
            borderCol = BLUCOLOR;
        }
        int pixelHardfill = 0;
        float hardfill = secondfill < 0 ? 0 : secondfill;
        hardfill = hardfill > 1 ? 1 : hardfill;
        if (hardfill >= fill) {
            hardfill = fill;
        }
        pixelHardfill = (int) (boxWidth * hardfill);
        pixelHardfill = pixelHardfill <= 3 ? -pixelHardfill : -3;

        int hfboxWidth = (int) (boxWidth * hardfill);
        int fboxWidth = (int) (boxWidth * fill);

        OpenGLBar(ship, alpha, borderCol, innerCol, fboxWidth, hfboxWidth, boxHeight, boxWidth, pixelHardfill, shadowLoc, boxLoc);

    }

    /**
     * Draw text with the font victor10 where you want on the screen.
     *
     * @param ship The player ship.
     * @param textColor The color of the text
     * @param screenPos The position on the Screen.
     */
    private static void addHUDStatusText(ShipAPI ship, Color textColor, Vector2f screenPos) {
        Color borderCol = textColor == null ? GREENCOLOR : textColor;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1f;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }
        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(screenPos.getX(), screenPos.getY()),
                getHUDOffset(ship), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f),
                getHUDOffset(ship), null);

        // Global.getCombatEngine().getViewport().
        openGL11ForText();
       // TODRAW10.setText(text);
       // TODRAW10.setMaxHeight(26);
        TODRAW10.setColor(shadowcolor);
        TODRAW10.draw(shadowLoc);
        TODRAW10.setColor(color);
        TODRAW10.draw(boxLoc);
        closeGL11ForText();
    }

    private static void OpenGLBar(ShipAPI ship, float alpha, Color borderCol, Color innerCol, int fboxWidth, int hfboxWidth, int boxHeight, int boxWidth, int pixelHardfill, Vector2f shadowLoc, Vector2f boxLoc) {
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        if (ship.isAlive()) {
            // Render the drop shadow
            if (fboxWidth != 0) {
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                        1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y + boxHeight + 1);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y + boxHeight + 1);
                GL11.glEnd();
            }
        }

        // Render the drop shadow of border.
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
        GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y + boxHeight);

        // Render the border transparency fix
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);

        // Render the border
        GL11.glColor4f(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
        GL11.glEnd();

        // Render the fill element
        if (ship.isAlive()) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glColor4f(innerCol.getRed() / 255f, innerCol.getGreen() / 255f, innerCol.getBlue() / 255f,
                    alpha * (innerCol.getAlpha() / 255f)
                    * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
            GL11.glVertex2f(boxLoc.x, boxLoc.y);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y);
            GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y + boxHeight);
            GL11.glEnd();
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    /**
     * Get the UI Element Offset for the player on the center. (Depends of the
     * collision radius and the zoom)
     *
     * @param ship The player ship.
     * @return The offset.
     */
    private static Vector2f getHUDOffset(ShipAPI ship) {
        ViewportAPI viewport = Global.getCombatEngine().getViewport();
        float mult = viewport.getViewMult();

        return new Vector2f((int) (-ship.getCollisionRadius() / mult),
                (int) (ship.getCollisionRadius() / mult));
    }

    /**
     * Get the UI Element Offset for the Shipsystem bar. (Depends of the group
     * layout, or if the player has some wing)
     *
     * @param ship The player ship.
     * @param variant The variant of the ship.
     * @return The offset who depends of weapon and wing.
     */
    private static Vector2f getInterfaceOffsetFromSystemBar(ShipAPI ship, ShipVariantAPI variant) {
        return getUIElementOffset(ship, variant, SYSTEMBARVEC1, SYSTEMBARVEC2);
    }

    /**
     * Get the UI Element Offset for the Third bar. (Depends of the group
     * layout, or if the player has some wing)
     *
     * @param ship The player ship.
     * @param variant The variant of the ship.
     * @return The offset who depends of weapon and wing.
     */
    private static Vector2f getInterfaceOffsetFromStatusBars(ShipAPI ship, ShipVariantAPI variant) {
        return getUIElementOffset(ship, variant, PERCENTBARVEC1, PERCENTBARVEC2);
    }

    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
    private static void openGL11ForText() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
        GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    private static void closeGL11ForText() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}