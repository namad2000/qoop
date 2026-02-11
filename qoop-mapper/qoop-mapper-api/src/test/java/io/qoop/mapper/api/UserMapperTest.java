package io.qoop.mapper.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    void shouldToDomainSingle_withoutContext() {
        UserEntity entity = new UserEntity(1L, "Davood");

        UserDomain domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals("Davood", domain.getName());
    }

    @Test
    void shouldToResultSingle_withoutContext() {
        UserDomain domain = new UserDomain(1L, "Davood");

        UserDto dto = mapper.toResult(domain);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Davood", dto.getFullName());
    }

    @Test
    void shouldToResultSingle_withContext_uppercase() {
        UserDomain domain = new UserDomain(1L, "Davood");

        MappingContext context = MappingContext.empty().put("upper", true);

        UserDto dto = mapper.toResult(domain, context);

        assertEquals(1L, dto.getId());
        assertEquals("DAVOOD", dto.getFullName());
    }

    @Test
    void shouldToDomainList_withContext() {
        List<UserEntity> inputs = List.of(
                new UserEntity(1L, "Ali"),
                new UserEntity(2L, "Reza")
        );

        List<UserDomain> domains = mapper.toDomain(inputs, MappingContext.empty());

        assertEquals(2, domains.size());
        assertEquals(1L, domains.get(0).getId());
        assertEquals("Ali", domains.get(0).getName());
        assertEquals(2L, domains.get(1).getId());
        assertEquals("Reza", domains.get(1).getName());
    }

    @Test
    void shouldToDomainSet_withContext() {
        Set<UserEntity> inputs = Set.of(
                new UserEntity(1L, "Ali"),
                new UserEntity(2L, "Reza")
        );

        Set<UserDomain> domains = mapper.toDomain(inputs, MappingContext.empty());

        assertEquals(2, domains.size());

        assertTrue(domains.stream().anyMatch(d -> d.getId().equals(1L) && d.getName().equals("Ali")));
        assertTrue(domains.stream().anyMatch(d -> d.getId().equals(2L) && d.getName().equals("Reza")));
    }


    @Test
    void shouldToResultSet_withContext_uppercase() {
        Set<UserDomain> domains = Set.of(
                new UserDomain(1L, "Ali"),
                new UserDomain(2L, "Reza")
        );

        MappingContext context = MappingContext.empty().put("upper", true);

        Set<UserDto> dtos = mapper.toResult(domains, context);

        assertEquals(2, dtos.size());

        assertTrue(dtos.stream().anyMatch(d -> d.getId().equals(1L) && d.getFullName().equals("ALI")));
        assertTrue(dtos.stream().anyMatch(d -> d.getId().equals(2L) && d.getFullName().equals("REZA")));
    }

    @Test
    void shouldHandleEmptyCollections() {
        List<UserDomain> domains = mapper.toDomain(List.of(), MappingContext.empty());
        assertNotNull(domains);
        assertTrue(domains.isEmpty());

        Set<UserDto> dtos = mapper.toResult(Set.of(), MappingContext.empty());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}
