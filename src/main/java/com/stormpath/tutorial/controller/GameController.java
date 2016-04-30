package com.stormpath.tutorial.controller;

import com.google.common.base.Strings;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.tutorial.model.CommandRequest;
import com.stormpath.tutorial.model.CommandResponse;
import com.stormpath.tutorial.model.Registration;
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

    @Autowired
    Client client;

    @Autowired
    Application application;

    @RequestMapping(value = "/v1/r", method = RequestMethod.POST)
    public CommandResponse register(@RequestBody Registration registration) {

        if (
            registration == null ||
            Strings.isNullOrEmpty(registration.getGivenName()) || Strings.isNullOrEmpty(registration.getSurName()) ||
            Strings.isNullOrEmpty(registration.getEmail()) || Strings.isNullOrEmpty(registration.getPassword())
        ) {
            throw new RegistrationException("givenName, surName, email and password are all required to register.");
        }

        Account account = client.instantiate(Account.class);
        account
            .setEmail(registration.getEmail())
            .setPassword(registration.getPassword())
            .setGivenName(registration.getGivenName())
            .setSurname(registration.getSurName());

        account = application.createAccount(account);

        CommandResponse res = new CommandResponse();
        res.setStatus("SUCCESS");
        String[] response = {
            "Thank you for registering!",
            account.getGivenName() + " " + account.getSurname(),
            account.getEmail()
        };
        res.setResponse(response);

        return res;
    }

    @RequestMapping(value = "/v1/c", method = RequestMethod.POST)
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

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommandResponse badRegistration(RegistrationException ex) {
        CommandResponse res = new CommandResponse();
        res.setStatus("ERROR");
        res.setMessage(ex.getMessage());
        return res;
    }

    class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }
}
