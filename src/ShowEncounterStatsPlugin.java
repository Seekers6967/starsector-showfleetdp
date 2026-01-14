package ShowEncounterStats;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import ShowEncounterStats.ShowEncounterStatsSettings;
import lunalib.lunaSettings.LunaSettings;

public class ShowEncounterStatsPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientListener(new ShowEncounterStatsListener());

        LunaSettings.addSettingsListener(new ShowEncounterStatsSettings());
    }
}
