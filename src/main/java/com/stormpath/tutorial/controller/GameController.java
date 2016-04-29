package com.stormpath.tutorial.controller;

import com.google.common.base.Stopwatch;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.tutorial.model.CommandRequest;
import com.stormpath.tutorial.model.CommandResponse;
import com.stormpath.tutorial.support.ZMachinery;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

@RestController
public class GameController {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GameController.class);

    @RequestMapping(value = "/c", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody(required = false) CommandRequest commandRequest, HttpServletRequest req) throws IOException {
        Account account = AccountResolver.INSTANCE.getAccount(req);
        String id = account.getHref().substring(account.getHref().lastIndexOf("/")+1);
        String fileName =  saveFilePath + File.separator + id + ".sav";

        StringBuffer zMachineCommands = new StringBuffer();

        Stopwatch stopwatch = Stopwatch.createStarted();

        // retrieve existing game (if any) from customData
        String zMachineSaveData = (String) account.getCustomData().get("zMachineSaveData");

        stopwatch.stop();
        log.info("time to load zMachine save data from customData: " + stopwatch);

        stopwatch = Stopwatch.createStarted();

        if (zMachineSaveData != null) {
            // save data to file to be restored in game before sending new command
            byte[] rawData = Base64.getDecoder().decode(zMachineSaveData);
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(rawData);
            fos.close();

            stopwatch.stop();
            log.info("time to write zMachine save data to file: " + stopwatch);
            stopwatch = Stopwatch.createStarted();

            // setup restore command if we had custom data
            zMachineCommands.append("restore\nlook\n");
        }

        // setup passed in command
        if (commandRequest != null && commandRequest.getRequest() != null) {
            zMachineCommands.append(commandRequest.getRequest());
            zMachineCommands.append("\n");
        }

        // setup save command
        zMachineCommands.append("save\n");

        // setup zmachine
        InputStream in = new ByteArrayInputStream(zMachineCommands.toString().getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZMachinery zMachinery = new ZMachinery(zFile, in, out, fileName);

        // get anything from zMachine in buffer
        pollStream(out, 10, 10);
        String tmpString = out.toString();

        // kill zmachine
        zMachinery.quit();

        stopwatch.stop();
        log.info("time to fire up and execute zMachine commands: " + stopwatch);

        // process output
        // get rid of "OK"s and prompts
        tmpString = tmpString.replace("Ok.\n\n>", "").replace(">", "");

        String[] tmpResponse = tmpString.split("\n\n");

        int rLength = tmpResponse.length;

        String[] gameInfo = tmpResponse[0].split("\n");
        String[] look = (rLength < 3) ? tmpResponse[1].split("\n") : tmpResponse[2].split("\n");

        String[] response = (rLength >= 4) ? tmpResponse[3].split("\n") : new String[0];

        // get response from zmachine
        CommandResponse res = new CommandResponse();
        res.setGameInfo(gameInfo);
        res.setLook(look);
        res.setRequest((commandRequest != null) ? commandRequest.getRequest() : null);
        res.setResponse(response);
        res.setStatus("SUCCESS");

        stopwatch = Stopwatch.createStarted();

        // store save file in custom data
        Path p = FileSystems.getDefault().getPath("", fileName);

        byte [] fileData = Files.readAllBytes(p);
        String saveFile = Base64.getEncoder().encodeToString(fileData);
        account.getCustomData().put("zMachineSaveData", saveFile);
        account.getCustomData().save();

        stopwatch.stop();
        log.info("time to save zMachine save data to customData: " + stopwatch);

        // return response
        return res;
    }

    private void pollStream(ByteArrayOutputStream stream, int waitMillis, int numWait) {
        int i = 0;
        while (stream.size() <= 0 && i++ < numWait) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                log.error("Interrupted during polling.", e);
            }
        }
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommandResponse internalError(IOException ex) {
        CommandResponse res = new CommandResponse();
        res.setStatus("ERROR");
        res.setMessage(ex.getMessage());
        return res;
    }
}
