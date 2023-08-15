package org.magiclib.achievements;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Date;

/**
 * Note: this class is serialized to the save file.
 *
 * @author Wisp
 * @since 1.3.0
 */
public class MagicAchievement {
    private final Logger logger;
    @NotNull MagicAchievementSpec spec;

    @Nullable
    private Float progress = null;
    @Nullable
    private Float maxProgress = null;
    @Nullable
    private Date dateCompleted;
    @Nullable
    private String completedByUserId;
    @Nullable
    private String completedByUserName;

    /**
     * By default, only run the achievement advance check every 2-5 seconds for performance reasons.
     */
    private IntervalUtil advanceInterval = new IntervalUtil(2f, 5f);

    public MagicAchievement(@NotNull MagicAchievementSpec spec) {
        logger = Global.getLogger(this.getClass());
        this.spec = spec;
    }

    /**
     * Called each time the achievement is loaded, for example when the game is loaded.
     * Place any code here that registers itself with the game, such as adding a listener.
     */
    public void onCreated() {
    }

    public void beforeGameSave() {
    }

    public void afterGameSave() {
    }

    /**
     * Do any cleanup logic here, e.g. removing listeners.
     */
    public void onDestroyed() {
    }

    /**
     * Call when the achievement is completed.
     * Sets the date completed and the player who completed it.
     *
     * @param completedByPlayer The player's character who completed the achievement, if applicable.
     */
    public void completeAchievement(@Nullable PersonAPI completedByPlayer) {
        this.dateCompleted = new Date();

        if (completedByPlayer != null) {
            this.completedByUserId = completedByPlayer.getId();
            this.completedByUserName = completedByPlayer.getName().getFullName();
        }
    }

    protected void advanceInternal(float amount) {
        advanceInterval.advance(amount);

        if (advanceInterval.intervalElapsed()) {
            advance(advanceInterval.getElapsed());
        }
    }

    public void advance(float amount) {
        logger.info("My name is " + spec.getId() + " and I'm advancing by " + amount);
    }

    public void saveChanges() {
        MagicAchievementManager.getInstance().saveAchievements();
    }

    @Nullable
    public JSONObject toJsonObject() {
        try {
            JSONObject jsonObject = spec.toJsonObject();
            jsonObject.put("progress", progress);
            jsonObject.put("maxProgress", maxProgress);
            jsonObject.put("dateCompleted", dateCompleted == null ? null : dateCompleted.getTime());
            jsonObject.put("completedByUserId", completedByUserId);
            jsonObject.put("completedByUserName", completedByUserName);
            return jsonObject;
        } catch (Exception e) {
            logger.warn("Unable to convert achievement to JSON.", e);
            return null;
        }
    }

    public static MagicAchievement fromJsonObject(@NotNull JSONObject jsonObject) {
        try {
            MagicAchievement achievement = new MagicAchievement(MagicAchievementSpec.fromJsonObject(jsonObject));
            achievement.progress = (float) jsonObject.optDouble("progress", 0);
            achievement.maxProgress = (float) jsonObject.optDouble("maxProgress", 0);
            long dateCompletedTimestamp = jsonObject.optLong("dateCompleted", 0);
            achievement.dateCompleted = dateCompletedTimestamp == 0 ? null : new Date(dateCompletedTimestamp);
            achievement.completedByUserId = jsonObject.optString("completedByUserId", null);
            achievement.completedByUserName = jsonObject.optString("completedByUserName", null);
            return achievement;
        } catch (Exception e) {
            Global.getLogger(MagicAchievement.class).warn("Unable to convert achievement from JSON.", e);
            return null;
        }
    }

    public @Nullable Float getProgress() {
        return progress;
    }

    public void setProgress(@Nullable Float progress) {
        this.progress = progress;
    }

    public @Nullable Float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(@Nullable Float maxProgress) {
        this.maxProgress = maxProgress;
    }

    public @NotNull String getModId() {
        return spec.getModId();
    }

    public @NotNull String getSpecId() {
        return spec.getId();
    }

    public @NotNull String getName() {
        return spec.getName();
    }

    public void setName(@NotNull String name) {
        spec.setName(name);
    }

    public @NotNull String getDescription() {
        return spec.getDescription();
    }

    public void setDescription(@NotNull String description) {
        spec.setDescription(description);
    }

    public @NotNull String getScript() {
        return spec.getScript();
    }

    public void setScript(@NotNull String script) {
        spec.setScript(script);
    }

    public @Nullable String getImage() {
        return spec.getImage();
    }

    public void setImage(@Nullable String image) {
        spec.setImage(image);
    }

    public boolean getHasProgressBar() {
        return spec.getHasProgressBar();
    }

    public void setHasProgressBar(boolean hasProgressBar) {
        spec.setHasProgressBar(hasProgressBar);
    }

    public @NotNull SpoilerLevel getSpoilerLevel() {
        return spec.getSpoilerLevel();
    }

    public void setSpoilerLevel(@NotNull SpoilerLevel spoilerLevel) {
        spec.setSpoilerLevel(spoilerLevel);
    }

    public boolean isComplete() {
        return dateCompleted != null;
    }

    public @Nullable Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(@Nullable Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public @Nullable String getCompletedByUserId() {
        return completedByUserId;
    }

    public void setCompletedByUserId(@Nullable String completedByUserId) {
        this.completedByUserId = completedByUserId;
    }

    public @Nullable String getCompletedByUserName() {
        return completedByUserName;
    }

    public void setCompletedByUserName(@Nullable String completedByUserName) {
        this.completedByUserName = completedByUserName;
    }

    /**
     * Returns the time interval in seconds between each call to {@link #advance(float)}.
     */
    public IntervalUtil getAdvanceIntervalUtil() {
        return advanceInterval;
    }

    /**
     * Sets the time interval between each call to {@link #advance(float)}.
     */
    public void setAdvanceIntervalUtil(IntervalUtil interval) {
        advanceInterval = interval;
    }
}
