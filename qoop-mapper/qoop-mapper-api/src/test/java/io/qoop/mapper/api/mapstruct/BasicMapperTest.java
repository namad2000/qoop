package io.qoop.mapper.api.mapstruct;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BasicMapperTest {

    // --- 1. Mock Implementation for Testing ---
    // Scenario: Mapping between a Command (Source) and a Domain Entity (Target)
    @Getter
    static class CreateUserCommand {
        private final String username;
        private final String email;

        public CreateUserCommand(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }

    static class User {
        private final String name;
        private final String contact;

        public User(String name, String contact) {
            this.name = name;
            this.contact = contact;
        }

        public String getName() {
            return name;
        }

        public String getContact() {
            return contact;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', contact='" + contact + "'}";
        }
    }

    // The Mapper Implementation
    static class CommandToUserMapper implements BasicMapper<CreateUserCommand, User> {
        @Override
        public User toTarget(CreateUserCommand source) {
            return new User(source.getUsername(), source.getEmail());
        }

        @Override
        public CreateUserCommand toSource(User target) {
            return new CreateUserCommand(target.getName(), target.getContact());
        }
    }

    // --- 2. Test Cases ---

    @Test
    void testToTarget_whenSourceIsProvided_shouldReturnTarget() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        CreateUserCommand command = new CreateUserCommand("Ali", "ali@example.com");

        // Act
        User result = mapper.toTarget(command);

        // Assert
        assertNotNull(result);
        assertEquals("Ali", result.getName());
        assertEquals("ali@example.com", result.getContact());
    }

    @Test
    void testToSource_whenTargetIsProvided_shouldReturnSource() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        User user = new User("Reza", "reza@example.com");

        // Act
        CreateUserCommand result = mapper.toSource(user);

        // Assert
        assertNotNull(result);
        assertEquals("Reza", result.getUsername());
        assertEquals("reza@example.com", result.getEmail());
    }

    @Test
    void testToOptionalTarget_whenSourceIsPresent_shouldReturnMappedTarget() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Optional<CreateUserCommand> optionalCommand = Optional.of(new CreateUserCommand("Sara", "sara@example.com"));

        // Act
        Optional<User> result = mapper.toTarget(optionalCommand);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Sara", result.get().getName());
    }

    @Test
    void testToOptionalTarget_whenOptionalSourceIsEmpty_shouldReturnEmpty() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Optional<CreateUserCommand> optionalCommand = Optional.empty();

        // Act
        Optional<User> result = mapper.toTarget(optionalCommand);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testToOptionalSource_whenTargetIsPresent_shouldReturnMappedSource() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Optional<User> optionalUser = Optional.of(new User("Mohammad", "mohammad@example.com"));

        // Act
        Optional<CreateUserCommand> result = mapper.toSource(optionalUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Mohammad", result.get().getUsername());
    }

    @Test
    void testToOptionalSource_whenOptionalTargetIsEmpty_shouldReturnEmpty() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Optional<User> optionalUser = Optional.empty();

        // Act
        Optional<CreateUserCommand> result = mapper.toSource(optionalUser);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testToTargetList_whenSourceListIsProvided_shouldReturnTarget() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<CreateUserCommand> commands = List.of(
                new CreateUserCommand("User1", "user1@test.com"),
                new CreateUserCommand("User2", "user2@test.com")
        );

        // Act
        List<User> result = mapper.toTarget(commands);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("User1", result.get(0).getName());
        assertEquals("User2", result.get(1).getName());
    }

    @Test
    void testToTargetList_whenSourceListIsEmpty_shouldReturnEmpty() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<CreateUserCommand> commands = List.of();

        // Act
        List<User> result = mapper.toTarget(commands);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToSourceList_whenTargetListIsProvided_shouldReturnSource() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<User> users = List.of(
                new User("Target1", "target1@test.com"),
                new User("Target2", "target2@test.com")
        );

        // Act
        List<CreateUserCommand> result = mapper.toSource(users);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Target1", result.get(0).getUsername());
        assertEquals("Target2", result.get(1).getUsername());
    }

    @Test
    void testToTargetSet_whenSourceSetIsProvided_shouldReturnTargetSet() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Set<CreateUserCommand> commands = Set.of(
                new CreateUserCommand("User1", "user1@test.com"),
                new CreateUserCommand("User2", "user2@test.com")
        );

        // Act
        Set<User> result = mapper.toTarget(commands);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User1")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User2")));
    }

    @Test
    void testToTargetSet_whenSourceSetIsEmpty_shouldReturnEmptySet() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Set<CreateUserCommand> commands = Set.of();

        // Act
        Set<User> result = mapper.toTarget(commands);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToSourceSet_whenTargetSetIsProvided_shouldReturnSourceSet() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        Set<User> users = Set.of(
                new User("Target1", "target1@test.com"),
                new User("Target2", "target2@test.com")
        );

        // Act
        Set<CreateUserCommand> result = mapper.toSource(users);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getUsername().equals("Target1")));
        assertTrue(result.stream().anyMatch(c -> c.getUsername().equals("Target2")));
    }
}