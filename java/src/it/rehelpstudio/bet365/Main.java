package it.rehelpstudio.bet365;

import it.rehelpstudio.bet365.parser.Bet365Parser;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Bet365 Parser by ReHelp Studio v1.0");
        Bet365Parser p = new Bet365Parser();
        p.parseAll();
    }
}
