package ShowEncounterStats;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class ShowEncounterStatsSettings implements LunaSettingsListener {

    private static final String MOD_ID = "show_encounter_stats";
	
	public static boolean shortenDescriptions = LunaSettings.getBoolean(MOD_ID, "shortenDescriptions");
	public static boolean showCivFP = LunaSettings.getBoolean(MOD_ID, "showCivFP");
	public static boolean countCivShips = LunaSettings.getBoolean(MOD_ID, "countCivShips");
	public static boolean showofficerCount = LunaSettings.getBoolean(MOD_ID, "showofficerCount");


    @Override
    public void settingsChanged(String s) {
        applySettings();
    }

    private void applySettings() {
        shortenDescriptions = LunaSettings.getBoolean(MOD_ID, "shortenDescriptions");
        showCivFP = LunaSettings.getBoolean(MOD_ID, "showCivFP");
		countCivShips = LunaSettings.getBoolean(MOD_ID, "countCivShips");
        showofficerCount = LunaSettings.getBoolean(MOD_ID, "showofficerCount");

    }
}
