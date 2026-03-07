package io.qoop.mapper.core.mapper;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PageMapperTest {

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
    static class CommandToUserMapper implements PageMapper<CreateUserCommand, User> {
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
    void testToPage_whenPageFilterDataIsProvided_shouldReturnPage() {
        // Arrange
        PageMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<User> userList = List.of(new User("Hamed", "hamed@test.com"));
        PageFilterData<User> pageData = PageFilterData.of(20L, userList);

        // Act
        Page<CreateUserCommand> result = mapper.toPage(pageData);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getTotalElements());
        assertEquals(20, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals("Hamed", result.getContent().get(0).getUsername());
    }

    @Test
    void testToPageFilterData_whenPageIsProvided_shouldReturnPageFilterData() {
        // Arrange
        PageMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
        List<CreateUserCommand> commandList = List.of(
                new CreateUserCommand("Ali", "ali@test.com"),
                new CreateUserCommand("Reza", "reza@test.com")
        );
        Page<CreateUserCommand> page = new PageImpl<>(commandList, Pageable.unpaged(), 50L);

        // Act
        PageFilterData<User> result = mapper.toPageFilterData(page);

        // Assert
        assertNotNull(result);
        assertEquals(50L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertEquals("Ali", result.getList().get(0).getName());
        assertEquals("Reza", result.getList().get(1).getName());
    }

    @Test
    void testToPageData_whenPageIsProvided_shouldReturnPageData() {
        // Arrange
        PageMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
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
        PageMapper<CreateUserCommand, User> mapper = new CommandToUserMapper();
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