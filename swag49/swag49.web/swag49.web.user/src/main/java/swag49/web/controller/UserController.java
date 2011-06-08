package swag49.web.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import swag49.dao.DataAccessObject;
import swag49.model.MapLocation;
import swag49.model.User;
import swag49.util.Log;
import swag49.web.TokenService;
import swag49.web.model.MapLocationDTO;
import swag49.web.model.UserDTO;
import swag49.web.model.UserLoginDTO;

import javax.validation.Valid;
import java.util.*;

/**
 * @author michael
 */
@Controller
@Scope(value = "session")
@RequestMapping(value = "/user")
public class UserController {

    @Log
    private static Logger logger;

    private static String mapController = "/swag/map/authenticate/";

    @Autowired
    @Qualifier("userDAO")
    private DataAccessObject<User, Long> userDAO;

    @Autowired
    @Qualifier("mapLoactionDAO")
    private DataAccessObject<MapLocation, Long> mapLocationDAO;

    @Autowired
    private TokenService tokenService;

    private UserDTO loggedInUser = null;
    private UUID userToken = null;

    public UserController() {
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @Transactional("swag49.user")
    public String registerUser(@Valid @ModelAttribute("user")
                               UserDTO userDTO, BindingResult bingBindingResult,
                               Map<String, Object> map) {

        if (bingBindingResult.hasErrors())
            return "register";

        User user = new User();
        user.setUsername(userDTO.getUsername());
        Collection<User> users = userDAO.queryByExample(user);

        // check if username already exists
        if (users != null && users.size() > 0) {
            map.put("registerError", "User with username: " +
                    userDTO.getUsername() + " is already registered!");
            return "register";
        }

        // register user
        System.out.println("Registered new user:" + userDTO);

        // TODO set correct utc offset
        userDTO.setUtcOffset(0);
        userDAO.create(userDTO.createUserEntity());

        return "redirect:./";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Map<String, Object> map) {
        UserDTO userDTO = new UserDTO();
        map.put("user", userDTO);
        map.put("userList", getRegisteredUsers());
        return "register";
    }

    @Transactional("swag49.user")
    private List<UserDTO> getRegisteredUsers() {
        List<UserDTO> users = new ArrayList<UserDTO>();

//        Collection<User> registeredUsers = userDAO.queryByExample(new User());
//        if (registeredUsers != null) {
//            for (User user : registeredUsers) {
//                users.add(new UserDTO(user));
//            }
//        }
        return users;
    }

    @RequestMapping(value = "/delete/{id}")
    @Transactional("swag49.user")
    public String deleteUser(@PathVariable("id") Long userID) {
        User user = userDAO.get(userID);
        if (user != null) {
            userDAO.delete(user);
            System.out.println("Remove User with username: " + user.getUsername());
        }
        return "redirect:./";
    }

    @RequestMapping(value = "/")
    public String handle(Map<String, Object> map) {
        map.put("loggedInUser", loggedInUser);
        return "overview";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Map<String, Object> map) {
        map.put("user", new UserLoginDTO());
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginUser(@Valid @ModelAttribute("user")
                            UserLoginDTO userLoginDTO, BindingResult bingBindingResult, Map<String, Object> map) {

        if (bingBindingResult.hasErrors())
            return "login";

        User template = new User();
        template.setUsername(userLoginDTO.getUsername());
        Collection<User> users = userDAO.queryByExample(template);
        User user = null;

        if (users != null && users.size() > 0) {
            Iterator<User> iterator = users.iterator();
            user = iterator.next();
        }

        if (user == null) {
            map.put("loginError", "Username not found!");
            return "login";
        }

        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            map.put("loginError", "Username/Password combination doesn't match!");
            return "login";
        }

        UserDTO loggedInUser = new UserDTO(user);

        // check if user is already logged in in another session
        if (tokenService.hasTokenForUser(loggedInUser.createUserEntity())) {
            map.put("loginError", "User is already logged in in another session!");
            return "login";
        }

        System.out.println("Logged in user:" + loggedInUser);

        this.loggedInUser = loggedInUser;

        // set UserToken
        UUID token = tokenService.generateToken(loggedInUser.createUserEntity());
        this.userToken = token;

        return "redirect:./";
    }

    @RequestMapping(value = "/overview")
    public String handleOverview() {
        return "redirect:./";
    }

    @RequestMapping(value = "/logout")
    public String logoutUser() {
        tokenService.removeToken(userToken);
        this.loggedInUser = null;
        return "redirect:./";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(Map<String, Object> map) {
        if (this.loggedInUser != null) {
            UserDTO user = new UserDTO(userDAO.get(loggedInUser.getId()));
            System.out.println("Try to update user: " + user);
            map.put("user", user);
            return "edit";
        } else
            return "redirect:./";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @Transactional("swag49.user")
    public String editUser(@Valid @ModelAttribute("user")
                           UserDTO userDTO, BindingResult bingBindingResult) {
        if (bingBindingResult.hasErrors())
            return "edit";

        // TODO set corect utcOffset
        userDTO.setUtcOffset(0);
        System.out.println("Updated user:" + userDTO);

        User user = userDAO.get(loggedInUser.getId());
        this.loggedInUser = new UserDTO(userDTO.updateUser(user));

        return "redirect:./";
    }

    @RequestMapping(value = "/maps", method = RequestMethod.GET)
    @Transactional("swag49.user")
    public String maps(Map<String, Object> map) {
        if (loggedInUser == null)
            return "redirect:./";

        User user = userDAO.get(loggedInUser.getId());

        List<MapLocationDTO> availableMaps = getMapLocations(user);
        map.put("availableMapLocations", availableMaps);
        map.put("myMapLocations", getUserMapLocations(user));

        return "maps";
    }

    @Transactional("swag49.user")
    private List<MapLocationDTO> getUserMapLocations(User user) {
        List<MapLocationDTO> mapLocations = new ArrayList<MapLocationDTO>();

        for (MapLocation mapLocation : user.getMapLocations())
            mapLocations.add(new MapLocationDTO(mapLocation));

        return mapLocations;
    }

    @RequestMapping(value = "/join/{id}")
    @Transactional("swag49.user")
    public String joinMap(@PathVariable("id") Long mapLocationID) {

        User user = userDAO.get(loggedInUser.getId());
        user.getMapLocations().add(getMapLocation(mapLocationID));
        userDAO.update(user);

        return "redirect:../maps";
    }


    @RequestMapping(value = "/play/{id}")
    public String playMap(@PathVariable("id") Long mapLocationID) {

        MapLocation mapLocation = getMapLocation(mapLocationID);

        return "redirect:" + mapLocation.getUrl() + mapController + userToken.toString();
    }

    @Transactional("swag49.user")
    public MapLocation getMapLocation(Long mapLocationID) {
        MapLocation mapLocation = mapLocationDAO.get(mapLocationID);
        return mapLocation;
    }

    @Transactional("swag49.user")
    public List<MapLocationDTO> getMapLocations(User user) {
        List<MapLocationDTO> mapLocations = new ArrayList<MapLocationDTO>();

        Collection<MapLocation> availableMaps = mapLocationDAO.queryByExample(new MapLocation());

        if (availableMaps != null) {
            Iterator<MapLocation> iterator = availableMaps.iterator();
            while (iterator.hasNext()) {
                MapLocation mapLocation = iterator.next();
                if (!user.getMapLocations().contains(mapLocation))
                    mapLocations.add(new MapLocationDTO(mapLocation));
            }
        }

        return mapLocations;
    }

    public UserDTO getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(UserDTO loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
