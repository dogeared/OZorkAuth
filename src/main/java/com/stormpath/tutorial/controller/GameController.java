package com.stormpath.tutorial.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.tutorial.model.CommandRequest;
import com.stormpath.tutorial.model.CommandResponse;
import com.stormpath.tutorial.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class GameController {

    @Value("#{ @environment['zmachine.file'] ?: '/tmp/zork1.z3' }")
    String zFile;

    @Value("#{ @environment['zmachine.save.file.path'] ?: '/tmp' }")
    String saveFilePath;

    @Autowired
    GameService gameService;

    @RequestMapping(value = "/c", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody(required = false) CommandRequest commandRequest, HttpServletRequest req) throws IOException {
        Account account = AccountResolver.INSTANCE.getAccount(req);

        StringBuffer zMachineCommands = new StringBuffer();

        // restore games state from customData, if it exists
        gameService.loadGameState(zMachineCommands, account);

        // we always want to look
        zMachineCommands.append("look\n");

        // setup passed in command
        String zMachineRequest = (commandRequest != null) ? commandRequest.getRequest() : null;
        if (zMachineRequest != null) {
            zMachineCommands.append(zMachineRequest + "\n");
            zMachineCommands.append("save\n");
        }

        // execute game move (which may just be looking)
        String zMachineResponse = gameService.doZMachine(zMachineCommands, account);

        CommandResponse res = gameService.processZMachineResponse(zMachineRequest, zMachineResponse);

        if (zMachineRequest != null) {
            gameService.saveGameState(account);
        }

        // return response
        return res;
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
