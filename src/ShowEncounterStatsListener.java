package ShowEncounterStats;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI; 
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import ShowEncounterStats.ShowEncounterStatsSettings;

public class ShowEncounterStatsListener extends BaseCampaignEventListener {

    public ShowEncounterStatsListener() {
        super(false);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {

        Object contextObj = dialog.getPlugin().getContext();
        if (!(contextObj instanceof FleetEncounterContext context)) {
            return;
        }

        BattleAPI battle = context.getBattle();
        if (battle == null) return;

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        BattleAPI.BattleSide playerSide = battle.pickSide(playerFleet);
        if (playerSide == null) return;

        CampaignFleetAPI playerCombined = battle.getCombined(playerSide);
        CampaignFleetAPI enemyCombined = battle.getOtherSideCombined(playerSide);
        if (playerCombined == null || enemyCombined == null) return;

        FleetStats playerStats = computeFleetStats(playerCombined);
        FleetStats enemyStats = computeFleetStats(enemyCombined);

        TextPanelAPI text = dialog.getTextPanel();

        text.addPara(
                buildLine("Your side", playerStats),
                Misc.getTextColor(),
                Misc.getHighlightColor(),
                playerStats.highlights(
                ShowEncounterStatsSettings.countCivShips,
                ShowEncounterStatsSettings.showofficerCount
				)
        );

        text.addPara(
                buildLine("Opposing side", enemyStats),
                Misc.getTextColor(),
                Misc.getHighlightColor(),
                enemyStats.highlights(
					ShowEncounterStatsSettings.countCivShips,
					ShowEncounterStatsSettings.showofficerCount
				)
        );

        float difficulty = context.computeBattleDifficulty();
        if (difficulty > 1f) {
            int bonusPercent = Math.round((difficulty - 1f) * 100f);
            text.addPara(
                    "Expected bonus EXP due to battle difficulty: %s.",
                    Misc.getTextColor(),
                    Misc.getHighlightColor(),
                    "+" + bonusPercent + "%"
            );
        }
    }

    /* ====================================================================== */
    /* ============================= TEXT BUILD ============================== */
    /* ====================================================================== */

    private String buildLine(String sideName, FleetStats s) {

        boolean shorten = ShowEncounterStatsSettings.shortenDescriptions;
        boolean showCivFP = ShowEncounterStatsSettings.showCivFP;
        boolean countCivShips = ShowEncounterStatsSettings.countCivShips;
        boolean showOfficers = ShowEncounterStatsSettings.showofficerCount;

        int ships = countCivShips ? s.shipCount : s.combatShipCount;
        String shipLabel = (ships == 1) ? "ship" : "ships";
		
		int deploy = countCivShips ? (int) s.totalDP : (int) s.combatDP;
		
        String dpLabel = shorten ? "DP" : "deployment " + (deploy == 1 ? "point" : "points");
        String fpLabel = shorten ? "FP" : "fleet strength";

        StringBuilder line = new StringBuilder();
        line.append(sideName).append(": ");
        line.append((int) ships).append(" ").append(shipLabel);

        if (showOfficers && s.officerCount > 0) {
            String officerLabel = (s.officerCount == 1) ? "officer" : "officers";
            int avgLvl = Math.round((float) s.officerLevelSum / s.officerCount);

            if (shorten) {
                line.append(", ").append(s.officerCount)
                        .append(" ").append(officerLabel)
                        .append(" (avg lvl ").append(avgLvl).append(")");
            } else {
                line.append(", ").append(s.officerCount)
                        .append(" ").append(officerLabel)
                        .append(" (average level of ").append(avgLvl).append(")");
            }
        }
		
        line.append(", ").append((int) deploy).append(" ").append(dpLabel);
        line.append(", ").append((int) s.totalFP).append(" ").append(fpLabel);

        if (showCivFP && s.civFP > 0) {
            if (shorten) {
                line.append(" (").append((int) s.civFP).append(" from civs)");
            } else {
                line.append(" (").append((int) s.civFP).append(" from civilian ships)");
            }
        }

        return line.toString();
    }

    /* ====================================================================== */
    /* ============================ COMPUTATION ============================== */
    /* ====================================================================== */

    private FleetStats computeFleetStats(CampaignFleetAPI fleet) {

        FleetStats stats = new FleetStats();

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {

            boolean isCivilian = member.getHullSpec()
					.getHints()
					.contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN);

            boolean isMothballed = member.isMothballed();

            if (!isMothballed) {
				float strenght = Misc.getMemberStrength(member, true, true, true);
				stats.totalFP += strenght;
				stats.shipCount++;
				float DP = member.getDeploymentPointsCost();
				stats.totalDP += DP;
				if (isCivilian) {
					stats.civFP += strenght;	
				} else {
						stats.combatShipCount++;
						stats.combatDP += DP;
				}
            }

            PersonAPI captain = member.getCaptain();
			if (captain != null
					&& !captain.isPlayer()
					&& ("officer".equals(captain.getPostId())
						|| "commander".equals(captain.getPostId()))) {

				stats.officerCount++;
				stats.officerLevelSum += captain.getStats().getLevel();
				}
			}

        return stats;
    }

    /* ====================================================================== */
    /* ============================== DATA =================================== */
    /* ====================================================================== */

    private static class FleetStats {
        int shipCount = 0;
        int combatShipCount = 0;
		
		float strenght = 0f;
		float DP = 0f;
		
        float totalDP = 0f;
		float combatDP = 0f;
        float totalFP = 0f;
        float civFP = 0f;

        int officerCount = 0;
        int officerLevelSum = 0;

        String[] highlights(boolean countCivShips, boolean showOfficers) {

			int ships = countCivShips ? shipCount : combatShipCount;
			int deploy = (int) (countCivShips ? totalDP : combatDP);

			int avgLvl = (officerCount > 0)
				? Math.round((float) officerLevelSum / officerCount)
				: 0;

			return new String[] {
				String.valueOf(ships),
				showOfficers && officerCount > 0 ? String.valueOf(officerCount) : "",
				showOfficers && officerCount > 0 ? String.valueOf(avgLvl) : "",
				String.valueOf(deploy),
				String.valueOf((int) totalFP),
				civFP > 0 ? String.valueOf((int) civFP) : ""
			};
		}
    }
}
