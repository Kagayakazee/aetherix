package xd.kagayakazee.aetherix.checks.combat;

import xd.kagayakazee.aetherix.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class DataSession {
    private final UUID playerUUID;
    private final String playerName;
    private final String status; // "LEGIT" or "CHEAT"
    private final List<TickData> recordedTicks = new ArrayList<>();
    private final long startTime;

    public DataSession(UUID playerUUID, String playerName, String status) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.status = status;
        this.startTime = System.currentTimeMillis();
    }

    public void addTick(TickData tickData) {
        recordedTicks.add(tickData);
    }

    public void saveAndClose() throws IOException {
        if (recordedTicks.isEmpty()) {
            return;
        }

        File dataFolder = new File(Main.getInstance().getDataFolder(), "datacollection");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(startTime));
        String fileName = String.format("%s_%s_%s.csv", status, playerName, timestamp);
        File outputFile = new File(dataFolder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(TickData.getCsvHeader());
            writer.newLine();

            for (TickData tick : recordedTicks) {
                writer.write(tick.toCsvRow(this.status));
                writer.newLine();
            }
        }
    }
}
