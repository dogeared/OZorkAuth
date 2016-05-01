package com.stormpath.ozorkauth.controller;

import com.google.common.base.Strings;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.ozorkauth.model.CommandRequest;
import com.stormpath.ozorkauth.model.CommandResponse;
import com.stormpath.ozorkauth.model.Registration;
import com.stormpath.ozorkauth.service.GameService;
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
import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping("/")
    public void root(HttpServletResponse res) throws IOException {
        res.sendRedirect("/v1/instructions");
    }

    @RequestMapping("/v1/instructions")
    public CommandResponse home(HttpServletRequest req) {
        String proto = (req.getHeader("x-forwarded-proto") != null) ?
            req.getHeader("x-forwarded-proto") : req.getScheme() ;
        String server = req.getServerName();
        String port = (req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort();
        String baseUrl = proto + "://" + server + port;

        CommandResponse res = new CommandResponse();

        String[] response = {
            "Welcome to the interactive OAuth2 Text Based Adventure!",
            "",
            "In order to play the game, you must:",
            "    1. Register an account",
            "    2. Get an access token using your account",
            "    3. Use the access token to send commands to the game",
            "",
            "To Register, you send a POST request to the registration endpoint (the below example uses httpie):",
            "    http POST " + baseUrl + "/v1/r givenName=Bob surName=Smith email=bob@smith.com password=123456aA",
            "",
            "To get an access token, you send a POST request to the oauth endpoint (the below example uses httpie):",
            "    http -f POST " + baseUrl + "/v1/a Origin:" + baseUrl + " grant_type=password username=bob@smith.com password=123456aA",
            "Note: The above command returns an access token and a refresh token. When the access token expires, you can use the refresh token to get a new access token.",
            "",
            "To use the access token to interact with the game, you send a POST request to the command endpoint (the below example uses httpie):",
            "    http POST " + baseUrl + "/v1/c Authorization:'Bearer <access token>'",
            "    http POST " + baseUrl + "/v1/c Authorization:'Bearer <access token>' request='go north'",
            "Note: if you don't send the request parameter, the response will contain the result of looking around your current location in the game",
            "",
            "Part of the game is discovering which language elements work to move you forward in the game. If you are impatient, here's a list of all the available commands: http://zork.wikia.com/wiki/Command_List"
        };

        res.setResponse(response);
        res.setStatus("SUCCESS");

        return res;
    }

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

        String zMachineRequest = (commandRequest != null) ? commandRequest.getRequest() : null;
        StringBuffer zMachineCommands = new StringBuffer();

        //check for restart
        if ("restart".equals(zMachineRequest)) {
            gameService.restart(account);
            zMachineRequest = null;
        } else {
            // restore games state from customData, if it exists
            gameService.loadGameState(zMachineCommands, account);
        }

        // we always want to look
        zMachineCommands.append("look\n");

        // setup passed in command
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

        gameService.cleanup(account);

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
