package swag49.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import swag49.dao.DataAccessObject;
import swag49.model.Map;
import swag49.model.ResourceType;
import swag49.model.Tile;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author michael
 */
@Component
@Scope(value = "singleton")
public class MapHelper {

    @Autowired
    @Qualifier("mapDAO")
    private DataAccessObject<Map, Long> mapDAO;

    @Autowired
    @Qualifier("tileDAO")
    private DataAccessObject<Tile, Tile.Id> tileDAO;


    @PostConstruct
    @Transactional
    public void init() {
        try {
            //create map
            Map map = new Map();
            map.setUrl("http://localhost:8080/map");

            Random rnd = new Random(0);

            if (mapDAO.queryByExample(map).isEmpty()) {
                map.setMaxUsers(100);
                Set<Tile> tiles = new HashSet<Tile>();
                map = mapDAO.create(map);
                for (int i = 0; i < 100; i++) {
                    for (int j = 0; j < 100; j++) {
                        Tile tile = new Tile(map, i, j);

                        double d = rnd.nextDouble();

                        if (d < 0.025)
                            tile.setSpecial(ResourceType.WOOD);
                        else if (d < 0.05)
                            tile.setSpecial(ResourceType.STONE);
                        else if (d < 0.075)
                            tile.setSpecial(ResourceType.GOLD);
                        else if (d < 0.1)
                            tile.setSpecial(ResourceType.CROPS);
                        else
                            tile.setSpecial(ResourceType.NONE);


                        tile.setBase(null);
                        tile = tileDAO.create(tile);

                        tiles.add(tile);
                    }
                }
                map.setConsistsOf(tiles);

                mapDAO.update(map);
            }

        } catch (Exception e) {
            // nothing to do
        }

        try {
            Random rnd = new Random(0);
            //create map
            Map map = new Map();
            map.setUrl("http://localhost:8080/map1");
            if (mapDAO.queryByExample(map).isEmpty()) {
                map.setMaxUsers(100);
                Set<Tile> tiles = new HashSet<Tile>();
                map = mapDAO.create(map);
                for (int i = 0; i < 50; i++) {
                    for (int j = 0; j < 50; j++) {
                        Tile tile = new Tile(map, i, j);
                        double d = rnd.nextDouble();

                        if (d < 0.025)
                            tile.setSpecial(ResourceType.WOOD);
                        else if (d < 0.05)
                            tile.setSpecial(ResourceType.STONE);
                        else if (d < 0.075)
                            tile.setSpecial(ResourceType.GOLD);
                        else if (d < 0.1)
                            tile.setSpecial(ResourceType.CROPS);
                        else
                            tile.setSpecial(ResourceType.NONE);

                        tile.setBase(null);
                        tile = tileDAO.create(tile);

                        tiles.add(tile);
                    }
                }
                map.setConsistsOf(tiles);

                mapDAO.update(map);
            }
        } catch (Exception e) {
            // nothing to do
        }
    }
}
