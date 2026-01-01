package ShowFleetDP;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;

public class ShowFleetDPListener extends BaseCampaignEventListener {
    public ShowFleetDPListener() {
        super(false);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        SectorEntityToken target = dialog.getInteractionTarget();

        if (!(target instanceof CampaignFleetAPI)) return;

        CampaignFleetAPI clickedFleet = (CampaignFleetAPI) target;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        BattleAPI battle = clickedFleet.getBattle();

        if (battle == null) return;

        BattleAPI.BattleSide playerSide = battle.pickSide(playerFleet);
        BattleAPI.BattleSide enemySide = (playerSide == BattleAPI.BattleSide.ONE) ? BattleAPI.BattleSide.TWO : BattleAPI.BattleSide.ONE;

        CampaignFleetAPI playerCombined = battle.getCombined(playerSide);
        CampaignFleetAPI enemyCombined = battle.getCombined(enemySide);

        if (playerCombined == null || enemyCombined == null) return;

        TextPanelAPI textPanel = dialog.getTextPanel();

        FleetStats playerStats = computeFleetStats(playerCombined);
        FleetStats enemyStats = computeFleetStats(enemyCombined);

        String playerLine = String.format("Your side: %s combat%s, %s deployment%s, %s fleet strength%s",
                "" + playerStats.combatShips, playerStats.combatShips == 1 ? " ship" : " ships",
                "" + (int) playerStats.combatDP, playerStats.combatDP == 1 ? " point" : " points",
                "" + (int) playerStats.totalStrength,
                playerStats.civilianStrength > 0 ? " (" + (int) playerStats.civilianStrength + " from civilian ships)" : ""
        );

        String enemyLine = String.format("Opposing side: %s combat%s, %s deployment%s, %s fleet strength%s",
                "" + enemyStats.combatShips, enemyStats.combatShips == 1 ? " ship" : " ships",
                "" + (int) enemyStats.combatDP, enemyStats.combatDP == 1 ? " point" : " points",
                "" + (int) enemyStats.totalStrength,
                enemyStats.civilianStrength > 0 ? " (" + (int) enemyStats.civilianStrength + " from civilian ships)" : ""
        );

        textPanel.addPara(playerLine,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "" + playerStats.combatShips, "" + (int) playerStats.combatDP, "" + (int) playerStats.totalStrength,
                playerStats.civilianStrength > 0 ? "" + (int) playerStats.civilianStrength : ""
        );

        textPanel.addPara(enemyLine,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "" + enemyStats.combatShips, "" + (int) enemyStats.combatDP, "" + (int) enemyStats.totalStrength,
                enemyStats.civilianStrength > 0 ? "" + (int) enemyStats.civilianStrength : ""
        );

        Object contextObj = dialog.getPlugin().getContext();
        if (contextObj instanceof FleetEncounterContext) {
            FleetEncounterContext context = (FleetEncounterContext) contextObj;
            float difficulty = context.computeBattleDifficulty();
            if (difficulty > 1f) {
                int bonusPercent = (int) Math.round((difficulty - 1f) * 100f);
                textPanel.addPara("Expected bonus EXP due to battle difficulty: %s.",
                        Misc.getTextColor(), Misc.getHighlightColor(),
                        "+" + bonusPercent + "%"
                );
            }
        }
    }

    private FleetStats computeFleetStats(CampaignFleetAPI fleet) {
        FleetStats stats = new FleetStats();

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            float memberStrength = Misc.getMemberStrength(member, true, true, true);
            boolean isCivilian = member.getHullSpec().getHints().toString().contains("CIVILIAN");
            boolean isMothballed = member.isMothballed();

            stats.totalStrength += memberStrength;

            if (isCivilian) {
                stats.civilianStrength += memberStrength;
            } else if (!isMothballed) {
                stats.combatShips++;
                stats.combatDP += member.getDeploymentPointsCost();
            }
        }

        return stats;
    }

    private static class FleetStats {
        int combatShips = 0;
        float combatDP = 0f;
        float totalStrength = 0f;
        float civilianStrength = 0f;
    }
}
