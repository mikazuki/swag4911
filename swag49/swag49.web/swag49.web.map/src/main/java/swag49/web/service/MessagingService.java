package swag49.web.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import swag49.dao.DataAccessObject;
import swag49.model.Player;
import swag49.util.Log;
import swag49.web.InternalMessageStore;
import swag49.web.model.MessageDTO;
import swag49.web.model.MessageQueryResponse;
import swag49.web.model.PlayerDTO;

import java.util.Collection;

@Controller
public class MessagingService {

    @Log
    private Logger log;

    @Autowired
    @Qualifier("playerDAO")
    private DataAccessObject<Player, Long> playerDAO;

    @Autowired
    private InternalMessageStore messageStore;

    @RequestMapping(value = "/messaging/user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public PlayerDTO getUser(@PathVariable("userId") long userId) {
        Player playerExample = new Player();
        //noinspection NullableProblems
        playerExample.setResources(null);
        //noinspection NullableProblems
        playerExample.setUpkeep(null);
        //noinspection NullableProblems
        playerExample.setIncome(null);
        playerExample.setUserId(userId);

        Collection<Player> queryResult = playerDAO.queryByExample(playerExample);
        assert queryResult.size() == 1;

        Player player = queryResult.iterator().next();

        return new PlayerDTO(player.getId(), userId, player.getOnline());
    }

    @RequestMapping(value = "/messaging/receive", method = RequestMethod.PUT)
    @ResponseBody
    public void receiveMessage(@RequestBody MessageDTO message) {
        log.info("received message {}", message);

        messageStore.addReceivedMessage(message.getReceiver().getId(), message);
    }

    @RequestMapping(value = "/messaging/list", method = RequestMethod.PUT)
    @ResponseBody
    public void listMessages(@RequestBody MessageQueryResponse messageQueryResponse) {
        log.info("received {} messages", messageQueryResponse.getMessages().size());

        messageStore.addMessageList(messageQueryResponse.getMessageQuery().getUserId(),
                messageQueryResponse.getMessages());
    }


}
