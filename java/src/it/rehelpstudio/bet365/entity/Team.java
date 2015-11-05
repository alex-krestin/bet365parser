package it.rehelpstudio.bet365.entity;

public class Team {
    private final String name;
    private int goal = 0;
    private int penalty = 0;
    private int attack = 0;
    private int dangerousAttack = 0;
    private int possession = 0;
    private int onTarget = 0;
    private int offTarget = 0;
    private int substitution = 0;
    private int corner = 0;
    private int yellowCard = 0;
    private int redCard = 0;
    //private String throwIn;
    //private String goalKick;
    //private String freeKick;


    public Team(String name, String goal, String penalty, String attack, String dangerousAttack, String possession,
                String onTarget, String offTarget, String substitution, String corner, String yellowCard, String redCard) {
        this.name = name;
        try {
            this.goal = Integer.parseInt(goal);
        } catch (Exception e) {/*skip*/}
        try {
            this.penalty = Integer.parseInt(penalty);
        } catch (Exception e) {/*skip*/}
        try {
            this.attack = Integer.parseInt(attack);
        } catch (Exception e) {/*skip*/}
        try {
            this.dangerousAttack = Integer.parseInt(dangerousAttack);
        } catch (Exception e) {/*skip*/}

        try {
            this.possession = Integer.parseInt(possession);
        } catch (Exception e) {/*skip*/}

        try {
            this.onTarget = Integer.parseInt(onTarget);
        } catch (Exception e) {/*skip*/}

        try {
            this.offTarget = Integer.parseInt(offTarget);
        } catch (Exception e) {/*skip*/}
        try {
            this.substitution = Integer.parseInt(substitution);
        } catch (Exception e) {/*skip*/}
        try {
            this.corner = Integer.parseInt(corner);
        } catch (Exception e) {/*skip*/}
        try {
            this.yellowCard = Integer.parseInt(yellowCard);
        } catch (Exception e) {/*skip*/}
        try {
            this.redCard = Integer.parseInt(redCard);
        } catch (Exception e) {/*skip*/}
    }

    public String getName() {
        return name;
    }

    public int getGoal() {
        return goal;
    }

    public int getPenalty() {
        return penalty;
    }

    public int getAttack() {
        return attack;
    }

    public int getDangerousAttack() {
        return dangerousAttack;
    }

    public int getPossession() {
        return possession;
    }

    public int getOnTarget() {
        return onTarget;
    }

    public int getOffTarget() {
        return offTarget;
    }

    public int getSubstitution() {
        return substitution;
    }

    public int getCorner() {
        return corner;
    }

    public int getYellowCard() {
        return yellowCard;
    }

    public int getRedCard() {
        return redCard;
    }
}
