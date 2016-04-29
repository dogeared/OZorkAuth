package com.stormpath.tutorial.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.tutorial.model.CommandRequest;
import com.stormpath.tutorial.model.CommandResponse;
import com.stormpath.tutorial.support.ZMachinery;
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
import java.util.HashMap;
import java.util.Map;

@RestController
public class GameController {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    @RequestMapping(value = "/c", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody(required = false) CommandRequest commandRequest, HttpServletRequest req) {
        Account account = AccountResolver.INSTANCE.getAccount(req);
        String id = account.getHref().substring(account.getHref().lastIndexOf("/")+1);
        String fileName =  saveFilePath + File.separator + id + ".sav";

        StringBuffer zMachineCommands = new StringBuffer();

        // retrieve existing game (if any) from customData
        String zMachineSaveData = (String) account.getCustomData().get("zMachineSaveData");

        if (zMachineSaveData != null) {
            // save data to file to be restored in game before sending new command
            byte[] rawData = Base64.getDecoder().decode(zMachineSaveData);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                fos.write(rawData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

        // store save file in custom data
        Path p = FileSystems.getDefault().getPath("", fileName);
        try {
            byte [] fileData = Files.readAllBytes(p);
            String saveFile = Base64.getEncoder().encodeToString(fileData);
            account.getCustomData().put("zMachineSaveData", saveFile);
            account.getCustomData().save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return response
        return res;
    }

    private void pollStream(ByteArrayOutputStream stream, int waitMillis, int numWait) {
        int i = 0;
        while (stream.size() <= 0 && i < numWait) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    @RequestMapping("/l")
    public Object look(HttpServletRequest req) {

        return null;
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> unauthorized(UnauthorizedException ex) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("status", "error");
        ret.put("message", ex.getMessage());
        return ret;
    }

    class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

}
