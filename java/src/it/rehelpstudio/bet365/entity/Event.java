package it.rehelpstudio.bet365.entity;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Event {
    private final String eventID;
    private final String competitionType;
    private final Team team1;
    private final Team team2;

    public Event(String eventID, String competitionType, Team team1, Team team2) {
        this.eventID = eventID;
        this.competitionType = competitionType;
        this.team1 = team1;
        this.team2 = team2;
    }

    public Map<Object, Object> toJSON() {
        Map<Object, Object> event = new LinkedHashMap<>();
        event.put("eventID",eventID);
        event.put("competitionType", competitionType);
        event.put("competitionName", team1.getName() + " vs " + team2.getName());
        event.put("currentResult", team1.getGoal() + " : " + team2.getGoal());

        Map<Object, Object> team1map = new LinkedHashMap<>();
        team1map.put("name", team1.getName());
        team1map.put("goal", team1.getGoal());
        team1map.put("penalty", team1.getPenalty());
        team1map.put("attack", team1.getAttack());
        team1map.put("dangerousAttack", team1.getDangerousAttack());
        team1map.put("possession", team1.getPossession());
        team1map.put("onTarget", team1.getOnTarget());
        team1map.put("offTarget", team1.getOffTarget());
        team1map.put("substitution", team1.getSubstitution());
        team1map.put("corner", team1.getCorner());
        team1map.put("yellowCard", team1.getYellowCard());
        team1map.put("redCard", team1.getRedCard());

        Map<Object, Object> team2map = new LinkedHashMap<>();
        team2map.put("name", team2.getName());
        team2map.put("goal", team2.getGoal());
        team2map.put("penalty", team2.getPenalty());
        team2map.put("attack", team2.getAttack());
        team2map.put("dangerousAttack", team2.getDangerousAttack());
        team2map.put("possession", team2.getPossession());
        team2map.put("onTarget", team2.getOnTarget());
        team2map.put("offTarget", team2.getOffTarget());
        team2map.put("substitution", team2.getSubstitution());
        team2map.put("corner", team2.getCorner());
        team2map.put("yellowCard", team2.getYellowCard());
        team2map.put("redCard", team2.getRedCard());

        LinkedList<Object> list = new LinkedList<>();
        list.add(team1map);
        list.add(team2map);

        event.put("teams", list);

        return event;
    }
}
