package ShowFleetDP;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class ShowFleetDPPlugin extends BaseModPlugin {
    public ShowFleetDPPlugin() {
    }

    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientListener(new ShowFleetDPListener());
    }

}
