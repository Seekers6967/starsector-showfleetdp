package ShowEncounterStats;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class ShowEncounterStatsPlugin extends BaseModPlugin {
    public ShowEncounterStatsPlugin() {
    }

    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientListener(new ShowEncounterStatsListener());
    }

}
