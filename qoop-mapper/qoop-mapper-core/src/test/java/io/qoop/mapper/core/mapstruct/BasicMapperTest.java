package io.qoop.mapper.core.mapstruct;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    @Test
    void testToPage_whenPageFilterDataIsProvided_shouldReturnPage() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<CreateUserCommand> commandList = List.of(
                new CreateUserCommand("User1", "user1@test.com"),
                new CreateUserCommand("User2", "user2@test.com")
        );
        PageFilterData<CreateUserCommand> pageFilterData = PageFilterData.of(100L, commandList);

        // Act
        Page<User> result = mapper.toPage(pageFilterData);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("User1", result.getContent().get(0).getName());
        assertEquals("User2", result.getContent().get(1).getName());
    }

    @Test
    void testToPageFilterData_whenPageIsProvided_shouldReturnPageFilterData() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<User> userList = List.of(
                new User("Ali", "ali@test.com"),
                new User("Reza", "reza@test.com")
        );
        Page<User> page = new PageImpl<>(userList, Pageable.unpaged(), 50L);

        // Act
        PageFilterData<CreateUserCommand> result = mapper.toPageFilterData(page);

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertEquals("Ali", result.getList().get(0).getUsername());
        assertEquals("Reza", result.getList().get(1).getUsername());
    }

    @Test
    void testToPageData_whenPageIsProvided_shouldReturnPageData() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<CreateUserCommand> commandList = List.of(new CreateUserCommand("Sara", "sara@test.com"));
        Page<CreateUserCommand> page = new PageImpl<>(commandList, Pageable.unpaged(), 10L);

        // Act
        PageData<User> result = mapper.toPageData(page);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getTotalElements());
        assertEquals(10, result.getTotalPages());
        assertEquals(1, result.getContents().size());
        assertEquals("Sara", result.getContents().get(0).getName());
    }

    @Test
    void testToPage_whenPageDataIsProvided_shouldReturnPage() {
        // Arrange
        BasicMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<User> userList = List.of(new User("Hamed", "hamed@test.com"));
        PageData<User> pageData = PageData.of(20L, 2, userList);

        // Act
        Page<CreateUserCommand> result = mapper.toPage(pageData);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getTotalElements());
        assertEquals(20, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals("Hamed", result.getContent().get(0).getUsername());
    }
}