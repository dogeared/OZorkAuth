package com.stormpath.ozorkauth.service;

import com.google.common.base.Stopwatch;
import com.stormpath.sdk.account.Account;
import com.stormpath.ozorkauth.model.CommandResponse;
import com.stormpath.ozorkauth.support.ZMachinery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class GameService {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    public String getSaveFile(Account account) {
        String id = account.getHref().substring(account.getHref().lastIndexOf("/")+1);
        return saveFilePath + File.separator + id + ".sav";
    }

    public void restart(Account account) {
        // retrieve existing game (if any) from customData
        account.getCustomData().remove("zMachineSaveData");
        account.getCustomData().save();
    }

    public void loadGameState(StringBuffer zMachineCommands, Account account) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // retrieve existing game (if any) from customData
        String zMachineSaveData = (String) account.getCustomData().get("zMachineSaveData");

        stopwatch.stop();
        log.info("time to load zMachine save data from customData: " + stopwatch);

        if (zMachineSaveData != null) {
            stopwatch = Stopwatch.createStarted();

            // save data to file to be restored in game before sending new command
            byte[] rawData = Base64.getDecoder().decode(zMachineSaveData);
            Path p = FileSystems.getDefault().getPath("", getSaveFile(account));
            Files.write(p, rawData);

            stopwatch.stop();
            log.info("time to write zMachine save data to file: " + stopwatch);

            // setup restore command if we had custom data
            zMachineCommands.append("restore\n");
        }
    }

    public void saveGameState(Account account) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // store save file in custom data
        Path p = FileSystems.getDefault().getPath("", getSaveFile(account));

        byte [] fileData = Files.readAllBytes(p);
        String saveFile = Base64.getEncoder().encodeToString(fileData);
        account.getCustomData().put("zMachineSaveData", saveFile);
        account.getCustomData().save();

        stopwatch.stop();
        log.info("time to save zMachine save data to customData: " + stopwatch);
        log.info("Wrote to file: " + getSaveFile(account));
    }

    public String doZMachine(StringBuffer zMachineCommands, Account account) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        log.info("executing: " + zMachineCommands.toString().replace("\n", ", ") + " for: " + account.getEmail());

        String fileName = getSaveFile(account);

        // setup zmachine
        InputStream in = new ByteArrayInputStream(zMachineCommands.toString().getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZMachinery zMachinery = new ZMachinery(zFile, in, out, fileName);

        // get anything from zMachine in buffer
        pollStream(out, 10, 10);
        String res = out.toString();

        if (zMachineCommands.indexOf("save\n") >= 0) {
            // ensure save file is written before killing zmachine
            pollFile(new File(fileName), 10, 10);
        }

        // kill zmachine
        zMachinery.quit();

        stopwatch.stop();
        log.info("time to fire up and execute zMachine commands: " + stopwatch);

        return res;
    }

    public CommandResponse processZMachineResponse(String zMachineRequest, String zMachineResponse) {
        // process output
        // get rid of "OK"s and prompts
        zMachineResponse = zMachineResponse.replace("Ok.\n\n>", "").replace(">", "");

        String[] tmpResponse = zMachineResponse.split("\n\n");

        int rLength = tmpResponse.length;

        String[] gameInfo = tmpResponse[0].split("\n");
        String[] look = (rLength < 3) ? tmpResponse[1].split("\n") : tmpResponse[2].split("\n");

        String[] response = (rLength >= 4) ? tmpResponse[3].split("\n") : null;

        // get response from zmachine
        CommandResponse res = new CommandResponse();
        res.setGameInfo(gameInfo);
        res.setLook(look);
        res.setRequest(zMachineRequest);
        res.setResponse(response);
        res.setStatus("SUCCESS");

        return res;
    }

    public void cleanup(Account account) throws IOException {
        String fileName = getSaveFile(account);
        Path p = FileSystems.getDefault().getPath("", getSaveFile(account));
        Files.deleteIfExists(p);
    }

    private void pollStream(ByteArrayOutputStream stream, int waitMillis, int numWait) {
        int i = 0;
        while (stream.size() <= 0 && i++ < numWait) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                log.error("Interrupted during polling stream: {}", e.getMessage(), e);
            }
        }
    }

    private void pollFile(File f, int waitMillis, int numWait) {
        // hack: ensure save file is written, avoid infinite
        int i = 0;
        while (i++ < numWait && (!f.exists() || f.length() == 0)) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                log.error("Interrupted during polling file: {}", e.getMessage(), e);
            }
        }
    }
}
