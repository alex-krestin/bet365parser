package it.rehelpstudio.bet365.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.rehelpstudio.bet365.entity.Event;
import it.rehelpstudio.bet365.entity.Team;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.rehelpstudio.bet365.parser.Connection.postRequest;

public class Bet365Parser {
    private static final String BET365_HOME = "https://mobile.bet365.com";
    private String cookies;
    private String homePage;
    private final ArrayList<String> hosts = new ArrayList<>();
    private final ArrayList<String> ports = new ArrayList<>();
    private String clientRn;
    private String clientID;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
    private int connectionAttempts = 0;
    private boolean done = false;
    private final LinkedList<Object> eventsList = new LinkedList<>();

    private static final String RECORD_DELIM = "\\x01";
    private static final String FIELD_DELIM = "\\x02";
    private static final String[] CHANNELS = {"OVInPlay_1_3", "OVInPlay_6_0"};

    private int serverNum;

    public void parseAll() {
        try {
            System.out.println("Connecting to " + BET365_HOME);
            setConnection();
        } catch (Exception e) {
            System.out.println("Can't establish connection with server.");
            if (connectionAttempts < 10 && !done) {
                connectionAttempts++;
                System.out.println("Trying to reconnect... (attempt #" + connectionAttempts + " of 10)");
                parseAll();
            }
        }

        System.out.println("Connected.");

        ArrayList<String> matches = new ArrayList<>();
        boolean success = false;

        for (String CHANNEL : CHANNELS) {
            System.out.println("Trying to get events list from " + CHANNEL + " channel.");
            matches = getAvailableMatches(CHANNEL);
            if (matches != null) {
                System.out.println("Success.");
                success = true;
                break;
            }
            System.out.println("Connection error... Trying next channel...");
        }

        if (success) {
            int matchCount = matches.size();
            System.out.println("Found " + matchCount + " matches.");
            System.out.println("Parsing data...");

            // Request full information for each event
            for (int i=0; i < matches.size(); i++) {
                try {
                    System.out.println("Parse " + (i+1) +" of " + matchCount + " [matchID: " + matches.get(i) + "]");
                    Map<Object, Object> info;
                    info = getSoccerEventInformation(matches.get(i));
                    if (info == null) {
                        System.out.println("Match is out of date.");
                    }
                    else {
                        eventsList.add(info);
                    }
                } catch (IOException e) {
                    System.out.println("Error parsing match id " + matches.get(i));
                    System.out.println("debug > " + e.getMessage());
                }
            }

            System.out.println("Create JSON file...");
            saveJSON("soccer_events");
            System.out.println("All done :)");
        }
        else {
            System.out.println("Can't receive matches ids");
        }

        done = true;
    }


    private ArrayList<String> getAvailableMatches(String channel) {
        ArrayList<String> matches = new ArrayList<>();

        try {
            matches = getEvents(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matches;
    }

    private void setConnection() throws Exception {
        getCookies();
        if (cookies.isEmpty()) return;

        String sessionID = getProperty("sessionId");
        if (sessionID == null) return;

        String connectionDetails = getProperty("ConnectionDetails");
        if (connectionDetails == null || connectionDetails.isEmpty()) return;

        setConnectionDetails(connectionDetails);
        if (hosts.size() != 2 || ports.size() != 2) return;

        // Generate random Client RN
        String characters = "1234567890";
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            double index = Math.random() * 10;
            buffer.append(characters.charAt((int) index));
        }
        clientRn = buffer.toString();

        // Send POST request to get clientID
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("method", "0"));
        headers.add(new BasicHeader("topic", "__time,S_" + sessionID));
        headers.add(new BasicHeader("transporttimeout", "20"));
        headers.add(new BasicHeader("type", "F"));

        String postResponce = powRequest(0, headers);
        String[] temp = postResponce.split(FIELD_DELIM);
        clientID = temp[1];
    }

    private void setConnectionDetails(String connectionDetails) {
        String pattern = "Host\":\"(.*?)\"";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(connectionDetails);

        while (m.find()) {
            hosts.add(m.group(1));
        }

        pattern = "Port\":(.*?),";
        p = Pattern.compile(pattern);
        m = p.matcher(connectionDetails);

        while (m.find()) {
            ports.add(m.group(1));
        }
    }

    private String getProperty(String property) {

        String pattern;

        switch (property) {
            case "sessionId":
                pattern = property + "\":\"(.*?)\"";
                break;
            case "ConnectionDetails":
                pattern = property + "\":\\[(.*?)\\]";
                break;
            default:
                return null;
        }

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(homePage);

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private void getCookies() throws IOException {
        CookieStore store = new CustomCookieStore();
        CookiePolicy policy = new CustomCookiePolicy();
        CookieManager handler = new CookieManager(store, policy);
        CookieHandler.setDefault(handler);

        URL url = new URL(BET365_HOME);

        URLConnection conn = url.openConnection();

        // load homepage html
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder html = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            html.append(inputLine);
        }
        in.close();

        homePage = html.toString();

        // set cookies
        String str = store.getCookies().toString();
        cookies = str.substring(1, str.length() - 1);
    }

    private ArrayList<String> getEvents(String channel) throws IOException {

        // Get actual events list
        subscribe(channel);
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("method", "1"));

        String gameDataRequest = powRequest(2, headers);

        String[] gameData = gameDataRequest.split(RECORD_DELIM);
        gameData = gameData[gameData.length -1].split("\\|");
        gameData = Arrays.copyOfRange(gameData, 1, gameData.length); // remove F

        Map<String, Map<String, String>> initialCL = parameterizeLine(gameData[0]);
        Map<String, String> params = new HashMap<>();

        if (initialCL != null) {
            params = initialCL.get("CL");
        }

        if (params == null) return null;

        ArrayList<String> events = new ArrayList<>();

        // skip the initial CL (soccer)
        for(int i = 1; i < gameData.length; i++) {
            Map<String, Map<String, String>> lineData = parameterizeLine(gameData[i]);

            if (lineData == null)
                continue;

            if(lineData.containsKey("EV")) {
                if (lineData.get("EV").get("ID").length() == 17) {
                    events.add(lineData.get("EV").get("ID"));
                }
            }
        }

        unsubscribe(channel);

        return events;
    }

    private Map<String, Map<String, String>> parameterizeLine(String line) {

        String[] chunk = line.split(";");

        if (chunk.length == 0)
            return null;

        String cmd = chunk[0];

        // remove cmd element
        chunk = Arrays.copyOfRange(chunk, 1, chunk.length);

        Map<String,  Map<String, String>> map = new HashMap<>();
        Map<String, String> params = new HashMap<>();

        for (String pstr : chunk) {
            String[] pdata = pstr.split("=");

            if (pdata.length == 2) {
                params.put(pdata[0], pdata[1]);
            }
        }

        map.put(cmd, params);

        return map;
    }

    private String powRequest(int sid, ArrayList<Header> specialHeaders) throws IOException {
        ArrayList<Header> defaultHeaders = new ArrayList<>();

        defaultHeaders.add(new BasicHeader("Content-Type", "text/plain; charset=UTF-8"));
        defaultHeaders.add(new BasicHeader("Referer", BET365_HOME + "/"));
        defaultHeaders.add(new BasicHeader("Origin", BET365_HOME));
        defaultHeaders.add(new BasicHeader("User-Agent", USER_AGENT));

        if (clientID != null) {
            defaultHeaders.add(new BasicHeader("clientid", clientID));
        }

        if(sid != 0) {
            defaultHeaders.add(new BasicHeader("s", String.valueOf(serverNum)));
            serverNum++;
        }

        ArrayList<Header> totalHeaders = new ArrayList<>(defaultHeaders);
        totalHeaders.addAll(specialHeaders);

        String url = hosts.get(1) + "/pow/?sid=" + sid + "&rn=" + clientRn;

        return postRequest(url, totalHeaders);
    }

    private void subscribe(String channel) throws IOException {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("method", "22"));
        headers.add(new BasicHeader("topic", channel));
        powRequest(2, headers);
    }

    private void unsubscribe(String channel) throws IOException {
        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("method", "23"));
        headers.add(new BasicHeader("topic", channel));
        powRequest(2, headers);
    }

    private Map<Object, Object> getSoccerEventInformation(String id) throws IOException {
        subscribe(id);

        ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("method", "1"));
        String requestPow = powRequest(2, headers);
        String[] eventExpandedData = requestPow.split(RECORD_DELIM);
        eventExpandedData = eventExpandedData[eventExpandedData.length - 1].split("\\|");

        ArrayList<Map<String, String>> result = new ArrayList<>();
        Map<String, String> currentRoot;
        result.add(new HashMap<>());
        result.add(new HashMap<>());
        int currentTeam;
        boolean firstItem = true;
        String currentKey = null;
        String competitionType = null;


        for (String anEventExpandedData : eventExpandedData) {
            Map<String, Map<String, String>> parsedLine = parameterizeLine(anEventExpandedData);

            if (parsedLine == null)
                continue;

            if (parsedLine.containsKey("EV")) { //Event
                currentRoot = parsedLine.get("EV");
                competitionType = currentRoot.get("CT");
            }
            else if (parsedLine.containsKey("SC")) {
                currentRoot = parsedLine.get("SC");
                if (firstItem) {
                    currentKey = "name";
                    firstItem = false;
                }
                else {
                    currentKey = currentRoot.get("NA");
                }
            }
            else if (parsedLine.containsKey("SL")) {
                currentRoot = parsedLine.get("SL");
                if (Objects.equals(currentRoot.get("OR"), "0")) {
                    currentTeam = 0;
                }
                else {
                    currentTeam = 1;
                }
                result.get(currentTeam).put(currentKey, currentRoot.get("D1"));
            }
            else if (parsedLine.containsKey("TE")) {
                currentRoot = parsedLine.get("TE");
                if (Objects.equals(currentRoot.get("OR"), "0")) {
                    currentTeam = 0;
                }
                else {
                    currentTeam = 1;
                }

                for (int stat = 1; stat < 9; stat++) {
                    if (currentRoot.containsKey("S" + stat)) {
                        if (currentRoot.get("S" + stat).isEmpty()) {
                            continue;
                        }
                        result.get(currentTeam).put("S" + stat, currentRoot.get("S" + stat));
                    }
                }
            }
        }

        unsubscribe(id);

        if (competitionType == null) return null;

        Team team1 = new Team(result.get(0).get("name"), result.get(0).get("IGoal"), result.get(0).get("IPenalty"),
                result.get(0).get("S3"), result.get(0).get("S4"), result.get(0).get("S7"), result.get(0).get("S1"),
                result.get(0).get("S2"), result.get(0).get("ISubstitution"), result.get(0).get("ICorner"),
                result.get(0).get("IYellowCard"), result.get(0).get("IRedCard"));

        Team team2 = new Team(result.get(1).get("name"), result.get(1).get("IGoal"), result.get(1).get("IPenalty"),
                result.get(1).get("S3"), result.get(1).get("S4"), result.get(1).get("S7"), result.get(1).get("S1"),
                result.get(1).get("S2"), result.get(1).get("ISubstitution"), result.get(1).get("ICorner"),
                result.get(1).get("IYellowCard"), result.get(1).get("IRedCard"));

        Event event = new Event(id, competitionType, team1, team2);

        return event.toJSON();
    }

    private void saveJSON(String filename) {
        try {
            FileWriter file = new FileWriter(filename + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(eventsList);
            file.write(jsonOutput);
            file.flush();
            file.close();

        } catch (IOException e) {
            System.out.println("Can't create json file.");
        }
    }
}
