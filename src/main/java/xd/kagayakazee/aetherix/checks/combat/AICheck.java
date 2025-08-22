package xd.kagayakazee.aetherix.checks.combat;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import xd.kagayakazee.aetherix.Main;
import xd.kagayakazee.aetherix.checks.Check;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.player.PlayerData;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
// WARNING! author DAUN
// я не знаю нахуя оно существует когда есть slothAC 🐍🐍🐍🐍





// как же оно воняет
public class AICheck extends Check implements PacketCheck {

    private static final int SEQUENCE_LENGTH = 40;

    private static final int API_TIMEOUT_SECONDS = 2;
    private static final int STEP_VALUE = 10;

    private static final double PUNISHMENT_THRESHOLD = 0.90;
    private static final double FORGIVENESS_THRESHOLD = 0.50;
    private static final double VL_INCREASE_MULTIPLIER = 100.0;
    private static final double VL_DECREASE_AMOUNT = 0.25;
    private static final double FLAG_THRESHOLD = 50.0;
    private static final double VL_RESET_AMOUNT = 25.0;



    private long lastApiLatency = -1;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(API_TIMEOUT_SECONDS))
            .build();


    private static final ObjectMapper MSGPACK_MAPPER = new ObjectMapper(new MessagePackFactory());


    private final Deque<TickData> recentTicks = new ArrayDeque<>(SEQUENCE_LENGTH);
    private boolean apiRequestInProgress = false;
    private int ticksStep = 0;
    private double violationLevel = 0.0;
    public double lastProbability = 0.0;

    public AICheck(PlayerData playerData) {
        super(playerData, "AI (Aim)");
    }

    private String getSecretKey() {
        return "JKsjdfkjsd(*!@&*d8synbczjxi*&A*&1i2jhnzxc";
    }

   private String getApiUrl() {
        return "http://172.18.0.1:25579/predict";
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
           {
                playerData.setAttackedThisTick(true);
            }
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            if (playerData.getTicksSinceServerPosAdjust() < 15) {
                recentTicks.clear();
                ticksStep = 0;
                return;
            }


            if (playerData.getPacketStateData().getHittiks() > SEQUENCE_LENGTH) {
                if (!recentTicks.isEmpty()) {
                    recentTicks.clear();
                    violationLevel = Math.max(0, violationLevel * 0.9);
                }
                ticksStep = 0;
                return;
            }


            if (playerData.getPacketStateData().isLastPacketWasOnePointSeventeenDuplicate()) {
                if (!recentTicks.isEmpty()) {
                    recentTicks.removeLast();
                }

            }

            recentTicks.addLast(new TickData(playerData, playerData.isAttackedThisTick()));
            playerData.setAttackedThisTick(false); 

            if (recentTicks.size() > SEQUENCE_LENGTH) {
                recentTicks.removeFirst();
            }

            ticksStep++;
            if (recentTicks.size() == SEQUENCE_LENGTH && !apiRequestInProgress && ticksStep >= STEP_VALUE) {
                sendDataToApi();
                ticksStep = 0;
            }
        }
    }


    private void sendDataToApi() {
        apiRequestInProgress = true;
        final List<TickData> dataToSend = new ArrayList<>(recentTicks);

        new BukkitRunnable() {
            @Override
            public void run() {
                long startTime = 0;
                try {
                    byte[] requestBody = serializeToMessagePack(dataToSend);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(getApiUrl()))
                            .timeout(Duration.ofSeconds(API_TIMEOUT_SECONDS))
                            .header("Content-Type", "application/x-msgpack")
                            .header("X-Secret-Key", getSecretKey())
                            .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                            .build();

                    startTime = System.currentTimeMillis();
                    HttpResponse<String> response = sendRequestWithRetry(request);
                    long endTime = System.currentTimeMillis();
                    lastApiLatency = endTime - startTime;

                    if (response != null && response.statusCode() == 200) {
                        handleApiResponse(response.body());
                    } else if (response != null) {
                       //  System.err.println("[AICheck ERROR] API responded with code: " + response.statusCode());
                    }

                } catch (JsonProcessingException e) {
                      // System.err.println("[AICheck ERROR] Failed to serialize data: " + e.getMessage());
                } finally {
                    if (startTime > 0 && lastApiLatency < 0) {
                        lastApiLatency = System.currentTimeMillis() - startTime;
                    }
                    apiRequestInProgress = false;
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }


    private HttpResponse<String> sendRequestWithRetry(HttpRequest request) {
        try {

            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {

            if (e.getMessage() != null && e.getMessage().contains("header parser received no bytes")) {
                // System.err.println("[AICheck INFO] Stale connection detected, retrying request once...");
                try {
                    return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException ex) {
                //   System.err.println("[AICheck ERROR] API request failed on retry: " + ex.getMessage());
                }
            } else {
            //   System.err.println("[AICheck ERROR] API request failed: " + e.getMessage());
            }
        } catch (InterruptedException e) {
         //   System.err.println("[AICheck ERROR] API request was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private byte[] serializeToMessagePack(List<TickData> ticks) throws JsonProcessingException {
        List<List<Double>> tickValuesList = ticks.stream().map(tick -> Arrays.asList(
                (double) tick.deltaYaw, (double) tick.deltaPitch, (double) tick.accelYaw,
                (double) tick.accelPitch, (double) tick.jerkYaw, (double) tick.jerkPitch,
                (double) tick.gcdErrorYaw, (double) tick.gcdErrorPitch, (double) tick.isOnGround,
                (double) tick.isSprinting, (double) tick.isSneaking, (double) tick.isUsingItem,
                tick.playerSpeedHorizontal, tick.playerFallDistance, (double) tick.isAttacking,
                (double) tick.ticksSinceAttack
        )).collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("ticks_data", tickValuesList);

        return MSGPACK_MAPPER.writeValueAsBytes(payload);
    }

    private void handleApiResponse(String responseBody) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) jsonParser.parse(responseBody);

            Object probabilityObject = jsonResponse.get("cheat_probability");
            double probability = 0.0;
            if (probabilityObject instanceof Double) {
                probability = (Double) probabilityObject;
            } else if (probabilityObject instanceof Long) {
                probability = ((Long) probabilityObject).doubleValue();
            }
            this.lastProbability = probability;

            double oldViolationLevel = this.violationLevel;

            if (probability > PUNISHMENT_THRESHOLD) {
                this.violationLevel += (probability - PUNISHMENT_THRESHOLD) * VL_INCREASE_MULTIPLIER;
            } else if (probability < FORGIVENESS_THRESHOLD) {
                this.violationLevel -= VL_DECREASE_AMOUNT;
            }

            if (this.violationLevel < 0) {
                this.violationLevel = 0;
            }

            if (this.violationLevel > FLAG_THRESHOLD) {
                flag("prob=" + String.format("%.2f", probability) + " vl=" + String.format("%.1f", this.violationLevel));
                this.violationLevel = VL_RESET_AMOUNT;
            }
        } catch (Exception e) {
          //  System.err.println("[AICheck ERROR] Error parsing API response: " + e.getMessage());
        }
    }






    public double getViolationLevel() {
        return this.violationLevel; }

  public long getLastApiLatency() {
        return this.lastApiLatency;
  }




    public double getLastProbability() {
        return this.lastProbability; }
}