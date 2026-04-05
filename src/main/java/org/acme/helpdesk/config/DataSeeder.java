package org.acme.helpdesk.config;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.entity.Rooms;
import org.acme.helpdesk.enums.UserRole;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class);

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (User.count() == 0) {
            LOG.info("Seeding initial users...");
            createUser("JanezNovak", "Janez123",  UserRole.USER);
            createUser("AnaKovac", "Ana456", UserRole.USER);
            createUser("MarkoKrajnc", "Marko789",  UserRole.USER);
            createUser("Operater_Petra", "PetraOp123", UserRole.OPERATOR);
            createUser("Operater_Luka", "LukaOp123", UserRole.OPERATOR);
            LOG.info("Seeded 3 users and 2 operators (password: password123)");
        }

        if (Rooms.count() == 0) {
            LOG.info("Seeding initial rooms...");
            createRoom("TEHNIKA", "Tehnična pomoč uporabnikom");
            createRoom("STORITVE", "Informacije o storitvah in naročninah");
            createRoom("POGOVOR", "Splošni pogovor z operaterjem");
            LOG.info("Seeded 3 rooms");
        }
    }

    private void createUser(String username, String password, UserRole role) {
        User user = new User();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(password);
        user.role = role;
        user.persist();
    }

    private void createRoom(String name, String description) {
        Rooms room = new Rooms();
        room.name = name;
        room.description = description;
        room.persist();
    }
}
